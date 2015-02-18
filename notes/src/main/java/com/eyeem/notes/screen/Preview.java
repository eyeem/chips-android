package com.eyeem.notes.screen;

import android.os.Bundle;

import com.eyeem.notes.R;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.utils.RxBus;
import com.eyeem.notes.view.PreviewView;

import javax.inject.Inject;

import flow.HasParent;
import flow.Layout;
import flow.Path;
import mortar.ViewPresenter;

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

      @Inject RxBus bus;

      @Inject Presenter() {}

      @Override protected void onLoad(Bundle savedInstanceState) {
         super.onLoad(savedInstanceState);
         if (savedInstanceState != null) getView().onRestoreInstanceState(savedInstanceState.getParcelable(KEY_INSTANCE_STATE));
      }

      @Override protected void onSave(Bundle outState) {
         super.onSave(outState);
         outState.putParcelable(KEY_INSTANCE_STATE, getView().onSaveInstanceState());
      }
   }
}
