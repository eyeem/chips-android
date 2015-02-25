package com.eyeem.chips;


import android.content.Context;
import android.graphics.*;
import android.text.TextPaint;

public class CursorDrawable {

   private final Paint paint;
   ChipsEditText editText;
   float textSize;
   float cursorWidth;
   AwesomeBubble bubble;
   public int color;

   public CursorDrawable(ChipsEditText editText, float textSize, float cursorWidth, Context context) {
      this.editText = editText;
      this.paint = new Paint();

      paint.setAntiAlias(true);
      paint.setFakeBoldText(true);
      paint.setStyle(Paint.Style.FILL);
      paint.setTextAlign(Paint.Align.LEFT);
      this.textSize = textSize;
      this.cursorWidth = cursorWidth;

      BubbleStyle bubbleStyle = editText.getCurrentBubbleStyle();
      bubble = new AwesomeBubble(" ", 100, bubbleStyle, new TextPaint());
      color = editText.getTextColors().getDefaultColor();
   }

   public void draw(Canvas canvas, boolean blink) {
      Point p = editText.getCursorPosition();
      canvas.save();
      canvas.translate(p.x, p.y - bubble.getHeight() + bubble.style.bubblePadding + bubble.baselineHeight());
      if (editText.manualModeOn) {
         // calculate cursor offset
         int x_offset = 0;
         int y_offset = bubble.style.bubblePadding;
         int y_h = bubble.getHeight() - 2 * bubble.style.bubblePadding;
         if (editText.manualStart == editText.getSelectionStart()) { // empty bubble case
            // draw bubble behind
            bubble.draw(canvas);
            x_offset = - bubble.getWidth()/2;
         } else {
            x_offset = 2 * bubble.style.bubblePadding;
         }

         // draw cursor inside
         if (blink) {
            paint.setColor(bubble.style.textColor);
            canvas.drawRect(0 - x_offset, y_offset, cursorWidth - x_offset, y_offset + y_h, paint);
         }
      } else if (blink) {
         paint.setColor(color);
         canvas.drawRect(0, 0, cursorWidth, textSize, paint);
      }
      canvas.restore();
   }

   public Point bubble_offset() {
      int x_offset = 0;
      int y_offset = 0;
      if (editText.manualModeOn) {
         if (editText.manualStart == editText.getSelectionStart()) { // empty bubble case
            x_offset = -bubble.getWidth() / 2;
         } else {
            x_offset = 2 * bubble.style.bubblePadding;
         }
      }
      return new Point(x_offset, y_offset);
   }

   public void setColor(int color) {
      this.color = color;
   }
}