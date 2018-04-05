package com.eyeem.chips;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dario on 03/04/2018.
 */

public class FontCache {

   private static Map<String, Typeface> cache = new HashMap<>();

   public static Typeface getTypeface(Context context, String assetPath) {
      if (!cache.containsKey(assetPath)) {
         Typeface tf = Typeface.createFromAsset(context.getAssets(), assetPath);
         cache.put(assetPath, tf);
      }
      return cache.get(assetPath);
   }
   
}
