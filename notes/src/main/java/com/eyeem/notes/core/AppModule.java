package com.eyeem.notes.core;

import android.content.Context;

import com.eyeem.notes.App;
import com.eyeem.notes.mortarflow.FlowBundler;
import com.eyeem.notes.mortarflow.GsonParceler;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.screen.Notes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import dagger.Module;
import dagger.Provides;
import flow.History;
import flow.StateParceler;

@Module
public class AppModule {

   private final App app;

   public AppModule(App app) {
      this.app = app;
   }

   @Provides Context context() { return app; }

   @Provides App app() { return app; }

   @Provides @ScopeSingleton(App.Component.class) NoteStorage provideNoteStorage(App app) {
      return new NoteStorage(app);
   }

   @Provides @ScopeSingleton(App.Component.class) FlowBundler provideFlowBundler(StateParceler parceler, final NoteStorage storage) {
      return new FlowBundler(parceler) {
         @Override protected History getColdStartHistory(History restoredHistory) {
            return restoredHistory == null ? History.single(new Notes(storage.all())) : restoredHistory;
         }
      };
   }

   @Provides @ScopeSingleton(App.Component.class) Gson provideGson() {
      return new GsonBuilder().create();
   }

   @Provides @ScopeSingleton(App.Component.class) StateParceler provideParcer(Gson gson) {
      return new GsonParceler(gson);
   }

   @Provides @ScopeSingleton(App.Component.class) Bus provideBus() {
      return new Bus();
   }
}
