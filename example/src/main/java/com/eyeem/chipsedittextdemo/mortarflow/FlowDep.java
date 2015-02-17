package com.eyeem.chipsedittextdemo.mortarflow;

import com.eyeem.chipsedittextdemo.App;
import com.squareup.picasso.Picasso;

/**
 * Created by vishna on 30/01/15.
 */
public interface FlowDep {
   App provideApp();
   Picasso providePicasso();
   FlowBundler provideFlowBundler();
}
