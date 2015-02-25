package com.eyeem.notes.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.eyeem.chips.BubbleSpan;
import com.eyeem.chips.ChipsTextView;
import com.eyeem.notes.R;
import com.eyeem.notes.event.BubbleClickedEvent;
import com.eyeem.notes.event.NoteClickedEvent;
import com.eyeem.notes.model.Note;
import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by vishna on 18/02/15.
 */
public class NoteHolder extends RecyclerView.ViewHolder implements ChipsTextView.OnBubbleClickedListener {

   @InjectView(R.id.note_text_view) ChipsTextView textView;
   final Bus bus;
   Note note;

   public NoteHolder(View itemView, Bus bus) {
      super(itemView);
      ButterKnife.inject(this, itemView);
      this.bus = bus;
      textView.setOnBubbleClickedListener(this);
   }

   public NoteHolder setNote(Note note) {
      this.note = note;
      return this;
   }

   @OnClick(R.id.note_container) void onClick(View view) {
      if (note != null) bus.post(new NoteClickedEvent(note));
   }

   @Override public void onBubbleClicked(View view, BubbleSpan bubbleSpan) {

      if (bubbleSpan.data() instanceof ChipsTextView.Truncation) {
         textView.expand(true);
         return;
      }

      bus.post(new BubbleClickedEvent(view, bubbleSpan));
   }
}
