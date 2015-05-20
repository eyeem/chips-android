package com.eyeem.notes;

import android.app.Application;

import com.eyeem.notes.core.StorageDep;
import com.eyeem.notes.core.AppDep;
import com.eyeem.notes.core.AppModule;
import com.eyeem.notes.core.PausableExecutorModule;
import com.eyeem.notes.mortarflow.FlowDep;
import com.eyeem.notes.mortarflow.ScopeSingleton;

import mortar.MortarScope;

import static com.eyeem.notes.mortarflow.Utils.DAGGER_SERVICE;
import static com.eyeem.notes.mortarflow.Utils.createComponent;


/**
 * Created by vishna on 27/01/15.
 */
public class App extends Application {
   private MortarScope rootScope;

   @dagger.Component(modules = {AppModule.class, PausableExecutorModule.class})
   @ScopeSingleton(Component.class)
   public interface Component extends FlowDep, AppDep, StorageDep {}

   @Override public void onCreate() {
      super.onCreate();
   }

   @Override public Object getSystemService(String name) {
      if (rootScope == null) {
         rootScope = MortarScope.buildRootScope()
            .withService(DAGGER_SERVICE, createComponent(Component.class, new AppModule(this)))
            .build(App.class.getName());
      }

      return rootScope.hasService(name) ? rootScope.getService(name) : super.getSystemService(name);
   }
}
