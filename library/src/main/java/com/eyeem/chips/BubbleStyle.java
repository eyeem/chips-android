package com.eyeem.chips;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
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

   public static BubbleStyle build(Context context, int styleId) {
      return build(context, styleId, context.getResources().getDimension(R.dimen.default_text_size));
   }

   public static BubbleStyle build(ChipsTextView textView, int styleId) {
      return build(textView.getContext(), styleId, textView.getTextSize()); // TODO: what's not deprecated?
   }

   public static BubbleStyle build(ChipsEditText editText, int styleId) {
      return build(editText.getContext(), styleId, editText.getTextSize());
   }

   private static final int[] ATTRS = {
      R.attr.bubbleStateActive,
      R.attr.bubbleStatePressed,
      R.attr.bubbleTextSize,
      R.attr.bubbleTextColor,
      R.attr.bubbleTextColorActive,
      R.attr.bubbleTextPadding
   };

   private static BubbleStyle build(Context context, int styleId, float textSize) {


      context = new ContextThemeWrapper(context, styleId);

      Drawable temp;
      TypedArray ta = context.obtainStyledAttributes(styleId, ATTRS);
      temp = ta.getDrawable(0);
      Drawable active = temp != null ? temp : context.getResources().getDrawable(R.drawable.greybubble_background);
      temp = ta.getDrawable(1);
      Drawable pressed = temp != null ? temp : context.getResources().getDrawable(R.drawable.greybubble_background_pressed);
      float size = ta.getDimension(2, textSize);
      int color = ta.getColor(3, context.getResources().getColor(R.color.default_text_color));
      int colorActive = ta.getColor(4, context.getResources().getColor(R.color.default_text_color_active));
      float pad = ta.getDimension(5, context.getResources().getDimension(R.dimen.default_text_pad));

      BubbleStyle bs = new BubbleStyle(
         active,
         pressed,
         (int) size,
         color,
         colorActive,
         (int) pad);

      ta.recycle();
      return bs;
   }

}
