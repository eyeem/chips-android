package com.eyeem.chips;


import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

public class CursorDrawable {

   private final Paint paint;
   ChipsEditText editText;
   float textSize;
   float cursorWidth;
   AwesomeBubble bubble;

   public CursorDrawable(ChipsEditText editText, float textSize, float cursorWidth, Context context) {
      this.editText = editText;
      this.paint = new Paint();

      paint.setAntiAlias(true);
      paint.setFakeBoldText(true);
      paint.setStyle(Paint.Style.FILL);
      paint.setTextAlign(Paint.Align.LEFT);
      this.textSize = textSize;
      this.cursorWidth = cursorWidth;
      bubble = new AwesomeBubble(" ", 100, DefaultBubbles.get(DefaultBubbles.LILA, context), new TextPaint());
   }

   public void draw(Canvas canvas, boolean blink) {
      Point p = editText.getCursorPosition();
      canvas.save();
      canvas.translate(p.x, p.y);
      if (editText.manualModeOn) {
         // calculate cursor offset
         int x_offset = 0;
         int y_offset = bubble.style.bubblePadding;
         int y_h = bubble.getHeight() - 2*bubble.style.bubblePadding;
         canvas.translate(0, -BubbleSpanImpl.lineCorrectionLogic(editText.getSelectionStart(), editText, bubble, null));
         if (editText.manualStart == editText.getSelectionStart()) { // empty bubble case
            // draw bubble behind
            bubble.draw(canvas);
            x_offset = - bubble.getWidth()/2;
         } else {
            x_offset = 2*bubble.style.bubblePadding;
         }

         // draw cursor inside
         if (blink) {
            paint.setColor(0xffffffff);
            canvas.drawRect(0 - x_offset, y_offset, cursorWidth - x_offset, y_offset + y_h, paint);
         }
      } else if (blink) {
         paint.setColor(0x88000000);
         canvas.drawRect(0, 0, cursorWidth, textSize, paint);
      }
      canvas.restore();
   }
}