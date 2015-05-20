package com.eyeem.notes.screen;

import android.os.Bundle;
import android.text.SpannableString;
import android.widget.Toast;

import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.Utils;
import com.eyeem.notes.R;
import com.eyeem.notes.event.TextSnapshotCaptured;
import com.eyeem.notes.mortarflow.HasParent;
import com.eyeem.notes.mortarflow.Layout;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.PreviewView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import flow.path.Path;
import lombok.Getter;
import mortar.MortarScope;
import mortar.ViewPresenter;

import static com.eyeem.notes.model.Note.from;

/**
 * Created by vishna on 18/02/15.
 */
@Layout(R.layout.preview) @WithComponent(Preview.Component.class)
public class Preview extends Path implements HasParent {

   public final static String KEY_INSTANCE_STATE = Preview.class.getCanonicalName() + "." + "KEY_INSTANCE_STATE";

   @Override public Path getParent() {
      return new Start();
   }

   @dagger.Component(modules = Module.class, dependencies = Note.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component {
      void inject(PreviewView t);
   }

   @dagger.Module
   public static class Module {
   }

   @ScopeSingleton(Component.class)
   public static class Presenter extends ViewPresenter<PreviewView> {

      @Inject @Named("noteBus") Bus noteBus;
      @Inject com.eyeem.notes.model.Note sourceNote;
      @Getter com.eyeem.notes.model.Note previewNote;

      @Inject Presenter() {}

      @Override protected void onLoad(Bundle savedInstanceState) {
         super.onLoad(savedInstanceState);
         if (savedInstanceState != null) getView().onRestoreInstanceState(savedInstanceState.getParcelable(KEY_INSTANCE_STATE));
      }

      @Override protected void onSave(Bundle outState) {
         super.onSave(outState);
         outState.putParcelable(KEY_INSTANCE_STATE, getView().onSaveInstanceState());
      }

      @Override protected void onEnterScope(MortarScope scope) {
         super.onEnterScope(scope);
         noteBus.register(this);
      }

      @Override protected void onExitScope() {
         super.onExitScope();
         noteBus.unregister(this);
      }

      @Subscribe public void textSnapshotCaptured(TextSnapshotCaptured textSnapshotCaptured) {
         previewNote = from(sourceNote.id, textSnapshotCaptured.getSnapshot());
         populateNote();
      }

      @Subscribe public void menuAction(ActionBarOwner.MenuAction menuAction) {
         if (menuAction.stringId == R.string.tag_setup && hasView()) {
            Toast.makeText(getView().getContext(), Utils.tag_setup(new SpannableString(getView().getTv().getLayoutBuild().getSpannable())), Toast.LENGTH_LONG).show();
         }
      }

      public void populateNote() {
         if (!hasView()) return;
         com.eyeem.notes.model.Note note = getPreviewNote();
         if (note == null) {
            note = sourceNote;
         }
         if (note == null) return;
         BubbleStyle style = com.eyeem.notes.model.Note.defaultBubbleStyle(getView().getContext(), getView().getTv().getTextSize());
         getView().getTv().setText(note.textSpan(style, null));
      }
   }
}
