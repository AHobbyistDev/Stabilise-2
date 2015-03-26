package com.stabilise.tests;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.util.concurrent.ConcurrentClearingQueue;
import com.stabilise.util.concurrent.SynchronizedClearingQueue;

@SuppressWarnings({ "unused", "deprecation" })
public class QueueTest {
	
	public static void main(String[] args) throws InterruptedException {
		final ClearingQueue<Integer> queue = new SynchronizedClearingQueue<>();
		final int numProducers = 2;
		final int intsPerProducer = 1024*512*8;
		final int numConsumers = 1;
		final AtomicInteger numConsumed = new AtomicInteger(0);
		final CountDownLatch startLatch = new CountDownLatch(numProducers + numConsumers);
		final CountDownLatch producerEnd = new CountDownLatch(numProducers);
		final CountDownLatch endLatch = new CountDownLatch(numProducers + numConsumers);
		
		for(int i = 0; i < numProducers; i++) {
			new Thread() {
				public void run() {
					Random rnd = new Random();
					startLatch.countDown();
					try {
						startLatch.await();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					for(int j = 0; j < intsPerProducer; j++)
						queue.add(rnd.nextInt());
					producerEnd.countDown();
					endLatch.countDown();
				}
			}.start();
		}
		
		for(int i = 0; i < numConsumers; i++) {
			new Thread() {
				public void run() {
					startLatch.countDown();
					try {
						startLatch.await();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					while(numConsumed.get() < numProducers*intsPerProducer) {
						int n = 0;
						for(Integer j : queue)
							n++;
						if(n == 0)
							continue;
						numConsumed.getAndAdd(n);
						//System.out.println("Added " + n + "/" + numConsumed.get());
					}
					endLatch.countDown();
				}
			}.start();
		}
		
		producerEnd.await();
		System.out.println("Producers complete");
		if(!endLatch.await(2, TimeUnit.SECONDS))
			System.out.println("Failed!");
		System.out.println(queue.size() + "/" + numConsumed.get());
		System.exit(0);;
	}
	
}
