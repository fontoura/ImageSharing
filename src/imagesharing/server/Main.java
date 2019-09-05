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

import imagesharing.controller.BufferedImageSharingController;

public class Main
{
	public static void main(String[] args)
	{
		BufferedImageSharingController controller = null;
		ScreenSharingServerUI ui = null;

		boolean ok = false;
		try
		{
			// start the screen sharing controller.
			controller = new BufferedImageSharingController();
			controller.start();

			// start the screen sharing UI.
			ui = new ScreenSharingServerUI(controller);
			ui.activate();

			// start the screen sharing server.
			SingleScreenSharingServer server = new SingleScreenSharingServer(controller);
			ok = server.start();
		}
		finally
		{
			if (!ok)
			{
				if (controller != null)
				{
					controller.stop();
				}
				if (ui != null)
				{
					ui.deactivate();
				}
			}
		}
	}
}
