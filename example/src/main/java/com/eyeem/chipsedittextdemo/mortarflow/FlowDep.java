package com.eyeem.chipsedittextdemo.mortarflow;

import com.eyeem.chipsedittextdemo.App;
import com.eyeem.chipsedittextdemo.experimental.PausableThreadPoolExecutor;
import com.squareup.picasso.Picasso;

/**
 * Created by vishna on 30/01/15.
 */
public interface FlowDep {
   FlowBundler provideFlowBundler();
//   App provideApp();
//   Picasso providePicasso();
//   PausableThreadPoolExecutor providePausableThreadPool();
}
