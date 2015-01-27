package com.eyeem.chipsedittextdemo;

import android.app.Application;

import com.eyeem.chipsedittextdemo.core.AppModule;
import com.eyeem.chipsedittextdemo.mortarflow.FlowDep;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;

import mortar.Mortar;
import mortar.MortarScope;
import mortar.dagger2support.Dagger2;

/**
 * Created by vishna on 27/01/15.
 */
public class App extends Application {
   private MortarScope rootScope;

   @dagger.Component(modules = AppModule.class)
   @ScopeSingleton(Component.class)
   public interface Component extends FlowDep {}

   @Override public void onCreate() {
      super.onCreate();

      Component appComponent = Dagger2.buildComponent(Component.class, new AppModule(this));
      rootScope = Mortar.createRootScope(appComponent);
   }

   @Override public Object getSystemService(String name) {
      if (Mortar.isScopeSystemService(name)) {
         return rootScope;
      }
      return super.getSystemService(name);
   }
}
