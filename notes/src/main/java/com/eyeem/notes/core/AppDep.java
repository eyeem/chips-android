package com.eyeem.notes.core;

import com.eyeem.notes.App;
import com.eyeem.notes.experimental.PausableThreadPoolExecutor;
import com.squareup.picasso.Picasso;

/**
 * Created by vishna on 17/02/15.
 */
public interface AppDep {
   App provideApp();
   Picasso providePicasso();
   PausableThreadPoolExecutor providePausableThreadPool();
}
