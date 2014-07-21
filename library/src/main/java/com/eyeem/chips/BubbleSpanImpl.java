package com.eyeem.chips;

import android.graphics.Canvas;
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
      float transY = top - baselineDiff;

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
      float baselineDiff = 0.0f;
      if (et != null) {
         // we do this because for some Androids the first line's bubbles
         // get cut off by inner scroll boundary - we redraw those
         // bubbles after onDraw passes
         // ...also edittext's layouting is erratic and baselineDiff is something
         // I have trouble calculating
         float multipler = -1.8f;
         if (et.getLayout() != null && et.getLayout().getLineForOffset(start) == 0) {
            // especially firstline has bigger next line space than others
            baselineDiff = ((float)bubble.style.bubblePadding) * multipler;
         } else {
            multipler += 1.0f;
            baselineDiff = ((float)bubble.style.bubblePadding) * multipler;
         }
      } else {
         // this is for ChipsTextView which base on StaticLayout... much less
         // troubles here
         baselineDiff = ((float)bubble.style.bubblePadding) * 0.6f;
      }
      return baselineDiff;
   }
}
