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
 * A {@link ImageSharingController} which shares a single image at a time.
 *
 * @author Felipe Michels Fontoura
 */
public interface SingleImageSharingController extends ImageSharingController
{
	/**
	 * Gets the shared image identifier.
	 * <p/>
	 * If no image is currently being shared this method returns {@code null}, otherwise it returns the same as {@link #getImageIdentifiers() getImageIdentifiers}{@link java.util.List#get(int) .get(0)}.
	 *
	 * @return The shared image identifier or {@code null}.
	 */
	ImageIdentifier getImageIdentifier();

	/**
	 * Gets the object describing the shared image image.
	 * <p/>
	 * If no image is currently being shared this method returns {@code null}, otherwise it returns the same as {@link #getImageDescription(ImageIdentifier) getImageDescription}{@link #getImageIdentifier() (getImageIdentifier())}
	 *
	 * @return The object describing the shared image image or {@code null}.
	 */
	ImageDescription getImageDescription();

}