package com.eyeem.chipsedittextdemo.core;

import com.eyeem.chips.Linkify;
import com.eyeem.chipsedittextdemo.App;
import com.eyeem.chipsedittextdemo.model.Note;
import com.eyeem.chipsedittextdemo.utils.Assets;
import com.eyeem.chipsedittextdemo.utils.LinkifyDeserialiser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by vishna on 17/02/15.
 */
@Module public class BootstrapNotesModule {

   @Provides public Observable<List<Note>> bootstrapNotes(final App app) {
      return Observable.create(new Observable.OnSubscribe<List<Note>>() {
         @Override
         public void call(Subscriber<? super List<Note>> subscriber) {

            String jsonString = Assets.from(app, "notes.json");
            List<Note> notes = fromJSONString(jsonString);

            if (!subscriber.isUnsubscribed()) {
               subscriber.onNext(notes);
            }
         }
      })
         .subscribeOn(Schedulers.io());
   }

   private static List<Note> fromJSONString(String jsonString) {
      Gson gson = new GsonBuilder().registerTypeAdapter(Linkify.Entities.class, new LinkifyDeserialiser()).create();
      Type listType = new TypeToken<ArrayList<Note>>() {}.getType();
      return gson.fromJson(jsonString, listType);
   }
}
