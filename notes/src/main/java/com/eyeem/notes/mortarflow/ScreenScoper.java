package com.eyeem.notes.mortarflow;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import mortar.MortarScope;

import static com.eyeem.notes.mortarflow.Utils.DAGGER_SERVICE;
import static com.eyeem.notes.mortarflow.Utils.createComponent;
import static mortar.MortarScope.findChild;
import static mortar.MortarScope.getScope;

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

      Object parentComponent = parentScope.getService(DAGGER_SERVICE);
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

         childScope = parentScope.buildChild()
            .withService(DAGGER_SERVICE, createComponent(withComponent.value(), dependenciesArray))
            .build(name);
      }
      return childScope;
   }


}
