package com.eyeem.chipsedittextdemo.mortarflow;

import com.squareup.picasso.Picasso;

/**
 * Created by vishna on 30/01/15.
 */
public interface FlowDep {
   Picasso providePicasso();
   FlowBundler provideFlowBundler();
}
