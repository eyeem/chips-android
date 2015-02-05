package com.eyeem.chipsedittextdemo.core;

import com.eyeem.chips.Linkify;
import com.eyeem.chipsedittextdemo.model.Note;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vishna on 04/02/15.
 */
@Module
public class RandomNotesModule {

   @Provides public Observable<List<Note>> randomNotes() {
      return Observable.create(new Observable.OnSubscribe<List<Note>>() {
         @Override
         public void call(Subscriber<? super List<Note>> subscriber) {
            List<Note> notes = generateTestNotes();
            if (! subscriber.isUnsubscribed()) {
               subscriber.onNext(notes);
            }
         }
      })
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread());
   }

   private static ArrayList<Note> generateTestNotes() {
      ArrayList<Note> notes = new ArrayList<>();
      for (int i = 0; i < COUNT; i++) {
         notes.add(generateNote(i));
      }
      return notes;
   }

   public static Note generateNote(int id) {
      Note note = new Note(LOREM_IPSUM, new Linkify.Entities());

      for (int j = 0; j < LOREM_IPSUM.length(); j++) {
         int shouldAdd = (int) (ODDS * Math.random());
         int next = (int) (10 * Math.random());
         if (shouldAdd % ODDS == 0 && next > 0 && j + next < LOREM_IPSUM.length()) {
            note.entities.add(new Linkify.Entity(j, j + next, "id:" + id + ":" + j, LOREM_IPSUM.substring(j, j + next), Linkify.Entity.ALBUM));
         }
         j += next;
      }

      return note;
   }

   private final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse id dui vitae massa pretium scelerisque. Suspendisse in molestie ex. Integer feugiat magna et mi sagittis laoreet. Cras sit amet elit scelerisque, pulvinar ante in, iaculis neque. Aliquam nulla velit, laoreet quis imperdiet eu, viverra sagittis elit. Aliquam erat volutpat. Integer posuere porta diam, eget maximus risus. Pellentesque ex erat, tempor quis quam et, scelerisque pulvinar odio. Fusce sollicitudin vestibulum dignissim. Vestibulum consectetur pretium ex sit amet hendrerit. Praesent in malesuada leo, at congue orci. Sed vestibulum orci id euismod fermentum. Donec placerat justo non hendrerit pellentesque. Praesent blandit faucibus dolor, eu dignissim arcu fermentum vitae. Praesent at porttitor purus, sit amet commodo felis. Integer congue bibendum convallis. Nulla tristique nisi consectetur dui porttitor tempor. Nulla et mi vehicula mi laoreet accumsan. Maecenas iaculis et sem et efficitur. Morbi pretium lacus id felis bibendum, ac lacinia purus ornare. Aliquam eu erat felis. Proin venenatis sodales ipsum ac vestibulum. In elementum ut metus non viverra.";

   private final static int ODDS = 4;
   private final static int COUNT = 1000;
}
