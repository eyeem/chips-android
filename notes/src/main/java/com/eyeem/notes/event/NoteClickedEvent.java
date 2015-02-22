package com.eyeem.notes.event;

import com.eyeem.notes.model.Note;

/**
 * Created by vishna on 18/02/15.
 */
public class NoteClickedEvent {
   public Note note;

   public NoteClickedEvent(Note note) {
      this.note = note;
   }
}
