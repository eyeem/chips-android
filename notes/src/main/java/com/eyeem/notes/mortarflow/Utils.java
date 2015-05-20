package com.eyeem.notes.mortarflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.reflect.Method;

import flow.path.Path;

/**
 * Created by vishna on 29/01/15.
 */
public class Utils {

   public final static String DAGGER_SERVICE = "DaggerService";

   public interface OnMeasuredCallback {
      void onMeasured(View view, int width, int height);
   }

   public static void waitForMeasure(final View view, final OnMeasuredCallback callback) {
      int width = view.getWidth();
      int height = view.getHeight();

      if (width > 0 && height > 0) {
         callback.onMeasured(view, width, height);
         return;
      }

      view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
         @Override public boolean onPreDraw() {
            final ViewTreeObserver observer = view.getViewTreeObserver();
            if (observer.isAlive()) {
               observer.removeOnPreDrawListener(this);
            }

            callback.onMeasured(view, view.getWidth(), view.getHeight());

            return true;
         }
      });
   }

   /**
    * Magic method that creates a component with its dependencies set, by reflection. Relies on
    * Dagger2 naming conventions.
    */
   public static <T> T createComponent(Class<T> componentClass, Object... dependencies) {
      String fqn = componentClass.getName();

      String packageName = componentClass.getPackage().getName();
      // Accounts for inner classes, ie MyApplication$Component
      String simpleName = fqn.substring(packageName.length() + 1);
      String generatedName = (packageName + ".Dagger" + simpleName).replace('$', '_');

      try {
         Class<?> generatedClass = Class.forName(generatedName);
         Object builder = generatedClass.getMethod("builder").invoke(null);

         for (Method method : builder.getClass().getMethods()) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1) {
               Class<?> dependencyClass = params[0];
               for (Object dependency : dependencies) {
                  if (dependencyClass.isAssignableFrom(dependency.getClass())) {
                     method.invoke(builder, dependency);
                     break;
                  }
               }
            }
         }
         //noinspection unchecked
         return (T) builder.getClass().getMethod("build").invoke(builder);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Caller is required to know the type of the component for this context.
    */
   @SuppressWarnings("unchecked") //
   public static <T> T getDaggerComponent(Context context) {
      return (T) context.getSystemService(DAGGER_SERVICE);
   }

   public static int getLayout(Path path) {
      Layout layout = path.getClass().getAnnotation(Layout.class);
      return layout.value();
   }

   public final static class Layouts {

      /**
       * Create an instance of the view specified in a {@link Layout} annotation.
       */
      public static android.view.View createView(Context context, Object screen) {
         return createView(context, screen.getClass());
      }

      /**
       * Create an instance of the view specified in a {@link Layout} annotation.
       */
      public static android.view.View createView(Context context, Class<?> screenType) {
         Layout screen = screenType.getAnnotation(Layout.class);
         if (screen == null) {
            throw new IllegalArgumentException(
               String.format("@%s annotation not found on class %s", Layout.class.getSimpleName(),
                  screenType.getName()));
         }

         int layout = screen.value();
         return inflateLayout(context, layout);
      }

      private static android.view.View inflateLayout(Context context, int layoutId) {
         return LayoutInflater.from(context).inflate(layoutId, null);
      }

      private Layouts() {}
   }

   private Utils() {}
}
