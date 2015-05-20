package com.eyeem.notes.screen;

import android.text.TextUtils;

import com.eyeem.notes.MainActivity;
import com.eyeem.notes.R;
import com.eyeem.notes.core.AppDep;
import com.eyeem.notes.core.NoteStorage;
import com.eyeem.notes.event.PreviewRefresh;
import com.eyeem.notes.mortarflow.BaseComponent;
import com.eyeem.notes.mortarflow.DynamicModules;
import com.eyeem.notes.mortarflow.FlowDep;
import com.eyeem.notes.mortarflow.HasParent;
import com.eyeem.notes.mortarflow.Layout;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.NoteView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import flow.path.Path;
import mortar.MortarScope;
import mortar.ViewPresenter;

/**
 * Created by vishna on 16/02/15.
 */
@Layout(R.layout.note) @WithComponent(Note.Component.class)
public class Note extends Path implements HasParent, DynamicModules, ActionBarOwner.MenuActions {

   Module module;

   public Note(NoteStorage.List list, String noteId) {
      this.module = new Module(list, noteId);
   }

   @Override public Path getParent() {
      return new Notes(module.list);
   }

   @Override public List<Object> dependencies() {
      return Arrays.asList(module);
   }

   @Override public List<ActionBarOwner.MenuAction> menuActions() {
      return Arrays.asList(
         new ActionBarOwner.MenuAction(R.string.clear_note),
         new ActionBarOwner.MenuAction(R.string.save_note),
         new ActionBarOwner.MenuAction(R.string.tag_setup)
      );
   }

   @dagger.Component(modules = Module.class, dependencies = BaseComponent.class)
   @ScopeSingleton(Component.class)
   public interface Component extends FlowDep, AppDep {
      void inject(NoteView t);
      com.eyeem.notes.model.Note provideNote();
      @Named("noteBus") Bus provideNoteBus();
   }

   @dagger.Module public static class Module {

      NoteStorage.List list;
      String noteId;
      com.eyeem.notes.model.Note localNote;
      Bus noteBus;
      NoteStorage storage;

      public Module(NoteStorage.List list, String noteId) {
         this.list = list;
         this.noteId = noteId;
         this.noteBus = new Bus("noteBus");
      }

      @Provides NoteStorage.List provideNoteList() {
         return list;
      }

      @Provides com.eyeem.notes.model.Note provideNote(NoteStorage storage) {
         if (localNote == null) {
            if (!TextUtils.isEmpty(noteId)) {
               localNote = storage.get(noteId);
            } else {
               localNote = new com.eyeem.notes.model.Note();
               localNote.id = String.valueOf(System.currentTimeMillis());
            }
         }
         return localNote;
      }

      @Provides @Named("noteBus") Bus provideNoteBus() {
         return noteBus;
      }
   }

   @ScopeSingleton(Component.class) public static class Presenter extends ViewPresenter<NoteView> {

      @Inject Bus bus;
      @Inject @Named("noteBus") Bus noteBus;
      @Inject Presenter() {}

      @Override protected void onEnterScope(MortarScope scope) {
         super.onEnterScope(scope);
         bus.register(this);
      }

      @Override protected void onExitScope() {
         super.onExitScope();
         bus.unregister(this);
      }

      @Subscribe public void menuAction(ActionBarOwner.MenuAction menuAction) {
         if (!hasView()) return;

         if (menuAction.stringId == R.string.tag_setup && getView().getPager().getCurrentItem() != 1) {
            // force generate the preview if we are on a wrong page
            noteBus.post(new PreviewRefresh());
         }

         noteBus.post(menuAction);
      }

      public void onPageSelected(int page) {
         if (page == 1) noteBus.post(new PreviewRefresh());
      }
   }
}
