package com.eyeem.chips;


import android.graphics.*;
import android.graphics.drawable.Drawable;

public class CursorDrawable extends Drawable {

   private final Paint paint;
   ChipsEditText editText;
   float textSize;
   float cursorWidth;

   public CursorDrawable(ChipsEditText editText, float textSize, float cursorWidth) {
      this.editText = editText;
      this.paint = new Paint();

      paint.setColor(Color.BLACK);
      paint.setAntiAlias(true);
      paint.setFakeBoldText(true);
      paint.setStyle(Paint.Style.FILL);
      paint.setTextAlign(Paint.Align.LEFT);
      this.textSize = textSize;
      this.cursorWidth = cursorWidth;
      setBounds(0, 0, (int)textSize, (int)cursorWidth);
   }


   @Override
   public int getIntrinsicHeight() {
      return (int)textSize;
   }

   @Override
   public int getIntrinsicWidth() {
      return (int)cursorWidth;
   }

   @Override
   public void draw(Canvas canvas) {
      Point p = editText.getInnerCursorPosition();
      canvas.save();
      canvas.translate(p.x, p.y);
      canvas.drawRect(0, 0, cursorWidth, textSize, paint);
      canvas.restore();
   }

   @Override
   public void setAlpha(int alpha) {
      paint.setAlpha(alpha);
   }

   @Override
   public void setColorFilter(ColorFilter cf) {
      paint.setColorFilter(cf);
   }

   @Override
   public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
   }
}