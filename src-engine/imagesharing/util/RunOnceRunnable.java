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
 * A {@linkplain Runnable} decorator which ensures the underlying {@link Runnable} is executed only once.
 *
 * @author Felipe Michels Fontoura
 */
public final class RunOnceRunnable<T extends Runnable> implements Runnable
{
	private static final int STARTED = 0x01;
	private static final int FINISHED = 0x02;

	private volatile int runOnceBits = 0;
	private final T action;

	public RunOnceRunnable(T action)
	{
		if (action == null)
		{
			throw new NullPointerException();
		}
		this.action = action;
	}

	public final T getAction()
	{
		return this.action;
	}

	@Override
	public final void run()
	{
		boolean mustRun = false;
		synchronized (this)
		{
			if (0 == (this.runOnceBits & STARTED))
			{
				this.runOnceBits = (this.runOnceBits | STARTED);
				mustRun = true;
			}
		}

		if (mustRun)
		{
			try
			{
				this.action.run();
			}
			finally
			{
				synchronized (this)
				{
					this.runOnceBits = this.runOnceBits | FINISHED;
					this.notifyAll();
				}
			}
		}
	}

	/**
	 * Waits until the underlying {@link Runnable} has been executed. If it has already been executed, this method returns immediately.
	 *
	 * @throws InterruptedException If the current thread is {@linkplain Thread#interrupt() interrupted}.
	 */
	public final void join() throws InterruptedException
	{
		while (true)
		{
			boolean mustBreak = false;

			synchronized (this)
			{
				if (0 != (this.runOnceBits & FINISHED))
				{
					mustBreak = true;
				}
				else
				{
					this.wait();
				}
			}

			if (mustBreak)
			{
				break;
			}
		}
	}
}
