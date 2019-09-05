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

import imagesharing.controller.BufferedImageSharingController;
import imagesharing.source.DesktopImageSource;
import imagesharing.source.ImageCallback;
import imagesharing.source.ImageSource;
import imagesharing.source.ProxyImageSource;

public class ScreenSharingServerUI
{
	private BufferedImageSharingController controller;
	private ProxyImageSource imageSource;

	private boolean enabled = false;
	private ImageSource chosenImageSource;

	private DesktopImageSource desktopImageSource;

	public ScreenSharingServerUI(BufferedImageSharingController controller)
	{
		this.controller = controller;
		this.imageSource = new ProxyImageSource();
		this.imageSource.addImageCallback(new ImageCallback()
		{
			@Override
			public void handleImage(BufferedImage image)
			{
				controller.setImage(image);
			}
		});
		this.desktopImageSource = new DesktopImageSource();

		ServerControlWindow window = new ServerControlWindow();
		window.setUI(this);
		window.setVisible(true);
	}

	public void resumeCapture()
	{
		if (!this.enabled)
		{
			this.enabled = true;
			this.imageSource.setUnderlyingImageSource(this.chosenImageSource);
		}
	}

	public void pauseCapture()
	{
		if (this.enabled)
		{
			this.enabled = false;
			this.imageSource.setUnderlyingImageSource(null);
		}
	}

	public void activate()
	{
		this.imageSource.activate();
		this.resumeCapture();
	}

	public void deactivate()
	{
		this.imageSource.deactivate();
	}

	public ImageSource getChosenImageSource()
	{
		return this.chosenImageSource;
	}

	public void setChosenImageSource(ImageSource value)
	{
		this.chosenImageSource = value;
		if (this.enabled)
		{
			this.imageSource.setUnderlyingImageSource(value);
		}
	}

	public DesktopImageSource getDesktopImageSource()
	{
		return this.desktopImageSource;
	}

	public void setDesktopImageSource(DesktopImageSource value)
	{
		this.desktopImageSource = value;
	}
}
