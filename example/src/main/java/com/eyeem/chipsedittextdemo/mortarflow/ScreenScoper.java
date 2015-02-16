package com.eyeem.chipsedittextdemo.mortarflow;


import android.content.Context;
import android.content.res.Resources;

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

   // TODO private final Map<Class, ComponentFactory> moduleFactoryCache = new LinkedHashMap<>();

   public MortarScope getScreenScope(Context context, String name, Object screen) {
      MortarScope parentScope = getScope(context);
      return getScreenScope(context, parentScope, name, screen);
   }

   /**
    * Finds or creates the scope for the given screen, honoring its optional {@link
    * WithComponentFactory} or {@link WithComponent} annotation. Note that scopes are also created
    * for unannotated screens.
    */
   public MortarScope getScreenScope(Context context, MortarScope parentScope, final String name,
                                     final Object screen) {
      WithComponent withComponent = screen.getClass().getAnnotation(WithComponent.class);
//      Object childComponent;
//      if (componentFactory != NO_FACTORY) {
//         childComponent = componentFactory.createDagger2Component(resources, screen);
//      } else {
//         // We need every screen to have a scope, so that anything it injects is scoped.  We need
//         // this even if the screen doesn't declare a module, because Dagger allows injection of
//         // objects that are annotated even if they don't appear in a module.
//         childComponent = null;
//      }
      MortarScope childScope = findChild(context, name);
      Object parentComponent = parentScope.getService(DaggerService.SERVICE_NAME);

      if (childScope == null) {
         childScope = parentScope.buildChild(context, name)
            .withService(DaggerService.SERVICE_NAME, createComponent(withComponent.value(), parentComponent))
            .build();
      }
      return childScope;
   }
}
