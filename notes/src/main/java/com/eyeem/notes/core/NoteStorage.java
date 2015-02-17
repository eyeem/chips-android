package com.eyeem.notes.core;

import android.content.Context;

import com.eyeem.notes.model.Note;
import com.eyeem.storage.Storage;

/**
 * Created by vishna on 16/02/15.
 */
public class NoteStorage extends Storage<Note> {

   public NoteStorage(Context context) {
      super(context);
      init();
   }

   @Override public String id(Note note) {
      return note.id;
   }

   @Override public Class<Note> classname() {
      return Note.class;
   }

   public List all() {
      return obtainList("all");
   }
}
