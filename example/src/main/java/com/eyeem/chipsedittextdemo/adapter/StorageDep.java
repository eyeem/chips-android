package com.eyeem.chipsedittextdemo.adapter;

import com.eyeem.chipsedittextdemo.core.NoteStorage;

/**
 * Created by vishna on 16/02/15.
 */
public interface StorageDep {
   NoteStorage provideNoteStorage();
}
