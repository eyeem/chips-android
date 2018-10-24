package com.eyeem.chips;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;

public class BubbleStyle {
   Drawable active;
   Drawable pressed;
   int textSize;
   int textColor;
   int textPressedColor;
   int bubblePadding;
   boolean nextNeedsSpacing;
   Typeface typeface;

   public BubbleStyle(Drawable active, Drawable pressed, int textSize,
                      int textColor, int textPressedColor, int bubblePadding, Typeface tf) {
      this(active, pressed, textSize, textColor, textPressedColor, bubblePadding, true);
      this.typeface = tf;
   }

   public BubbleStyle(Drawable active, Drawable pressed, int textSize,
                      int textColor, int textPressedColor, int bubblePadding) {
      this(active, pressed, textSize, textColor, textPressedColor, bubblePadding, true);
   }

   public BubbleStyle(Drawable active, Drawable pressed, int textSize,
                      int textColor, int textPressedColor, int bubblePadding, boolean nextNeedsSpacing) {
      this.active = active;
      this.pressed = pressed;
      this.textSize = textSize;
      this.textColor = textColor;
      this.textPressedColor = textPressedColor;
      this.bubblePadding = bubblePadding;
      this.nextNeedsSpacing = nextNeedsSpacing;
   }

   private static final int[] ATTRS = {
      R.attr.bubble_stateActive,
      R.attr.bubble_statePressed,
      R.attr.bubble_textSize,
      R.attr.bubble_textColor,
      R.attr.bubble_textColorActive,
      R.attr.bubble_textPadding,
      R.attr.bubble_customFont
   };

   // public static BubbleStyle buildDefault(Context context) {
   //    return build(context, R.style.default_bubble_style);
   // }

   public static BubbleStyle build(Context context, int styleId) {

      Drawable temp;

      context = new ContextThemeWrapper(context.getApplicationContext(), styleId);
      Resources r = context.getResources();
      TypedArray ta = context.obtainStyledAttributes(styleId, ATTRS);
      
      temp = ta.getDrawable(0);
      Drawable active = temp;
      temp = ta.getDrawable(1);
      Drawable pressed = temp;
      float size = ta.getDimension(2, r.getDimension(R.dimen.default_bubble_text_size));
      int color = ta.getColor(3, r.getColor(R.color.default_bubble_text_color));
      int colorActive = ta.getColor(4, r.getColor(R.color.default_bubble_text_color_active));
      float pad = ta.getDimension(5, r.getDimension(R.dimen.default_bubble_text_pad));
      String fontName = ta.getString(6);
      Typeface tf = null;
      if (fontName != null) {
         tf = FontCache.getTypeface(context, fontName);
      }


      BubbleStyle bs = new BubbleStyle(
         active,
         pressed,
         (int) size,
         color,
         colorActive,
         (int) pad,
         tf);

      ta.recycle();
      return bs;
   }

   public void setTextSize(int textSize) {
      this.textSize = textSize;
   }

   public Drawable getActive() {
      return active;
   }

   public void setActive(Drawable active) {
      this.active = active;
   }

   public Drawable getPressed() {
      return pressed;
   }

   public void setPressed(Drawable pressed) {
      this.pressed = pressed;
   }

   public int getTextSize() {
      return textSize;
   }

   public int getTextColor() {
      return textColor;
   }

   public void setTextColor(int textColor) {
      this.textColor = textColor;
   }

   public int getTextPressedColor() {
      return textPressedColor;
   }

   public void setTextPressedColor(int textPressedColor) {
      this.textPressedColor = textPressedColor;
   }

   public int getBubblePadding() {
      return bubblePadding;
   }

   public void setBubblePadding(int bubblePadding) {
      this.bubblePadding = bubblePadding;
   }
}
