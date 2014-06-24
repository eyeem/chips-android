package com.eyeem.chips;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

import java.util.HashMap;

/**
 * @author vishna
 */
public class DefaultBubbles {
   public static int LILA = 0;
   public static int GRAY = 1;
   public static int GRAY_WHITE_TEXT = 2;
   public static int GREY_EDIT = 3;
   public static int GREEN = 4;
   public static int CITY_COUNTRY = 5;

   private static HashMap<Integer, BubbleStyle[]> defaults = new HashMap<Integer, BubbleStyle[]>();

   public static int v_spacing;
   public static int h_spacing;
   public static int long_bubble_workaround;

   @Deprecated public static BubbleStyle get(int type, Context context) {
      int textSize = context.getResources().getDimensionPixelSize(R.dimen.bubble_text_size);
      if (defaults.get(textSize) == null) {
         defaults.put(textSize, init(context, textSize));
      }
      return defaults.get(textSize)[type];
   }

   public static BubbleStyle get(int type, Context context, int textSize) {
      if (defaults.get(textSize) == null) {
         defaults.put(textSize, init(context, textSize));
      }
      return defaults.get(textSize)[type];
   }

   public static BubbleStyle[] init(Context context, int textSize) {
      context = context.getApplicationContext();
      Resources r = context.getResources();

      int padding = Math.round((float)textSize * (0.05f));
      v_spacing = r.getDimensionPixelSize(R.dimen.bubble_v_spacing);
      h_spacing = r.getDimensionPixelSize(R.dimen.bubble_h_spacing);

      BubbleStyle[] array = new BubbleStyle[] {
         new BubbleStyle(
            context.getResources().getDrawable(R.drawable.lilatext_background_active),
            context.getResources().getDrawable(R.drawable.lilatext_background_pressed),
            textSize, 0xffebf5e0, 0xffebf5e0, padding), // LILA
         new BubbleStyle(
            context.getResources().getDrawable(R.drawable.greybubble_background_edit),
            context.getResources().getDrawable(R.drawable.greybubble_background_edit),
            textSize, Color.WHITE, Color.WHITE, padding), // GRAY
         new BubbleStyle(
            context.getResources().getDrawable(R.drawable.greybubble_background),
            context.getResources().getDrawable(R.drawable.greybubble_background_pressed),
            textSize, Color.WHITE, Color.WHITE, padding), // GRAY_WHITE_TEXT
         new BubbleStyle(
            context.getResources().getDrawable(R.drawable.greybubble_edit),
            context.getResources().getDrawable(R.drawable.greybubble_edit),
            textSize, Color.WHITE, Color.WHITE, padding), // GREY_EDIT
         new BubbleStyle(
            context.getResources().getDrawable(R.drawable.greentext_background_active),
            context.getResources().getDrawable(R.drawable.greentext_background_pressed),
            textSize, 0xffebe0f5, 0xffebe0f5, padding), // GREEN
         new BubbleStyle(
            null, null,
            context.getResources().getDimensionPixelSize(R.dimen.bubble_country_text_size),
            0xffcccccc, 0xff000000, 0, false) // CITY_COUNTRY
      };

      // calculate workaround per device
      TextPaint paint = new TextPaint();
      float _dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
      paint.setAntiAlias(true);
      paint.setTextSize(_dp);
      paint.setColor(Color.BLACK);
      long_bubble_workaround = (int)paint.measureText(" ");
      Log.i("CHIPS", "long_bubble_workaround = "+long_bubble_workaround);

      return array;
   }
}