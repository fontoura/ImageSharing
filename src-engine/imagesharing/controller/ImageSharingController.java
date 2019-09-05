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

import java.util.List;

/**
 * An image sharing controller is an object which shares identified images with a remote image.
 * <p/>
 * The images may change as time passes. The controller provides functions which allow another image to keep those images in synch.
 * <p/>
 * In order to do so, the controller splits each image is split in rectangular tiles. The controller keeps track of which tiles have changed each time an image is updated, allowing it to provide external modules with tile by tile updates.
 *
 * @author Felipe Michels Fontoura
 */
public interface ImageSharingController
{
	void start();

	void stop();

	/**
	 * Gets a report describing all tiles that have changes on a given image since a given instant
	 * <p/>
	 * If the image identifier does not correspond to a valid shared image, this method returns {@code null}.
	 * <p/>
	 * The implementation of this method must be thread safe.
	 *
	 * @param imageIdentifier The image identifier.
	 * @param instant The instant in controller time units (usually milliseconds since the controller started).
	 * @return A report containing all changes or {@code null}.
	 * @throws NullPointerException If the image identifier is {@code null}.
	 */
	public ImageChangesReport generateImageChangesReport(ImageIdentifier imageIdentifier, long instant);

	/**
	 * Gets the image corresponding of a single tile of a shared image.
	 * <p/>
	 * If the image identifier does not correspond to a shared image this method returns {@code null}.
	 * <p/>
	 * If the tile is out of bounds, this method throws an {@link IndexOutOfBoundsException}.
	 * <p/>
	 * The implementation of this method must be thread safe.
	 *
	 * @param imageIdentifier The image identifier.
	 * @param tilePosition The tile position.
	 * @return The tile image or {@code null}.
	 * @throws NullPointerException If the image identifier or the tile position are {@code null}.
	 * @throws IndexOutOfBoundsException If the tile position is out of the image boundaries.
	 */
	public TileImage getTileImage(ImageIdentifier imageIdentifier, TilePosition tilePosition);

	/**
	 * Gets a list containing the identifiers of all available shared images.
	 * <p/>
	 * The returned list is read-only and guaranteed not to change after this method has been called.
	 * <p/>
	 * The implementation of this method must be thread safe.
	 *
	 * @return A list containing the identifiers of all available shared images.
	 */
	public List<ImageIdentifier> getImageIdentifiers();

	/**
	 * Gets an object describing a shared image.
	 * <p/>
	 * If the image identifier does not correspond to a shared image this method returns {@code null}.
	 * <p/>
	 * The implementation of this method must be thread safe.
	 *
	 * @param imageIdentifier The image identifier.
	 * @return The image description.
	 * @throws NullPointerException If the image identifier is {@code null}.
	 */
	public ImageDescription getImageDescription(ImageIdentifier imageIdentifier);
}