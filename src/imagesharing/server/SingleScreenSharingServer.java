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

package imagesharing.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.nikkii.embedhttp.HttpServer;
import org.nikkii.embedhttp.handler.HttpRequestHandler;
import org.nikkii.embedhttp.impl.HttpMethod;
import org.nikkii.embedhttp.impl.HttpRequest;
import org.nikkii.embedhttp.impl.HttpResponse;
import org.nikkii.embedhttp.impl.HttpStatus;

import com.migcomponents.migbase64.Base64;

import imagesharing.controller.ImageChangesReport;
import imagesharing.controller.ImageDescription;
import imagesharing.controller.ImageIdentifier;
import imagesharing.controller.SingleImageSharingController;
import imagesharing.controller.TileImage;
import imagesharing.controller.TilePosition;
import imagesharing.util.JSON;

public class SingleScreenSharingServer
{
	private SingleImageSharingController controller;

	private ImageIdentifier cachedScreenIdentifier;
	private HashMap<TilePosition, CachedTileImage> cache = new HashMap<TilePosition, CachedTileImage>();

	private static class CachedTileImage
	{
		public long tileTimestamp;
		public String base64Image;

		public CachedTileImage(long timestamp, String base64)
		{
			this.tileTimestamp = timestamp;
			this.base64Image = base64;
		}

		public void update(long timestamp, String base64)
		{
			this.tileTimestamp = timestamp;
			this.base64Image = base64;
		}
	}

	public SingleScreenSharingServer(SingleImageSharingController controller)
	{
		this.controller = controller;
	}

	public boolean start()
	{
		// instancia o servidor HTTP.
		HttpServer server = new HttpServer();
		server.addRequestHandler(new HttpRequestHandler()
		{
			@Override
			public HttpResponse handleRequest(HttpRequest request)
			{
				return SingleScreenSharingServer.this.handleRequest(request);
			}
		});

		// tenta iniciar o servidor HTTP (em uma thread separada).
		boolean okay = false;
		try
		{
			server.bind(7666);
			server.start();
			okay = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (!okay)
			{
				// se caiu aqui, é porque o servidor não iniciou corretamente.
				// interrompe o server por garantia (não deve precisar...)
				server.stop();
			}
		}

		return okay;
	}

	protected HttpResponse handleRequest(HttpRequest request)
	{
		// router "manual" de HTTP.
		if (request.getMethod() == HttpMethod.GET)
		{
			if (request.getUri().equals("/"))
			{
				return this.handleStaticRequest("index.html", "text/html; charset=utf-8");
			}
			else if (request.getUri().equals("/jquery.js"))
			{
				return this.handleStaticRequest("jquery.js", "application/javascript; charset=utf-8");
			}
		}
		else if (request.getMethod() == HttpMethod.POST)
		{
			if (request.getUri().equals("/ajax/getInformation"))
			{
				return this.handleGetInformationRequest(request.getData());
			}
			else if (request.getUri().equals("/ajax/getTile"))
			{
				return this.handleGetTileImageRequest(request.getData());
			}
			else if (request.getUri().equals("/ajax/getDelta"))
			{
				return this.handleGetDeltaRequest(request.getData());
			}
		}
		return null;
	}

	private HttpResponse handleGetInformationRequest(String jsonPayloadOfRequest)
	{
		try
		{
			ImageDescription imageDescription;
			synchronized (this)
			{
				imageDescription = this.controller.getImageDescription();
				if (!Objects.equals(imageDescription != null ? imageDescription.imageIdentifier : null, this.cachedScreenIdentifier))
				{
					this.cachedScreenIdentifier = imageDescription.imageIdentifier;
					this.cache.clear();
				}
			}
			if (imageDescription != null)
			{
				StringBuilder jsonPayloadOfResponse = new StringBuilder();
				jsonPayloadOfResponse.append('{');
				jsonPayloadOfResponse.append("\"type\":\"SUCCESS\"");
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"image_id\":\"").append(imageDescription.imageIdentifier.toString());
				jsonPayloadOfResponse.append("\",");
				jsonPayloadOfResponse.append("\"image_width\":").append(imageDescription.width);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"image_height\":").append(imageDescription.height);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"tile_width\":").append(imageDescription.tileWidth);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"tile_height\":").append(imageDescription.tileHeight);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"tile_timestamp\":").append(this.stringifyTimestamp(imageDescription.mostRecentTileTimestamp));
				jsonPayloadOfResponse.append('}');

				byte[] bytes = jsonPayloadOfResponse.toString().getBytes("UTF-8");

				HttpResponse response = new HttpResponse(HttpStatus.OK, bytes);
				response.addHeader("Content-Type", "application/json; charset=utf-8");
				return response;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR!".getBytes());
		return response;
	}

	@SuppressWarnings( "unchecked" )
	private HttpResponse handleGetTileImageRequest(String jsonData)
	{
		try
		{
			Map<String, Object> payload = (Map<String, Object>) JSON.parse(jsonData);

			String imageId = ((String) payload.get("image_id"));
			int tileX = ((Number) payload.get("tile_x")).intValue();
			int tileY = ((Number) payload.get("tile_y")).intValue();

			TilePosition tilePosition = TilePosition.valueOf(tileX, tileY);

			TileImage genericInformation;
			String imageBase64;
			synchronized (this)
			{
				genericInformation = this.controller.getTileImage(ImageIdentifier.valueOf(imageId), tilePosition);
				if (genericInformation != null)
				{
					CachedTileImage cachedImage = this.cache.get(tilePosition);
					if (cachedImage != null && cachedImage.tileTimestamp == genericInformation.getInstant())
					{
						imageBase64 = cachedImage.base64Image;
					}
					else
					{
						imageBase64 = this.encodeBase64Image(genericInformation.image.getSubimage(genericInformation.x0, genericInformation.y0, genericInformation.x1 - genericInformation.x0, genericInformation.y1 - genericInformation.y0));
						if (cachedImage != null)
						{
							cachedImage.update(genericInformation.getInstant(), imageBase64);
						}
						else
						{
							cachedImage = new CachedTileImage(genericInformation.getInstant(), imageBase64);
							this.cache.put(tilePosition, cachedImage);
						}
					}
				}
				else
				{
					imageBase64 = null;
				}
			}
			if (genericInformation != null)
			{
				StringBuilder jsonPayloadOfResponse = new StringBuilder();
				jsonPayloadOfResponse.append('{');
				jsonPayloadOfResponse.append("\"type\":\"SUCCESS\"");
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"x0\":").append(genericInformation.x0);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"y0\":").append(genericInformation.y0);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"x1\":").append(genericInformation.x1);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"y1\":").append(genericInformation.y1);
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"tile_timestamp\":").append(this.stringifyTimestamp(genericInformation.getInstant()));
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"image\":\"").append(imageBase64).append('"');
				jsonPayloadOfResponse.append('}');

				byte[] bytes = jsonPayloadOfResponse.toString().getBytes("UTF-8");

				HttpResponse response = new HttpResponse(HttpStatus.OK, bytes);
				response.addHeader("Content-Type", "application/json; charset=utf-8");
				return response;
			}
			else
			{
				byte[] bytes = "{\"type\":\"SCREEN_LAYOUT_CHANGED\"}".getBytes("UTF-8");

				HttpResponse response = new HttpResponse(HttpStatus.OK, bytes);
				response.addHeader("Content-Type", "application/json; charset=utf-8");
				return response;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR!".getBytes());
		return response;
	}

	@SuppressWarnings( "unchecked" )
	private HttpResponse handleGetDeltaRequest(String jsonData)
	{
		try
		{
			Map<String, Object> payload = (Map<String, Object>) JSON.parse(jsonData);

			String imageId = ((String) payload.get("image_id"));
			long timestamp = this.parseTimestamp(payload.get("tile_timestamp"));

			ImageChangesReport changesReport = this.controller.generateImageChangesReport(ImageIdentifier.valueOf(imageId), timestamp);
			if (changesReport != null)
			{
				StringBuilder jsonPayloadOfResponse = new StringBuilder();
				jsonPayloadOfResponse.append('{');
				jsonPayloadOfResponse.append("\"type\":\"SUCCESS\"");
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"tile_timestamp\":").append(this.stringifyTimestamp(changesReport.getMaxTileTimestamp()));
				jsonPayloadOfResponse.append(',');
				jsonPayloadOfResponse.append("\"tiles\":[");
				int tileIndex = 0;
				for (TilePosition tilePosition : changesReport.getChangedTiles())
				{
					try
					{
						if (tileIndex > 0)
						{
							jsonPayloadOfResponse.append(',');
						}
						jsonPayloadOfResponse.append("{\"x\":").append(tilePosition.getX()).append(",\"y\":").append(tilePosition.getY()).append('}');
					}
					finally
					{
						tileIndex++;
					}
				}
				jsonPayloadOfResponse.append(']');
				jsonPayloadOfResponse.append('}');

				byte[] bytes = jsonPayloadOfResponse.toString().getBytes("UTF-8");

				HttpResponse response = new HttpResponse(HttpStatus.OK, bytes);
				response.addHeader("Content-Type", "application/json; charset=utf-8");
				return response;
			}
			else
			{
				byte[] bytes = "{\"type\":\"SCREEN_LAYOUT_CHANGED\"}".getBytes("UTF-8");

				HttpResponse response = new HttpResponse(HttpStatus.OK, bytes);
				response.addHeader("Content-Type", "application/json; charset=utf-8");
				return response;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR!".getBytes());
		return response;
	}

	private String encodeBase64Image(BufferedImage image) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ImageIO.write(image, "png", out);

		return Base64.encodeToString(out.toByteArray(), false);
	}

	private HttpResponse handleStaticRequest(String pageName, String mimeType)
	{
		InputStream in = this.getClass().getResourceAsStream("pages/" + pageName);
		if (in != null)
		{
			try
			{
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int nRead;
				byte[] data = new byte[16384];

				while ((nRead = in.read(data, 0, data.length)) != -1)
				{
					buffer.write(data, 0, nRead);
				}

				buffer.flush();

				byte[] bytes = buffer.toByteArray();

				HttpResponse response = new HttpResponse(HttpStatus.OK, bytes);
				if (mimeType != null)
				{
					response.addHeader("Content-Type", mimeType);
				}
				return response;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (Throwable t)
				{
				}
			}
		}
		HttpResponse response = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR!".getBytes());
		return response;
	}

	private long parseTimestamp(Object value)
	{
		return ((Number) value).longValue();
	}

	private String stringifyTimestamp(long timestamp)
	{
		return Long.toString(timestamp);
	}
}
