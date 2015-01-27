package com.eyeem.chipsedittextdemo.mortarflow;


import android.content.Context;
import android.content.res.Resources;

import mortar.Mortar;
import mortar.MortarScope;
import mortar.dagger2support.Dagger2;

import static java.lang.String.format;

/**
 * Creates {@link MortarScope}s for screens that may be annotated with {@link WithComponent},
 * {@link WithComponent}.
 */
public class ScreenScoper {

   // TODO private final Map<Class, ComponentFactory> moduleFactoryCache = new LinkedHashMap<>();

   public MortarScope getScreenScope(Context context, String name, Object screen) {
      MortarScope parentScope = Mortar.getScope(context);
      return getScreenScope(context.getResources(), parentScope, name, screen);
   }

   /**
    * Finds or creates the scope for the given screen, honoring its optional {@link
    * WithComponentFactory} or {@link WithComponent} annotation. Note that scopes are also created
    * for unannotated screens.
    */
   public MortarScope getScreenScope(Resources resources, MortarScope parentScope, final String name,
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
      Object parentGraph = parentScope.getObjectGraph();
      MortarScope childScope = parentScope.findChild(name);
      if (childScope == null) {
         childScope = parentScope.createChild(name, Dagger2.buildComponent(withComponent.value(), parentGraph));
      }
      return childScope;
   }
}
