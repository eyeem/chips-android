package com.eyeem.chipsedittextdemo.screen;

import com.eyeem.chipsedittextdemo.MainActivity;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;
import com.eyeem.chipsedittextdemo.mortarflow.WithComponent;
import com.eyeem.chipsedittextdemo.view.StartView;

import javax.inject.Inject;

import flow.Layout;
import flow.Path;
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
