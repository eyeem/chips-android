package com.eyeem.chipsedittextdemo.screen;

import com.eyeem.chipsedittextdemo.MainActivity;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.mortarflow.FlowDep;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;
import com.eyeem.chipsedittextdemo.mortarflow.WithComponent;
import com.eyeem.chipsedittextdemo.view.NoteView;

import javax.inject.Inject;

import flow.Layout;
import flow.Path;
import mortar.ViewPresenter;

/**
 * Created by vishna on 16/02/15.
 */
@Layout(R.layout.note) @WithComponent(Note.Component.class)
public class Note extends Path {

   @dagger.Component(dependencies = MainActivity.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component extends FlowDep {
      void inject(NoteView t);
   }

   @dagger.Module
   public static class Module {
      // adapter maybe ?
   }

   @ScopeSingleton(Component.class)
   public static class Presenter extends ViewPresenter<NoteView> {

      @Inject Presenter() {
      }

   }
}
