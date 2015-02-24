package com.eyeem.notes.core;

import com.eyeem.notes.App;
import com.eyeem.notes.experimental.PausableThreadPoolExecutor;
import com.eyeem.notes.mortarflow.ScopeSingleton;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vishna on 17/02/15.
 */
@Module
public class PausableExecutorModule {
   private final static PausableThreadPoolExecutor sPausableExecutor;
   // Sets the amount of time an idle thread waits before terminating
   private static final int NUMBER_OF_CORES = 2;
   // Sets the amount of time an idle thread waits before terminating
   private static final int KEEP_ALIVE_TIME = 5;
   // Sets the Time Unit to seconds
   private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

   @Provides @ScopeSingleton(App.Component.class) PausableThreadPoolExecutor provideExecutor() {
      return sPausableExecutor;
   }

   static {
      sPausableExecutor = new PausableThreadPoolExecutor(
         NUMBER_OF_CORES,       // Initial pool size
         NUMBER_OF_CORES,       // Max pool size
         KEEP_ALIVE_TIME,
         KEEP_ALIVE_TIME_UNIT,
         new LinkedBlockingQueue<Runnable>());
   }
}
