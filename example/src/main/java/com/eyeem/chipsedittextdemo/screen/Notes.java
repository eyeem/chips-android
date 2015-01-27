package com.eyeem.chipsedittextdemo.screen;

import android.os.Bundle;

import com.eyeem.chips.Linkify;
import com.eyeem.chipsedittextdemo.MainActivity;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.adapter.NotesAdapter;
import com.eyeem.chipsedittextdemo.core.RandomNotesModule;
import com.eyeem.chipsedittextdemo.model.Note;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;
import com.eyeem.chipsedittextdemo.mortarflow.WithComponent;
import com.eyeem.chipsedittextdemo.view.NotesView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Provides;
import flow.HasParent;
import flow.Layout;
import flow.Path;
import mortar.ViewPresenter;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by vishna on 03/02/15.
 */
@Layout(R.layout.notes) @WithComponent(Notes.Component.class)
public class Notes extends Path implements HasParent {

   @Override public Path getParent() {
      return new Start();
   }

   @dagger.Component(modules = {Module.class, RandomNotesModule.class}, dependencies = MainActivity.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component {
      void inject(NotesView t);
   }

   @dagger.Module
   public static class Module {
      @Provides NotesAdapter provideAdapter() {
         return new NotesAdapter();
      }
   }

   @ScopeSingleton(Component.class)
   public static class Presenter extends ViewPresenter<NotesView> {

      private final Observable<List<Note>> noteSource;
      private List<Note> notes;

      @Inject Presenter(Observable<List<Note>> noteSource) {
         this.noteSource = noteSource;
      }

      @Override protected void onLoad(Bundle savedInstanceState) {
         super.onLoad(savedInstanceState);
         if (!hasView()) return;

         noteSource.subscribe(new Action1<List<Note>>() {
            @Override public void call(List<Note> notes) {
               if (!hasView()) return;
               Presenter.this.notes = notes;
               getView().setNotes(notes);
            }
         });
      }

      @Override protected void onSave(Bundle outState) {
         super.onSave(outState);

      }
   }
}
