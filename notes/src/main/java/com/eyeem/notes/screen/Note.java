package com.eyeem.notes.screen;

import com.eyeem.notes.MainActivity;
import com.eyeem.notes.R;
import com.eyeem.notes.core.AppDep;
import com.eyeem.notes.core.NoteStorage;
import com.eyeem.notes.mortarflow.DynamicModules;
import com.eyeem.notes.mortarflow.FlowDep;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.NoteView;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

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

   @dagger.Component(dependencies = MainActivity.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component extends FlowDep, AppDep {
      void inject(NoteView t);
   }

   @dagger.Module public static class Module {

      NoteStorage.List list;
      String noteId;

      public Module(NoteStorage.List list, String noteId) {
         this.list = list;
         this.noteId = noteId;
      }

      @Provides NoteStorage.List provideNoteList() {
         return list;
      }
      // TODO provideList
      // TODO provideNote
   }

   @ScopeSingleton(Component.class) public static class Presenter extends ViewPresenter<NoteView> {

      @Inject Presenter() {
      }

   }
}
