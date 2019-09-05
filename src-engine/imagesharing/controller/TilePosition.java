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
 * The position of a tile (piece of the image) from the upper-left corner.
 * <p/>
 * A tile position uniquely identifies a tile on image and should be regarded as the tile identifier for all practical purposes.
 *
 * @author Felipe Michels Fontoura
 */
public final class TilePosition
{
	private final int x;
	private final int y;

	/**
	 * Creates a tile position.
	 *
	 * @param x The horizontal position of the tile.
	 * @param y The vertical position of the tile.
	 */
	public TilePosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Gets the horizontal position of the tile.
	 * <p/>
	 * A value of zero means the first tile from the left.
	 *
	 * @return The horizontal position of the tile.
	 */
	public final int getX()
	{
		return this.x;
	}

	/**
	 * Gets the vertical position of the tile.
	 * <p/>
	 * A value of zero means the first tile from the top.
	 *
	 * @return The vertical position of the tile.
	 */
	public final int getY()
	{
		return this.y;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode()
	{
		return this.x | (this.y << 16);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj)
	{
		if (obj instanceof TilePosition)
		{
			TilePosition other = (TilePosition) obj;
			return other.x == this.x && other.y == this.y;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString()
	{
		return "(" + this.x + ", " + this.y + ")";
	}

	/**
	 * Creates a tile position.
	 *
	 * @param x The horizontal position of the tile.
	 * @param y The vertical position of the tile.
	 * @return The tile position.
	 */
	public static final TilePosition valueOf(int x, int y)
	{
		return new TilePosition(x, y);
	}
}
