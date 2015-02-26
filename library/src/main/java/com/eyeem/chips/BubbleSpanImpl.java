package com.eyeem.chips;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ReplacementSpan;

import java.util.ArrayList;

public class BubbleSpanImpl extends ReplacementSpan implements BubbleSpan {
   public Object data;
   public AwesomeBubble bubble;
   ChipsEditText et;
   int start;
   float baselineDiff;

   public BubbleSpanImpl(AwesomeBubble bubble) {
      this.bubble = bubble;
   }

   public BubbleSpanImpl(AwesomeBubble bubble, ChipsEditText et) {
      this.bubble = bubble;
      this.et = et;
   }

   @Override
   public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
      this.start = start;
      canvas.save();

      baselineDiff = lineCorrectionLogic(start, et, bubble, this);
      float transY = y - baselineDiff;

      canvas.translate(x, transY);
      bubble.draw(canvas);
      canvas.restore();
   }

   @Override
   public void redraw(Canvas canvas) {
      if (et.getScrollY() != 0)
         return;

      int pos = et.getText().getSpanStart(this);
      if (pos == -1)
         return;
      Layout layout = et.getLayout();
      int line = layout.getLineForOffset(pos);
      float x = layout.getPrimaryHorizontal(pos);
      float y = layout.getLineTop(line);
      x += et.getPaddingLeft();
      y += et.getPaddingTop();

      canvas.save();
      canvas.translate(x, y - baselineDiff);
      bubble.draw(canvas);
      canvas.restore();
   }

   @Override
   public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
      return bubble.getWidth();
   }

   @Override
   public void setPressed(boolean value, Spannable s) {
      bubble.setPressed(value);
   }

   @Override
   public void resetWidth(int width) {
      bubble.resetWidth(width);
   }

   @Override
   public ArrayList<Rect> rect(ILayoutCallback callback) {
      ArrayList<Rect> result = new ArrayList<Rect>();
      Rect position = new Rect(bubble.rect());
      int spanStart = callback.getSpannable().getSpanStart(this);
      Point startPoint = callback.getCursorPosition(spanStart);
      if (startPoint == null) return result;
      position.offset(startPoint.x, startPoint.y);
      result.add(position);
      return result;
   }

   @Override
   public void setData(Object data) {
      this.data = data;
   }

   @Override
   public Object data() {
      return data;
   }

   public static float lineCorrectionLogic(int start, ChipsEditText et, AwesomeBubble bubble, BubbleSpanImpl span) {
      return (bubble.getHeight() - bubble.style.bubblePadding - bubble.baselineHeight());
   }
}
