package com.eyeem.notes.screen;

import android.text.TextUtils;

import com.eyeem.notes.MainActivity;
import com.eyeem.notes.R;
import com.eyeem.notes.core.AppDep;
import com.eyeem.notes.core.NoteStorage;
import com.eyeem.notes.mortarflow.DynamicModules;
import com.eyeem.notes.mortarflow.FlowDep;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.NoteView;
import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import flow.HasParent;
import flow.Layout;
import flow.Path;
import mortar.ViewPresenter;

/**
 * Created by vishna on 16/02/15.
 */
@Layout(R.layout.note) @WithComponent(Note.Component.class)
public class Note extends Path implements HasParent, DynamicModules {

   Module module;

   public Note(NoteStorage.List list, String noteId) {
      this.module = new Module(list, noteId);
   }

   @Override public Path getParent() {
      return new Notes(module.list);
   }

   @Override public List<Object> dependencies() {
      return Arrays.<Object>asList(module);
   }

   @dagger.Component(modules = Module.class, dependencies = MainActivity.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component extends FlowDep, AppDep {
      void inject(NoteView t);
      com.eyeem.notes.model.Note provideNote();
      @Named("noteBus") Bus provideNoteBus();
   }

   @dagger.Module public static class Module {

      NoteStorage.List list;
      String noteId;
      com.eyeem.notes.model.Note localNote;
      Bus noteBus;
      NoteStorage storage;

      public Module(NoteStorage.List list, String noteId) {
         this.list = list;
         this.noteId = noteId;
         this.noteBus = new Bus();
      }

      @Provides NoteStorage.List provideNoteList() {
         return list;
      }

      @Provides com.eyeem.notes.model.Note provideNote(NoteStorage storage) {
         if (localNote == null) {
            if (!TextUtils.isEmpty(noteId)) {
               localNote = storage.get(noteId);
            } else {
               localNote = new com.eyeem.notes.model.Note();
               localNote.id = String.valueOf(System.currentTimeMillis());
            }
         }
         return localNote;
      }

      @Provides @Named("noteBus") Bus provideNoteBus() {
         return noteBus;
      }
   }

   @ScopeSingleton(Component.class) public static class Presenter extends ViewPresenter<NoteView> {
      @Inject Presenter() {
      }
   }
}
