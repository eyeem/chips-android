package com.eyeem.notes.screen;

import android.os.Bundle;

import com.eyeem.chips.LayoutBuild;
import com.eyeem.notes.MainActivity;
import com.eyeem.notes.R;
import com.eyeem.notes.adapter.NotesAdapter;
import com.eyeem.notes.core.BootstrapNotesModule;
import com.eyeem.notes.core.NoteStorage;
import com.eyeem.notes.experimental.CacheOnScroll;
import com.eyeem.notes.experimental.PausableThreadPoolExecutor;
import com.eyeem.notes.model.Note;
import com.eyeem.notes.mortarflow.DynamicModules;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.NotesView;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.Provides;
import flow.Layout;
import flow.Path;
import mortar.ViewPresenter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by vishna on 03/02/15.
 */
@Layout(R.layout.notes) @WithComponent(Notes.Component.class)
public class Notes extends Path implements DynamicModules {

   NoteStorage.List list;

   public Notes(NoteStorage.List list) {
      this.list = list;
   }

   @Override public List<Object> dependencies() {
      return Arrays.<Object>asList(new Module(list));
   }

   @dagger.Component(modules = {Module.class, BootstrapNotesModule.class}, dependencies = MainActivity.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component {
      void inject(NotesView t);
   }

   @dagger.Module
   public static class Module {

      NoteStorage.List list;

      public Module(NoteStorage.List list) {
         this.list = list;
      }

      @Provides NotesAdapter provideAdapter(CacheOnScroll<LayoutBuild> cacheOnScroll) {
         return new NotesAdapter(list, cacheOnScroll);
      }

      @Provides CacheOnScroll<LayoutBuild> provideCacheOnScroll(PausableThreadPoolExecutor pausableThreadPoolExecutor) {
         return new CacheOnScroll<>(pausableThreadPoolExecutor, 100);
      }

      @Provides NoteStorage.List provideNoteStorageList() {
         return list;
      }
   }

   @ScopeSingleton(Component.class)
   public static class Presenter extends ViewPresenter<NotesView> {

      private final Observable<List<Note>> noteSource;
      private final NoteStorage.List noteStorage;

      @Inject Presenter(NoteStorage.List noteStorage, Observable<List<Note>> noteSource) {
         this.noteStorage = noteStorage;
         this.noteSource = noteSource;
      }

      @Override protected void onLoad(Bundle savedInstanceState) {
         super.onLoad(savedInstanceState);
         if (!hasView()) return;

         noteSource
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Note>>() {
               @Override public void call(List<Note> notes) {
                  if (!hasView() || noteStorage.size() > 0) return;
                  noteStorage.addAll(notes);
               }
            });
      }

      @Override protected void onSave(Bundle outState) {
         super.onSave(outState);

      }
   }
}
