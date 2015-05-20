package com.eyeem.notes.mortarflow;


import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;

import flow.path.Path;
import flow.path.PathContextFactory;
import mortar.MortarScope;

import static mortar.MortarScope.getScope;

public final class MortarContextFactory implements PathContextFactory {
   private final ScreenScoper screenScoper = new ScreenScoper();

   public MortarContextFactory() {
   }

   @Override public Context setUpContext(Path path, Context parentContext) {
      MortarScope screenScope =
         screenScoper.getScreenScope(parentContext, path.getClass().getName(), path);
      return new TearDownContext(parentContext, screenScope);
   }

   @Override public void tearDownContext(Context context) {
      TearDownContext.destroyScope(context);
   }

   static class TearDownContext extends ContextWrapper {
      private static final String SERVICE = "SNEAKY_MORTAR_PARENT_HOOK";
      private final MortarScope parentScope;
      private LayoutInflater inflater;

      static void destroyScope(Context context) {
         MortarScope child = getScope(context);
         child.destroy();
      }

      public TearDownContext(Context context, MortarScope scope) {
         super(scope.createContext(context));
         this.parentScope = getScope(context);
      }

      @Override public Object getSystemService(String name) {
         if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (inflater == null) {
               inflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return inflater;
         }

         if (SERVICE.equals(name)) {
            return parentScope;
         }

         return super.getSystemService(name);
      }
   }
}