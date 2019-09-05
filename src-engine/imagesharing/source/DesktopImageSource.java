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

package imagesharing.source;

import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * An agent which captures desktop screenshots at a given interval and triggers callbacks for each capture.
 *
 * @author Felipe Michels Fontoura
 */
public class DesktopImageSource extends AbstractImageSource
{
	private final long timeQuantum = 100;

	private volatile boolean shouldActivate = false;

	private volatile boolean shouldRun = false;
	private volatile boolean running = false;

	private GraphicsDevice sourceDevice;

	public DesktopImageSource()
	{
		try
		{
			GraphicsDevice bestDevice = null;
			int areaOfBestDevice = 0;
			int xOffOfBestDevice = 0;
			int xOff = 0;
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			for (GraphicsDevice curGs : gs)
			{
				DisplayMode dm = curGs.getDisplayMode();
				int area = dm.getWidth() * dm.getHeight();
				if (bestDevice == null || area < areaOfBestDevice)
				{
					bestDevice = curGs;
					areaOfBestDevice = area;
					xOffOfBestDevice = xOff;
				}
				xOff += dm.getWidth();
			}
			this.sourceDevice = bestDevice;
		}
		catch (Exception e)
		{

		}
	}

	@Override
	public void activate()
	{
		this.shouldActivate = true;
		this.updateRunningStatus();
	}

	@Override
	public void deactivate()
	{
		this.shouldActivate = false;
		this.updateRunningStatus();
	}

	public GraphicsDevice getSourceDevice()
	{
		return this.sourceDevice;
	}

	public void setSourceDevice(GraphicsDevice value)
	{
		this.sourceDevice = value;
	}

	private void run()
	{
		Robot robot = null;
		GraphicsDevice currentDevice = null;

		// polls the screen for changes.
		long targetTimestamp = this.timeQuantum * (System.currentTimeMillis() / this.timeQuantum);
		while (true)
		{
			// wait until the next timestamp.
			long currentTimestamp = System.currentTimeMillis();
			synchronized (this)
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
						this.wait(targetTimestamp - currentTimestamp);
					}
					catch (InterruptedException e)
					{
					}
					continueRunning = this.shouldRun;
					currentTimestamp = System.currentTimeMillis();
				}
				if (!continueRunning)
				{
					this.running = false;
					break;
				}
			}

			GraphicsDevice desiredDevice;
			synchronized (this)
			{
				desiredDevice = this.sourceDevice;
			}
			if (robot == null || desiredDevice != currentDevice)
			{
				currentDevice = null;
				robot = null;

				if (desiredDevice != null)
				{
					try
					{
						robot = new Robot(desiredDevice);
						currentDevice = desiredDevice;
					}
					catch (AWTException e)
					{
					}
				}
			}

			if (robot != null)
			{
				// AWT doesn't provide a direct way to get the unscaled boundaries of a desktop screen.
				// it provides a direct way to get the scaled boundaries of a desktop screen...
				// and it provides a direct way to get the unscaled width and height...
				// but it doesn't provide a direct way to get the unscaled offsets of the desktop screen...
				// and we need the unscaled boundaries (offsets and dimensions) in order to get a screen capture!

				// in order to get the unscaled boundaries, we're first guessing the scaling factor.
				// we can do that because we have the unscaled and scaled dimensions of the desktop screen.
				// with the scaling factor, we are able to calculate the unscaled offsets of the destkop screen!

				Rectangle scaledBoundaries = currentDevice.getDefaultConfiguration().getBounds();
				DisplayMode displayMode = currentDevice.getDisplayMode();

				int width = displayMode.getWidth();
				int height = displayMode.getHeight();

				int offsetX = (scaledBoundaries.x * width) / scaledBoundaries.width;
				int offsetY = (scaledBoundaries.y * height) / scaledBoundaries.height;

				Rectangle screenRectangle = new Rectangle(offsetX, offsetY, width, height);

				BufferedImage screenImage = robot.createScreenCapture(screenRectangle);

				// trigger the callbacks.
				this.triggerCallbacks(screenImage);
			}

			// calculate the time of the next capture.
			currentTimestamp = System.currentTimeMillis();
			targetTimestamp += this.timeQuantum;
			while (targetTimestamp < currentTimestamp)
			{
				targetTimestamp += this.timeQuantum;
			}
		}
	}

	private void updateRunningStatus()
	{
		boolean startNow = false;

		boolean shouldRun = this.shouldActivate && this.sourceDevice != null;
		synchronized (this)
		{
			if (shouldRun != this.shouldRun)
			{
				this.shouldRun = shouldRun;
				if (shouldRun)
				{
					if (!this.running)
					{
						this.running = true;
						startNow = true;
					}
				}
				else
				{
					if (this.running)
					{
						this.notifyAll();
					}
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
					DesktopImageSource.this.run();
				}
			});
			thread.start();
		}
	}
}
