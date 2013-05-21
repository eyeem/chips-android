package com.eyeem.chips;

import android.graphics.*;
import android.graphics.drawable.Drawable;

import android.text.TextPaint;

public class BubbleDrawable extends Drawable {

   AwesomeBubble bubble;
   int fakeHeight;

   public BubbleDrawable(int fakeHeight, String text, AwesomeBubbles.BubbleStyle bubbleStyle, int maxWidth, TextPaint textPaint) {
      this.fakeHeight = fakeHeight;
      bubble = new AwesomeBubble(text, maxWidth, bubbleStyle, textPaint);
      setBounds(0, 0, (int)fakeHeight, (int)bubble.getWidth());
   }


   @Override
   public int getIntrinsicHeight() {
      return fakeHeight;
   }

   @Override
   public int getIntrinsicWidth() {
      return bubble.getWidth();
   }

   @Override
   public void draw(Canvas canvas) {
      canvas.save();
      bubble.draw(canvas);
      canvas.restore();
   }

   @Override
   public void setAlpha(int alpha) {
      //
   }

   @Override
   public void setColorFilter(ColorFilter cf) {
      //
   }

   @Override
   public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
   }
}
