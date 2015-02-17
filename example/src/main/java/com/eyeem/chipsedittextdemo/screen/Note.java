package com.eyeem.chipsedittextdemo.screen;

import com.eyeem.chipsedittextdemo.MainActivity;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.core.AppDep;
import com.eyeem.chipsedittextdemo.core.NoteStorage;
import com.eyeem.chipsedittextdemo.mortarflow.DynamicModules;
import com.eyeem.chipsedittextdemo.mortarflow.FlowDep;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;
import com.eyeem.chipsedittextdemo.mortarflow.WithComponent;
import com.eyeem.chipsedittextdemo.view.NoteView;
import com.eyeem.storage.Storage;

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
