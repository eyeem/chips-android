package com.eyeem.notes.screen;

import com.eyeem.notes.MainActivity;
import com.eyeem.notes.R;
import com.eyeem.notes.mortarflow.Layout;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.mortarflow.WithComponent;
import com.eyeem.notes.view.StartView;

import javax.inject.Inject;

import flow.path.Path;
import mortar.ViewPresenter;

@Layout(R.layout.start) @WithComponent(Start.Component.class)
public class Start extends Path {

   @dagger.Component(dependencies = MainActivity.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component {
      void inject(StartView view);
   }

   @ScopeSingleton(Component.class)
   public static class Presenter extends ViewPresenter<StartView> {
      @Inject Presenter() {}
   }

}
