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

import java.util.ArrayList;

public class ProxyImageSource implements ImageSource
{
	private ImageSource imageSource = null;
	private boolean active = false;

	private final ArrayList<ImageCallback> callbacks = new ArrayList<ImageCallback>();

	public ImageSource getUnderlyingImageSource()
	{
		return this.imageSource;
	}

	public void setUnderlyingImageSource(ImageSource imageSource)
	{
		synchronized (this)
		{
			ImageSource oldImageSource = this.imageSource;
			if (oldImageSource != imageSource)
			{
				this.imageSource = imageSource;

				if (oldImageSource != null)
				{
					oldImageSource.deactivate();
				}

				for (ImageCallback callback : this.callbacks)
				{
					if (oldImageSource != null)
					{
						oldImageSource.removeImageCallback(callback);
					}
					if (imageSource != null)
					{
						imageSource.addImageCallback(callback);
					}
				}

				if (imageSource != null)
				{
					if (this.active)
					{
						imageSource.activate();
					}
					else
					{
						imageSource.deactivate();
					}
				}
			}
		}
	}

	@Override
	public void activate()
	{
		synchronized (this)
		{
			this.active = true;
			if (this.imageSource != null)
			{
				this.imageSource.activate();
			}
		}
	}

	@Override
	public void deactivate()
	{
		synchronized (this)
		{
			this.active = false;
			if (this.imageSource != null)
			{
				this.imageSource.deactivate();
			}
		}
	}

	@Override
	public void addImageCallback(ImageCallback callback)
	{
		synchronized (this)
		{
			this.callbacks.add(callback);
			if (this.imageSource != null)
			{
				this.imageSource.addImageCallback(callback);
			}
		}
	}

	@Override
	public void removeImageCallback(ImageCallback callback)
	{
		synchronized (this)
		{
			if (this.callbacks.remove(callback))
			{
				if (this.imageSource != null)
				{
					this.imageSource.removeImageCallback(callback);
				}
			}
		}
	}
}
