package com.eyeem.chipsedittextdemo.mortarflow;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

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
    * DynamicModules} or {@link WithComponent} annotation. Note that scopes are also created
    * for unannotated screens.
    */
   public MortarScope getScreenScope(Context context, MortarScope parentScope, final String name,
                                     final Object screen) {

      Object parentComponent = parentScope.getService(DaggerService.SERVICE_NAME);
      MortarScope childScope = findChild(context, name);

      if (childScope == null) {
         WithComponent withComponent = screen.getClass().getAnnotation(WithComponent.class);
         List<Object> dependencies = new ArrayList<>();
         dependencies.add(parentComponent);

         if (screen instanceof DynamicModules) {
            dependencies.addAll(((DynamicModules) screen).dependencies());
         }

         Object[] dependenciesArray = new Object[dependencies.size()];
         dependencies.toArray(dependenciesArray);

         childScope = parentScope.buildChild(name)
            .withService(DaggerService.SERVICE_NAME, createComponent(withComponent.value(), dependenciesArray))
            .build();
      }
      return childScope;
   }


}
