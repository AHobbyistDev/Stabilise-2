package com.stabilise.tests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Try to find the lowest number x such that 
 */
public class Collatz {
	
	// ---------------------------------------------
	
	private static final int N_CACHED = 2048;
	private static final int MASK = N_CACHED - 1; // N_CACHED must be a power of 2
	private static final long[] CACHED_RESULTS;
	
	static {
		CACHED_RESULTS = new long[N_CACHED];
		for(int i = 0; i < N_CACHED; i++)
			CACHED_RESULTS[i] = new Collatz(i).work();
	}
	
	// ---------------------------------------------
	
	final long init;
	long result;
	long iterations;
	
	public Collatz(long x) {
		init = x;
	}
	
	public long work() {
		long x = init;
		
		// f(x)
		// = x/2       if x is even
		// = 3x + 1    if x is odd
		
		int i = 0;
		while(x > 1) {
			i++;
			x = stepX(x);
		}
		result = x;
		return iterations = i;
	}
	
	protected long stepX(long x) {
		if((x & 1) == 0) // is is even
			return x >> 1;     // x /= 2
		else
			return 3*x + 1;
	}
	
	public long iterations() {
		return iterations;
	}
	
	public long result() {
		return result;
	}
	
	@Override
	public String toString() {
		return init + " -> " + result + ": " + iterations + " iterations";
	}
	
	public void print() {
		System.out.println(toString());
	}
	
	public static class Collatz2 extends Collatz {
		public Collatz2(long x) {
			super(x);
		}
		@Override
		public long work() {
			long x = init;
			
			int i = 0;
			while(x > 1) {
				if(x < N_CACHED) {
					result = 1;
					return iterations = i + CACHED_RESULTS[(int)(x & MASK)];
				}
				i++;
				x = stepX(x);
			}
			result = x;
			return iterations = i;
		}
	}
	
	// -------------------------
	
	public static void main(String[] args) {
		//reverseCollatz(70);
		
		//for(int i = 0; i < 64; i++)
		//	new Collatz((1L << i) - 1).print();
		
		bruteForce(new BruteForcer(5));
	}
	
	public static void bruteForce(BruteForcer f) {
		f.start();
		f.await();
		f.getResult();
	}
	
	public static void bruteForce(BruteForcer f, long timeout, TimeUnit unit) {
		f.start();
		f.await(timeout, unit);
		if(!f.getResult())
			f.stop();
	}
	
	public static class BruteForcer {
		
		final long lowerBound;
		final int target;
		volatile boolean completed = false;
		volatile long result = 0;
		final int numThreads;
		Worker[] threads;
		CountDownLatch latch;
		
		public BruteForcer(int targetIterations) {
			this(targetIterations, 0L);
		}
		
		public BruteForcer(int targetIterations, long lowerBound) {
			this.target = targetIterations;
			this.lowerBound = lowerBound;
			
			numThreads = Runtime.getRuntime().availableProcessors();
		}
		
		public void start() {
			if(threads != null)
				throw new IllegalStateException("Already started");
			
			threads = new Worker[numThreads];
			latch = new CountDownLatch(numThreads);
			
			for(int i = 0; i < numThreads; i++) {
				threads[i] = new Worker(i);
				threads[i].start();
			}
		}
		
		private class Worker extends Thread {
			
			private final long id;
			
			private Worker(long threadID) {
				this.id = threadID;
			}
			
			public void run() {
				int l = 0;
				for(long i = id + lowerBound; !completed || i < result; i += numThreads) {
					// Sleep for 50ms every million cycles
					if(l++ == 1000000) {
						l = 0;
						try {
							Thread.sleep(50L);
						} catch(InterruptedException e) {}
					}
					if(new Collatz2(i).work() == target) {
						synchronized(Worker.class) {
							if(result == 0L || i < result)
								result = i;
							completed = true;
							break;
						}
					}
				}
				System.out.println("Thread " + id + " done");
				latch.countDown();
			}
			
		}
		
		public void stop() {
			completed = true;
			joinThreads();
		}
		
		public void await() {
			try {
				latch.await();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			joinThreads();
		}
		
		public void await(long time, TimeUnit unit) {
			try {
				latch.await(time, unit);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			joinThreads();
		}
		
		private void joinThreads() {
			for(Thread t : threads) {
				try {
					t.join();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private boolean getResult() {
			if(completed)
				System.out.println(target + " -> " + result);
			else
				System.out.println("NOT COMPLETED");
			return completed;
		}
		
	}
	
	@SuppressWarnings("unused")
	private static void reverseCollatz(int numIterations) {
		long x = 1, t;
		int i = 0;
		while(i < numIterations) {
			i++;
			// Try the odd route first...
			if((t = (x - 1)) % 3 == 0 && ((t /= 3) & 1) == 1 && t > 1)
				x = t;
			// Try the even route
			else
				x <<= 1;
			System.out.println(i + ": " + x);
		}
		System.out.println("An upper bound for " + numIterations + " iterations is " + x);
	}

}
