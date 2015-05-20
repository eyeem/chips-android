package com.eyeem.notes.screen;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.eyeem.chips.BubbleSpan;
import com.eyeem.chips.LayoutBuild;
import com.eyeem.notes.MainActivity;
import com.eyeem.notes.R;
import com.eyeem.notes.adapter.NotesAdapter;
import com.eyeem.notes.core.BootstrapNotesModule;
import com.eyeem.notes.core.NoteStorage;
import com.eyeem.notes.event.BubbleClickedEvent;
import com.eyeem.notes.event.NewNoteEvent;
import com.eyeem.notes.event.NoteClickedEvent;
import com.eyeem.notes.experimental.CacheOnScroll;
import com.eyeem.notes.experimental.PausableThreadPoolExecutor;
import com.eyeem.notes.model.Note;
import com.eyeem.notes.mortarflow.DynamicModules;
import com.eyeem.notes.mortarflow.Layout;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.NotesView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.Provides;
import flow.Flow;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by vishna on 03/02/15.
 */
@Layout(R.layout.notes) @WithComponent(Notes.Component.class)
public class Notes extends Path implements DynamicModules, ActionBarOwner.MenuActions {

   NoteStorage.List list;

   public Notes(NoteStorage.List list) {
      this.list = list;
   }

   @Override public List<Object> dependencies() {
      return Arrays.<Object>asList(new Module(list));
   }

   @Override public List<ActionBarOwner.MenuAction> menuActions() {
      return Arrays.asList(
         new ActionBarOwner.MenuAction(R.string.clear_all_notes)
      );
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

      @Provides NotesAdapter provideAdapter(CacheOnScroll<LayoutBuild> cacheOnScroll, Bus bus) {
         return new NotesAdapter(list, cacheOnScroll, bus);
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

      @Inject Observable<List<Note>> noteSource;
      @Inject NoteStorage.List noteList;
      @Inject Bus bus;

      @Inject Presenter() {}

      @Override protected void onLoad(Bundle savedInstanceState) {
         super.onLoad(savedInstanceState);
         if (!hasView()) return;

         noteSource
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<Note>>() {
               @Override public void call(List<Note> notes) {
                  if (!hasView() || noteList.size() > 0) return;
                  noteList.addAll(notes);
               }
            });
      }

      @Override protected void onEnterScope(MortarScope scope) {
         super.onEnterScope(scope);
         bus.register(this);
      }

      @Override protected void onExitScope() {
         super.onExitScope();
         bus.unregister(this);
      }

      @Subscribe public void noteClicked(NoteClickedEvent noteClickedEvent) {
         Note note = noteClickedEvent.note;
         if (hasView()) Flow.get(getView()).set(new com.eyeem.notes.screen.Note(noteList, note.id));
      }

      @Subscribe public void bubbleClicked(BubbleClickedEvent bubbleClickedEvent) {
         BubbleSpan span = bubbleClickedEvent.bubbleSpan;
         // TODO some event handling links, emails and such
         Toast.makeText(getView().getContext(), span.data().toString(), Toast.LENGTH_SHORT).show();
      }

      @Subscribe public void newNoteClicked(NewNoteEvent newNoteEvent) {
         Flow.get(getView().getContext()).set(new com.eyeem.notes.screen.Note(noteList, null));
      }

      @Subscribe public void  menuAction(ActionBarOwner.MenuAction action) {
         if (action.stringId == R.string.clear_all_notes) {
            noteList.clear();
         }
      }
   }
}
