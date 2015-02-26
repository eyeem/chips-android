package com.eyeem.chips;

import android.graphics.*;
import android.text.*;
import android.text.style.StyleSpan;

public class AwesomeBubble {
   String text;
   private Rect rect;
   StaticLayout textLayout;
   boolean isPressed;
   BubbleStyle style;
   TextPaint text_paint;
   int containerWidth = 0;

   public AwesomeBubble (String text, int containerWidth, BubbleStyle style, TextPaint text_paint) {
      this.style = style;
      this.text_paint = text_paint;
      this.text = text;
      if (containerWidth > 0) {
         resetWidth(containerWidth);
      }
   }

   public AwesomeBubble resetWidth(int containerWidth) {
      containerWidth -= DefaultBubbles.long_bubble_workaround;
      if (this.containerWidth == containerWidth)
         return this;
      this.containerWidth = containerWidth;
      text_paint.setTextSize(style.textSize);

      int correction = 0;
      if (android.os.Build.VERSION.SDK_INT >= 18) {
         // so with 4.3, StaticLayout.getDesiredWidth started giving bad results
         // adding 1px helps
         correction = 1;
      }

      int maximum_w = containerWidth - 4 * style.bubblePadding;
      int desired_w = (int)StaticLayout.getDesiredWidth(text, text_paint) + correction;
      int best_w = Math.max(Math.min(maximum_w, desired_w), 0);
      textLayout = new StaticLayout(text, text_paint, best_w, Layout.Alignment.ALIGN_CENTER, 1.0f, 1, false);
      if (desired_w > maximum_w) {
         makeOneLiner(maximum_w);
      }
      setPosition(0, 0);
      return this;
   }

   public AwesomeBubble makeOneLiner(int width) {
      text_paint.setTextSize(style.textSize);
      int i = text_paint.breakText(textLayout.getText().toString(), true, (float)width, null);
      while (i > 1 && textLayout.getLineCount() > 1) {
         textLayout = new StaticLayout(textLayout.getText().subSequence(0, i - 1) + "\u2026",
            text_paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
         i--;
      }
      return this;
   }

   public int getWidth() {
      return textLayout == null ? 0 : textLayout.getWidth() + 4 * style.bubblePadding;
   }

   public int getHeight() {
      return textLayout == null ? 0 : textLayout.getHeight() + 2 * style.bubblePadding;
   }

   public void setPosition(int x, int y) {
      rect = new Rect(x, y, x + getWidth(), y + getHeight());
   }

   public void draw(Canvas canvas) {
      if (textLayout == null)
         return;
      if (isPressed && style.pressed != null) {
         style.pressed.setBounds(rect);
         style.pressed.draw(canvas);
      } else if (!isPressed && style.active != null){
         style.active.setBounds(rect);
         style.active.draw(canvas);
      }
      canvas.translate(rect.left + 2 * style.bubblePadding, rect.top + style.bubblePadding);
      text_paint.setTextSize(style.textSize);
      text_paint.setColor(isPressed ? style.textPressedColor : style.textColor);
      text_paint.setAntiAlias(true);
      textLayout.draw(canvas);
      canvas.translate(-rect.left - 2 * style.bubblePadding, - rect.top - style.bubblePadding);
   }

   public void setPressed(boolean value) {
      this.isPressed = value;
   }

   public String text() {
      return text;
   }

   public Rect rect() {
      return rect;
   }

   /**
    * distance between baseline and text bottom
    * @return
    */
   public int baselineHeight() {
      return textLayout == null ? 0 : textLayout.getHeight() - textLayout.getLineBaseline(0);
   }
}
