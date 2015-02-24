package com.eyeem.notes.event;

import android.text.SpannableString;

import lombok.Getter;

/**
 * Created by vishna on 19/02/15.
 */
public class TextSnapshotCaptured {
   @Getter SpannableString snapshot;

   public TextSnapshotCaptured(SpannableString snapshot) {
      this.snapshot = snapshot;
   }
}
