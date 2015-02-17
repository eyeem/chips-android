package com.eyeem.chipsedittextdemo.screen;

import android.os.Bundle;

import com.eyeem.chips.LayoutBuild;
import com.eyeem.chipsedittextdemo.MainActivity;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.adapter.NotesAdapter;
import com.eyeem.chipsedittextdemo.core.BootstrapNotesModule;
import com.eyeem.chipsedittextdemo.core.NoteStorage;
import com.eyeem.chipsedittextdemo.experimental.CacheOnScroll;
import com.eyeem.chipsedittextdemo.experimental.PausableThreadPoolExecutor;
import com.eyeem.chipsedittextdemo.model.Note;
import com.eyeem.chipsedittextdemo.mortarflow.DynamicModules;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;
import com.eyeem.chipsedittextdemo.mortarflow.WithComponent;
import com.eyeem.chipsedittextdemo.view.NotesView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

      @Provides CacheOnScroll<LayoutBuild> provideCacheOnScroll() {
         return new CacheOnScroll<>(sPausableExecutor, 100);
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

   private final static PausableThreadPoolExecutor sPausableExecutor;
   // Sets the amount of time an idle thread waits before terminating
   private static final int NUMBER_OF_CORES = 2;
   // Sets the amount of time an idle thread waits before terminating
   private static final int KEEP_ALIVE_TIME = 5;
   // Sets the Time Unit to seconds
   private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

   static {
      sPausableExecutor = new PausableThreadPoolExecutor(
         NUMBER_OF_CORES,       // Initial pool size
         NUMBER_OF_CORES,       // Max pool size
         KEEP_ALIVE_TIME,
         KEEP_ALIVE_TIME_UNIT,
         new LinkedBlockingQueue<Runnable>());
   }
}
