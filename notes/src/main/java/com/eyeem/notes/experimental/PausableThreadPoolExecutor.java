package com.eyeem.notes.experimental;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vishna on 06/02/15.
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {
   private boolean isPaused;
   private ReentrantLock pauseLock = new ReentrantLock();
   private Condition unpaused = pauseLock.newCondition();

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
   }

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
   }

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
   }

   public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
   }

   protected void beforeExecute(Thread t, Runnable r) {
      super.beforeExecute(t, r);
      pauseLock.lock();
      try {
         while (isPaused) unpaused.await();
      } catch (InterruptedException ie) {
         t.interrupt();
      } finally {
         pauseLock.unlock();
      }
   }

   public void pause() {
      pauseLock.lock();
      try {
         isPaused = true;
      } finally {
         pauseLock.unlock();
      }
   }

   public void resume() {
      pauseLock.lock();
      try {
         isPaused = false;
         unpaused.signalAll();
      } finally {
         pauseLock.unlock();
      }
   }
}
