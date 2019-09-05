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

/**
 * An image identifier is a string which uniquely identifies a shared image.
 * <p/>
 * This is just a wrapper around a string. It is meant to prevent standard strings from being wrongly used as image identifiers and the other way around.
 *
 * @author Felipe Michels Fontoura
 */
public final class ImageIdentifier
{
	public static ImageIdentifier valueOf(String value)
	{
		ImageIdentifier result = null;
		if (value != null)
		{
			result = new ImageIdentifier(value);
		}
		return result;
	}

	private final String value;

	public ImageIdentifier(String value)
	{
		if (value == null)
		{
			throw new NullPointerException("The wrapped string must not be null!");
		}
		this.value = value;
	}

	@Override
	public final String toString()
	{
		return this.value;
	}

	@Override
	public final int hashCode()
	{
		return this.value.hashCode();
	}

	@Override
	public final boolean equals(Object obj)
	{
		boolean result;
		if (obj == null)
		{
			result = false;
		}
		else if (obj == this)
		{
			result = true;
		}
		else if (obj instanceof ImageIdentifier)
		{
			result = ((ImageIdentifier) obj).value.equals(this.value);
		}
		else
		{
			result = false;
		}
		return result;
	}
}
