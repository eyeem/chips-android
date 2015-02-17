package com.eyeem.chipsedittextdemo.core;

import com.eyeem.chipsedittextdemo.App;
import com.eyeem.chipsedittextdemo.experimental.PausableThreadPoolExecutor;
import com.squareup.picasso.Picasso;

/**
 * Created by vishna on 17/02/15.
 */
public interface AppDep {
   App provideApp();
   Picasso providePicasso();
   PausableThreadPoolExecutor providePausableThreadPool();
}
