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

package imagesharing.util;

/**
 * Class with static utility methods for handling integer arithmetics.
 *
 * @author Felipe Michels Fontoura
 */
public class IntegerArithmetics
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private IntegerArithmetics()
	{
	}

	/**
	 * The method divides a long integer by another and returns the rounded-up result.
	 * <p/>
	 * This method assumes both parameters are positive numbers. It does not check for parameter sanity.
	 *
	 * @param num The dividend.
	 * @param divisor The divisor.
	 * @return The rounded-up result of the division.
	 */
	public static long divideAndRoundUp(long num, long divisor)
	{

		return (num + divisor - 1) / divisor;
	}

	/**
	 * The method divides an integer by another and returns the rounded-up result.
	 * <p/>
	 * This method assumes both parameters are positive numbers. It does not check for parameter sanity.
	 *
	 * @param num The dividend.
	 * @param divisor The divisor.
	 * @return The rounded-up result of the division.
	 */
	public static int divideAndRoundUp(int num, int divisor)
	{
		return (num + divisor - 1) / divisor;
	}
}
