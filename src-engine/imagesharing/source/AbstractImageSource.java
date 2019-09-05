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

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * An agent which captures images from some source at a given interval and triggers callbacks for each capture.
 *
 * @author Felipe Michels Fontoura
 */
public abstract class AbstractImageSource implements ImageSource
{
	private final ArrayList<ImageCallback> callbacks = new ArrayList<ImageCallback>();

	public AbstractImageSource()
	{
	}

	@Override
	public abstract void activate();

	@Override
	public abstract void deactivate();

	@Override
	public void addImageCallback(ImageCallback callback)
	{
		synchronized (this.callbacks)
		{
			this.callbacks.add(callback);
		}
	}

	@Override
	public void removeImageCallback(ImageCallback callback)
	{
		synchronized (this.callbacks)
		{
			this.callbacks.remove(callback);
		}
	}

	protected void triggerCallbacks(BufferedImage image)
	{
		ImageCallback[] clonedCallbacks;
		synchronized (this.callbacks)
		{
			if (this.callbacks.isEmpty())
			{
				clonedCallbacks = null;
			}
			else
			{
				clonedCallbacks = this.callbacks.toArray(new ImageCallback[this.callbacks.size()]);
			}
		}
		if (clonedCallbacks != null)
		{
			for (int i = 0; i < clonedCallbacks.length; i++)
			{
				clonedCallbacks[i].handleImage(image);
				clonedCallbacks[i] = null;
			}
		}
	}
}
