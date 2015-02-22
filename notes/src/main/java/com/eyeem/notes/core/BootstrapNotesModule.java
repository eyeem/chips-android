package com.eyeem.notes.core;

import com.eyeem.chips.Linkify;
import com.eyeem.notes.App;
import com.eyeem.notes.model.Note;
import com.eyeem.notes.utils.Assets;
import com.eyeem.notes.utils.LinkifyDeserialiser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by vishna on 17/02/15.
 */
@Module public class BootstrapNotesModule {

   @Provides public Observable<List<Note>> bootstrapNotes(final App app) {
      return
         Assets.from(app, "notes.json")
            .map((String jsonString) -> fromJSONString(jsonString)).subscribeOn(Schedulers.io());
   }

   private static List<Note> fromJSONString(String jsonString) {
      Gson gson = new GsonBuilder().registerTypeAdapter(Linkify.Entities.class, new LinkifyDeserialiser()).create();
      Type listType = new TypeToken<ArrayList<Note>>() {}.getType();
      return gson.fromJson(jsonString, listType);
   }
}
