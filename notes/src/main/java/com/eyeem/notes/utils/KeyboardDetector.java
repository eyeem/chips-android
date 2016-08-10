package com.eyeem.notes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by vishna on 10/08/16.
 */
public class KeyboardDetector implements ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnPreDrawListener {

   public final static String TAG = KeyboardDetector.class.getSimpleName();

   private static final int DP_KEYBOARD_THRESHOLD = 60;
   private int keyboardThreshold;

   private int currentHeight;
   private View view;
   private boolean isKeyboardShown = false;
   private KeyboardListener nonFinalListener;

   public void attachToView(View view, KeyboardListener listener) {
      nonFinalListener = listener;
      if (nonFinalListener == null) return;

      if (Build.VERSION.SDK_INT >= 19) {
         Activity activity = findInContext(view.getContext());
         if (activity == null) {
            return;
         }
         view = activity.getWindow().getDecorView();
      }

      keyboardThreshold = (int) TypedValue.applyDimension(
         TypedValue.COMPLEX_UNIT_DIP, DP_KEYBOARD_THRESHOLD, view.getResources().getDisplayMetrics());

      this.view = view;
      currentHeight = view.getHeight();
      view.getViewTreeObserver().addOnGlobalLayoutListener(this);

      if (currentHeight <= 0) {
         view.getViewTreeObserver().addOnPreDrawListener(this);
      }
   }

   public void detachFromView() {
      if (view != null) {
         view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
         view.getViewTreeObserver().removeOnPreDrawListener(this);
         view = null;
      }
      nonFinalListener = null;
   }

   @Override
   public void onGlobalLayout() {
      int newHeight = view.getHeight();

      if (Build.VERSION.SDK_INT >= 19) {
         Rect r = new Rect();
         //r will be populated with the coordinates of your view that area still visible.
         view.getWindowVisibleDisplayFrame(r);
         newHeight = r.height();
      }

      if (currentHeight > 0) {
         int diff = newHeight - currentHeight;
         if (diff < -keyboardThreshold) {
            Log.d(TAG, "onGlobalLayout. keyboard is show. height diff = " + -diff);
            // keyboard is show
            isKeyboardShown = true;
            if (nonFinalListener != null) {
               nonFinalListener.onKeyboardShow(-diff);
            }

         } else if (diff > keyboardThreshold) {
            Log.d(TAG, "onGlobalLayout.keyboard is hide.  height diff = " + diff);
            // keyboard is hide
            isKeyboardShown = false;
            if (nonFinalListener != null) {
               nonFinalListener.onKeyboardHide(diff);
            }
         } else {
            Log.v(TAG, "onGlobalLayout. height diff = " + diff);
         }
      }
      currentHeight = newHeight;
   }

   public boolean isKeyboardShown() {
      return isKeyboardShown;
   }

   @Override
   public boolean onPreDraw() {
      currentHeight = view.getHeight();
      view.getViewTreeObserver().removeOnPreDrawListener(this);
      return true;
   }

   public interface KeyboardListener {
      public void onKeyboardShow(int height);

      public void onKeyboardHide(int height);
   }

   public static Activity findInContext(Context context) {
      if (context instanceof Activity) return (Activity) context;
      if (context instanceof ContextWrapper) return findInContext(((ContextWrapper)context).getBaseContext());
      return null;
   }
}