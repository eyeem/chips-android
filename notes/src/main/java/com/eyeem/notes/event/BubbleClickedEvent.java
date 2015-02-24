package com.eyeem.notes.event;

import android.view.View;

import com.eyeem.chips.BubbleSpan;

/**
 * Created by vishna on 18/02/15.
 */
public class BubbleClickedEvent {
   public View view;
   public BubbleSpan bubbleSpan;

   public BubbleClickedEvent(View view, BubbleSpan bubbleSpan) {
      this.view = view;
      this.bubbleSpan = bubbleSpan;
   }
}
