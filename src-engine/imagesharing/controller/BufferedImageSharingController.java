/*
 * Copyright (c) 2019 Felipe Michels Fontoura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 
 * Except as contained in this notice, the name of the above copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use or
 * other dealings in this Software without prior written authorization.
 */

package imagesharing.controller;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import imagesharing.util.IntegerArithmetics;
import imagesharing.util.RunOnceRunnable;

/**
 * A {@linkplain SingleImageSharingController} which shares an updateable buffered image.
 *
 * @author Felipe Michels Fontoura
 */
public class BufferedImageSharingController implements SingleImageSharingController
{
	private final Object imageLock = new Object();

	private final int tileWidth = 128;
	private final int tileHeight = 128;
	private final long timeQuantum = 100;

	private volatile boolean shouldRun = false;
	private volatile boolean running = false;

	private volatile ImageIdentifier imageIdentifier = null;
	private volatile long nextImageNumber = 0;
	private volatile TileData[][] tiles = null;
	private volatile BufferedImage currentImage = null;
	private volatile int currentImageNumber = 0;
	private volatile BufferedImage candidateImage = null;
	private volatile int candidateImageNumber = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start()
	{
		boolean startNow = false;
		synchronized (this.imageLock)
		{
			if (!this.shouldRun)
			{
				this.shouldRun = true;
				if (!this.running)
				{
					this.running = true;
					startNow = true;
				}
			}
		}

		if (startNow)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					BufferedImageSharingController.this.run();
				}
			});
			thread.start();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop()
	{
		synchronized (this.imageLock)
		{
			if (this.shouldRun)
			{
				this.shouldRun = false;
				if (this.running)
				{
					this.imageLock.notifyAll();
				}
			}
		}
	}

	/**
	 * Sets the image that should be shared by this controller.
	 * <p/>
	 * If the controller is active, this method will trigger a asynchronous update.
	 *
	 * @param bufferedImage The image or {@code null}.
	 */
	public void setImage(BufferedImage bufferedImage)
	{
		synchronized (this.imageLock)
		{
			this.candidateImage = bufferedImage;
			this.candidateImageNumber++;
		}
	}

	/**
	 * Gets the image shared by this controller.
	 *
	 * @return The image or {@code null}.
	 */
	public BufferedImage getImage()
	{
		return this.candidateImage;
	}

	public boolean waitUntilImageUpdated() throws InterruptedException
	{
		boolean result = false;
		synchronized (this.imageLock)
		{
			if (this.running)
			{
				result = true;
				int desiredImageNumber = this.currentImageNumber;
				while (true)
				{
					if (this.candidateImageNumber < desiredImageNumber)
					{
						this.imageLock.wait();
					}
					else
					{
						break;
					}

					if (!this.running)
					{
						result = false;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageChangesReport generateImageChangesReport(ImageIdentifier imageIdentifier, long instant)
	{
		if (imageIdentifier == null)
		{
			throw new NullPointerException();
		}

		ImageChangesReport response;
		synchronized (this.imageLock)
		{
			if (this.imageIdentifier != null && this.imageIdentifier.equals(imageIdentifier))
			{
				// calculate the number of tiles in both x and y axes.
				int tilesXAxis = IntegerArithmetics.divideAndRoundUp(this.currentImage.getWidth(), this.tileWidth);
				int tilesYAxis = IntegerArithmetics.divideAndRoundUp(this.currentImage.getHeight(), this.tileHeight);

				// check for changed tiles since given instant.
				long maxTileTimestamp = 0;
				LinkedList<TilePosition> chosenTiles = new LinkedList<TilePosition>();
				for (int tileX = 0; tileX < tilesXAxis; tileX++)
				{
					for (int tileY = 0; tileY < tilesYAxis; tileY++)
					{
						TileData tileData = this.tiles[tileX][tileY];
						maxTileTimestamp = Math.max(maxTileTimestamp, tileData.whenLastUpdated);
						if (tileData.whenLastUpdated > instant)
						{
							chosenTiles.add(new TilePosition(tileX, tileY));
						}
					}
				}

				// the response is a report describing changed tiles in this shared image.
				response = new ImageChangesReport(imageIdentifier, maxTileTimestamp, chosenTiles);
			}
			else
			{
				// a resposta indica que a tela já mudou, e portanto deve ser recarregada por inteiro.
				response = null;
			}
		}
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileImage getTileImage(ImageIdentifier imageIdentifier, TilePosition tilePosition)
	{
		if (imageIdentifier == null)
		{
			throw new NullPointerException("The image identifier must be provided!");
		}
		if (tilePosition == null)
		{
			throw new NullPointerException("The tile position must be provided!");
		}

		TileImage response;
		synchronized (this.imageLock)
		{
			if (this.imageIdentifier != null && this.imageIdentifier.equals(imageIdentifier))
			{
				if (tilePosition.getX() < 0 || this.tiles.length <= tilePosition.getX() || tilePosition.getY() < 0 || this.tiles[0].length <= tilePosition.getY())
				{
					throw new IndexOutOfBoundsException("The tile position must be within image boundaries!");
				}

				TileData tileData = this.tiles[tilePosition.getX()][tilePosition.getY()];
				response = new TileImage(this.currentImage, tileData.x0, tileData.x1, tileData.y0, tileData.y1, tileData.whenLastUpdated);
			}
			else
			{
				response = null;
			}
		}
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ImageIdentifier> getImageIdentifiers()
	{
		List<ImageIdentifier> result;
		synchronized (this.imageLock)
		{
			if (this.imageIdentifier != null)
			{
				result = Collections.singletonList(this.imageIdentifier);
			}
			else
			{
				result = null;
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescription getImageDescription(ImageIdentifier imageIdentifier)
	{
		if (imageIdentifier == null)
		{
			throw new NullPointerException("The image identifier must be provided!");
		}

		ImageDescription response;
		synchronized (this.imageLock)
		{
			if (this.imageIdentifier != null && this.imageIdentifier.equals(imageIdentifier))
			{
				// determina o número de peças.
				int tilesXAxis = IntegerArithmetics.divideAndRoundUp(this.currentImage.getWidth(), this.tileWidth);
				int tilesYAxis = IntegerArithmetics.divideAndRoundUp(this.currentImage.getHeight(), this.tileHeight);

				// verifica qual o timestamp mais recente da tela.
				long mostRecent = 0;
				for (int tileX = 0; tileX < tilesXAxis; tileX++)
				{
					for (int tileY = 0; tileY < tilesYAxis; tileY++)
					{
						mostRecent = Math.max(this.tiles[tileX][tileY].whenLastUpdated, mostRecent);
					}
				}

				// a resposta indica o token, o tamanho da tela e o timestamp mais recente.
				response = new ImageDescription(this.imageIdentifier, this.currentImage.getWidth(), this.currentImage.getHeight(), this.tileWidth, this.tileHeight, mostRecent);
			}
			else
			{
				response = null;
			}
		}
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageIdentifier getImageIdentifier()
	{
		return this.imageIdentifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescription getImageDescription()
	{
		ImageIdentifier imageIdentifier = this.imageIdentifier;
		if (imageIdentifier != null)
		{
			return this.getImageDescription(this.imageIdentifier);
		}
		else
		{
			return null;
		}
	}

	private void run()
	{
		// polls the buffered image for changes.
		long targetTimestamp = this.timeQuantum * (this.getTimestamp() / this.timeQuantum);
		while (true)
		{
			// waits until the next timestamp.
			long currentTimestamp = this.getTimestamp();
			synchronized (this.imageLock)
			{
				boolean continueRunning = this.shouldRun;
				while (currentTimestamp < targetTimestamp)
				{
					if (!continueRunning)
					{
						break;
					}
					try
					{
						this.imageLock.wait(targetTimestamp - currentTimestamp);
					}
					catch (InterruptedException e)
					{
					}
					continueRunning = this.shouldRun;
					currentTimestamp = this.getTimestamp();
				}
				if (!continueRunning)
				{
					this.running = false;
					break;
				}
			}

			// updates the image, if required.
			boolean hasCandidate;
			int targetNumber;
			BufferedImage candidate;
			synchronized (this.imageLock)
			{
				hasCandidate = this.candidateImageNumber > this.currentImageNumber;
				targetNumber = this.candidateImageNumber;
				candidate = this.candidateImage;
			}

			if (hasCandidate)
			{
				this.updateImage(candidate, targetNumber, this.getTimestamp());
			}

			// calculates the next timestamp.
			currentTimestamp = this.getTimestamp();
			targetTimestamp += this.timeQuantum;
			while (targetTimestamp < currentTimestamp)
			{
				targetTimestamp += this.timeQuantum;
			}
		}
	}

	/**
	 * Updates the shared image.
	 *
	 * @param image The next shared image.
	 * @param imageNumber The next image number.
	 * @param timestamp The image timestamp.
	 */
	private void updateImage(BufferedImage image, int imageNumber, long timestamp)
	{
		if (image != null)
		{
			// sincroniza só na hora de atualizar de fato.
			if (this.currentImage == null || this.currentImage.getWidth() != image.getWidth() || this.currentImage.getHeight() != image.getHeight())
			{
				// não havia tela antes ou havia e o tamanho mudou.

				// determina quantos setores no eixo X e quantos setores no eixo Y.
				int tilesXAxis = IntegerArithmetics.divideAndRoundUp(image.getWidth(), this.tileWidth);
				int tilesYAxis = IntegerArithmetics.divideAndRoundUp(image.getHeight(), this.tileHeight);

				// recria a matriz de peças.
				TileData[][] newTiles = new TileData[tilesXAxis][tilesYAxis];
				for (int tileX = 0; tileX < tilesXAxis; tileX++)
				{
					for (int tileY = 0; tileY < tilesYAxis; tileY++)
					{
						// determina os cantos da peça atual.
						int x0 = tileX * this.tileWidth;
						int y0 = tileY * this.tileHeight;
						int x1 = Math.min(x0 + this.tileWidth, image.getWidth());
						int y1 = Math.min(y0 + this.tileHeight, image.getHeight());

						// instancia a peça.
						newTiles[tileX][tileY] = new TileData(x0, y0, x1, y1, timestamp);
					}
				}

				// substitui os dados antigos pelos novos.
				synchronized (this.imageLock)
				{
					this.imageIdentifier = ImageIdentifier.valueOf(String.valueOf(this.nextImageNumber++));
					this.currentImage = image;
					this.currentImageNumber = imageNumber;
					this.tiles = newTiles;
				}
			}
			else
			{
				int tilesXAxis = IntegerArithmetics.divideAndRoundUp(image.getWidth(), this.tileWidth);
				int tilesYAxis = IntegerArithmetics.divideAndRoundUp(image.getHeight(), this.tileHeight);

				// somente esta thread é escritora, então aqui é garantido que ler é seguro.
				RunOnceRunnable<CompareTileTask>[][] tasks = new RunOnceRunnable[tilesXAxis][tilesYAxis];
				for (int tileX = 0; tileX < tilesXAxis; tileX++)
				{
					for (int tileY = 0; tileY < tilesYAxis; tileY++)
					{
						tasks[tileX][tileY] = new RunOnceRunnable<CompareTileTask>(new CompareTileTask(this.tiles[tileX][tileY], this.currentImage, image));
					}
				}

				// TODO optimize this by running the tasks in multi-threaded workers.
				for (int tileX = 0; tileX < tilesXAxis; tileX++)
				{
					for (int tileY = 0; tileY < tilesYAxis; tileY++)
					{
						tasks[tileX][tileY].run();
					}
				}

				// wait until all tasks have finished.
				for (int tileX = 0; tileX < tilesXAxis; tileX++)
				{
					for (int tileY = 0; tileY < tilesYAxis; tileY++)
					{
						while (true)
						{
							try
							{
								tasks[tileX][tileY].join();
								break;
							}
							catch (InterruptedException e)
							{
							}
						}
					}
				}

				// apply the image changes.
				synchronized (this.imageLock)
				{
					int changedTileCount = 0;
					for (int tileX = 0; tileX < tilesXAxis; tileX++)
					{
						for (int tileY = 0; tileY < tilesYAxis; tileY++)
						{
							if (tasks[tileX][tileY].getAction().getComparisonResult().booleanValue() == false)
							{
								changedTileCount++;
								this.tiles[tileX][tileY].whenLastUpdated = timestamp;
							}
						}
					}
					if (changedTileCount > 0)
					{
						this.currentImage = image;
					}
					this.currentImageNumber = imageNumber;
				}
			}
		}
		else
		{
			synchronized (this.imageLock)
			{
				if (this.currentImage != null)
				{
					this.currentImage = null;
					this.imageIdentifier = null;
					this.tiles = null;
				}
				this.currentImageNumber = imageNumber;
			}
		}
	}

	private long getTimestamp()
	{
		return System.currentTimeMillis();
	}

	private static final class TileData
	{
		public final int x0;
		public final int x1;
		public final int y0;
		public final int y1;

		public volatile long whenLastUpdated;

		public TileData(int x0, int y0, int x1, int y1, long whenLastUpdated)
		{
			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
			this.whenLastUpdated = whenLastUpdated;
		}

	}

	private static final class CompareTileTask implements Runnable
	{
		private final TileData tile;
		private final BufferedImage image0;
		private final BufferedImage image1;

		private volatile Boolean comparisonResult;

		public CompareTileTask(TileData tile, BufferedImage image0, BufferedImage image1)
		{
			this.tile = tile;
			this.image0 = image0;
			this.image1 = image1;
		}

		public Boolean getComparisonResult()
		{
			return this.comparisonResult;
		}

		@Override
		public void run()
		{
			boolean result = false;
			try
			{
				result = this.doImagesMatch();
			}
			finally
			{
				this.comparisonResult = Boolean.valueOf(result);
			}
		}

		private boolean doImagesMatch()
		{
			for (int pixelY = this.tile.y0; pixelY < this.tile.y1; pixelY++)
			{
				for (int pixelX = this.tile.x0; pixelX < this.tile.x1; pixelX++)
				{
					if (this.image0.getRGB(pixelX, pixelY) != this.image1.getRGB(pixelX, pixelY))
					{
						return false;
					}
				}
			}
			return true;
		}
	}
}
