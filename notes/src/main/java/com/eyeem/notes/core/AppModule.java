package com.eyeem.notes.core;

import android.content.Context;

import com.eyeem.notes.App;
import com.eyeem.notes.mortarflow.FlowBundler;
import com.eyeem.notes.mortarflow.GsonParceler;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.screen.Notes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import dagger.Module;
import dagger.Provides;
import flow.Backstack;
import flow.Parceler;

@Module
public class AppModule {

   private final App app;

   public AppModule(App app) {
      this.app = app;
   }

   @Provides Context context() { return app; }

   @Provides App app() { return app; }

   @Provides @ScopeSingleton(App.Component.class) Picasso providePicasso() {
      return Picasso.with(app);
   }

   @Provides @ScopeSingleton(App.Component.class) NoteStorage provideNoteStorage(App app) {
      return new NoteStorage(app);
   }

   @Provides @ScopeSingleton(App.Component.class) FlowBundler provideFlowBundler(Parceler parceler, final NoteStorage storage) {
      return new FlowBundler(parceler) {
         @Override protected Backstack getColdStartBackstack(Backstack restoredBackstack) {
            return restoredBackstack == null ? Backstack.single(new Notes(storage.all())) : restoredBackstack;
         }
      };
   }

   @Provides @ScopeSingleton(App.Component.class) Gson provideGson() {
      return new GsonBuilder().create();
   }

   @Provides @ScopeSingleton(App.Component.class) Parceler provideParcer(Gson gson) {
      return new GsonParceler(gson);
   }
}
