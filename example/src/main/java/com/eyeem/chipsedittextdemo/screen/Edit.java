package com.eyeem.chipsedittextdemo.screen;

import android.os.Bundle;

import com.eyeem.chipsedittextdemo.MainActivity;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.core.AppModule;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;
import com.eyeem.chipsedittextdemo.mortarflow.WithComponent;
import com.eyeem.chipsedittextdemo.view.EditView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Provides;
import flow.HasParent;
import flow.Layout;
import flow.Path;
import mortar.ViewPresenter;

/**
 * Created by vishna on 27/01/15.
 */
@Layout(R.layout.edit) @WithComponent(Edit.Component.class)
public class Edit extends Path implements HasParent {

   public final static String KEY_INSTANCE_STATE = Edit.class.getCanonicalName() + "." + "KEY_INSTANCE_STATE";

   @Override public Path getParent() {
      return new Start();
   }

   @dagger.Component(modules = Module.class, dependencies = Note.Component.class)
   @ScopeSingleton(Component.class)
   public interface Component {
      void inject(EditView t);
   }

   @dagger.Module
   public static class Module {
      @Provides List<String> provideSuggestions() {
         final ArrayList<String> availableItems = new ArrayList<String>();
         availableItems.add("Changing the world");
         availableItems.add("Best startup ever");
         availableItems.add("Lars but not least");
         availableItems.add("My Ramzi");
         availableItems.add("Walking around");
         availableItems.add("Today's hot look");
         availableItems.add("In other news");
         availableItems.add("Screw that stuff");
         availableItems.add("x");
         availableItems.add("NADA NADA NADA");
         availableItems.add("...");
         availableItems.add("IPA");
         availableItems.add("#flowers#nature#hangingout#takingphotos#colors#hello world#flora#fauna");
         return availableItems;
      }
   }

   @ScopeSingleton(Component.class)
   public static class Presenter extends ViewPresenter<EditView> {

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
