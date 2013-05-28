package com.eyeem.chips;

import android.graphics.drawable.Drawable;

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
}
