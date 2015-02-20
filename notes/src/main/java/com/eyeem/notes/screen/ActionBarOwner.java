package com.eyeem.notes.screen;

import android.content.Context;
import android.os.Bundle;

import com.eyeem.notes.MainActivity;
import com.eyeem.notes.mortarflow.ScopeSingleton;

import java.util.List;

import javax.inject.Inject;

import mortar.bundler.BundleService;

import static mortar.bundler.BundleService.getBundleService;

/** Allows shared configuration of the Android ActionBar. */
public class ActionBarOwner {

   public interface Activity {
      void setShowHomeEnabled(boolean enabled);

      void setUpButtonEnabled(boolean enabled);

      void setTitle(CharSequence title);

      void setMenu(List<MenuAction> actions);

      Context getContext();
   }

   public static class Config {
      public final boolean showHomeEnabled;
      public final boolean upButtonEnabled;
      public final CharSequence title;
      public final List<MenuAction> actions;

      public Config(boolean showHomeEnabled, boolean upButtonEnabled, CharSequence title,
                    List<MenuAction> actions) {
         this.showHomeEnabled = showHomeEnabled;
         this.upButtonEnabled = upButtonEnabled;
         this.title = title;
         this.actions = actions;
      }

      public Config withAction(List<MenuAction> actions) {
         return new Config(showHomeEnabled, upButtonEnabled, title, actions);
      }
   }

   public static class MenuAction {
      public final int stringId;

      public MenuAction(int stringId) {
         this.stringId = stringId;
      }
   }

   @ScopeSingleton(MainActivity.Component.class)
   public static class Presenter extends mortar.Presenter<Activity> {

      @Inject Presenter() {}

      @Override protected BundleService extractBundleService(Activity activity) {
         return getBundleService(activity.getContext());
      }

      private Config config;

      public void setConfig(Config config) {
         this.config = config;
         update();
      }

      public Config getConfig() {
         return config;
      }

      @Override protected void onLoad(Bundle savedInstanceState) {
         if (config != null) update();
      }

      private void update() {
         if (!hasView()) return;
         Activity activity = getView();

         activity.setShowHomeEnabled(config.showHomeEnabled);
         activity.setUpButtonEnabled(config.upButtonEnabled);
         activity.setTitle(config.title);
         activity.setMenu(config.actions);
      }
   }

   public interface MenuActions {
      List<MenuAction> menuActions();
   }
}