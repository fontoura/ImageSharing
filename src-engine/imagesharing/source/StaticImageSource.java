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

public class StaticImageSource extends AbstractImageSource
{
	private BufferedImage image;
	private boolean active = false;

	public BufferedImage getImage()
	{
		return this.image;
	}

	public void setImage(BufferedImage image)
	{
		this.image = image;
		if (this.active && image != null)
		{
			this.triggerCallbacks(image);
		}
	}

	@Override
	public void activate()
	{
		if (!this.active)
		{
			this.active = true;
			if (this.image != null)
			{
				this.triggerCallbacks(this.image);
			}
		}
	}

	@Override
	public void deactivate()
	{
		if (this.active)
		{
			this.active = false;
		}
	}
}
