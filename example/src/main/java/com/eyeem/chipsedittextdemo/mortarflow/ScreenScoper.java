package com.eyeem.chipsedittextdemo.mortarflow;


import android.content.Context;
import android.content.res.Resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import mortar.MortarScope;
import mortar.dagger2support.DaggerService;

import static mortar.MortarScope.findChild;
import static mortar.MortarScope.getScope;
import static mortar.dagger2support.DaggerService.createComponent;

/**
 * Creates {@link MortarScope}s for screens that may be annotated with {@link WithComponent},
 * {@link WithComponent}.
 */
public class ScreenScoper {

   public MortarScope getScreenScope(Context context, String name, Object screen) {
      MortarScope parentScope = getScope(context);
      return getScreenScope(context, parentScope, name, screen);
   }

   /**
    * Finds or creates the scope for the given screen, honoring its optional {@link
    * ComponentFactory} or {@link WithComponent} annotation. Note that scopes are also created
    * for unannotated screens.
    */
   public MortarScope getScreenScope(Context context, MortarScope parentScope, final String name,
                                     final Object screen) {

      Object parentComponent = parentScope.getService(DaggerService.SERVICE_NAME);
      MortarScope childScope = findChild(context, name);

      if (childScope == null && screen instanceof ComponentFactory) {

         Object childComponent = ((ComponentFactory) screen).createDaggerComponent(context, parentComponent);
         childScope = parentScope.buildChild(name)
            .withService(DaggerService.SERVICE_NAME, childComponent)
            .build();

      }

      if (childScope == null) {
         WithComponent withComponent = screen.getClass().getAnnotation(WithComponent.class);
         childScope = parentScope.buildChild(name)
            .withService(DaggerService.SERVICE_NAME, createComponent(withComponent.value(), parentComponent))
            .build();
      }
      return childScope;
   }


}
