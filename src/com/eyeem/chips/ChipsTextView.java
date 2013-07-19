package com.eyeem.chips;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ChipsTextView
 */
public class ChipsTextView extends View implements ILayoutCallback {

   BubbleSpan selectedSpan;
   ArrayList<BubbleSpan> spans = new ArrayList<BubbleSpan>();
   HashMap<BubbleSpan, ArrayList<Rect>> positions = new HashMap<BubbleSpan, ArrayList<Rect>>();
   Spannable text;
   TextPaint textPaint;
   StaticLayout layout;
   OnBubbleClickedListener listener;

   public ChipsTextView(Context context) {
      super(context);
   }

   public ChipsTextView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public ChipsTextView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      int action = event.getAction();
      int x = (int) event.getX();
      int y = (int) event.getY();

      x += getScrollX();
      y += getScrollY();

      switch (action) {
         case MotionEvent.ACTION_DOWN:
            selectBubble(x, y);
            break;
         case MotionEvent.ACTION_MOVE:
            if (selectedSpan != null) selectedSpan.setPressed(spanContains(selectedSpan, x, y), (Spannable)layout.getText());
            break;
         case MotionEvent.ACTION_UP:
            if (listener != null && selectedSpan != null && spanContains(selectedSpan, x, y)) {
               listener.onBubbleClicked(this, selectedSpan);
            }
            // fall through
         case MotionEvent.ACTION_CANCEL:
            if (selectedSpan != null) selectedSpan.setPressed(false, (Spannable)layout.getText());
            selectedSpan = null;
            break;

         default:
            break;
      }
      invalidate();
      return true;
   }

   boolean spanContains(BubbleSpan span, int x, int y) {
      ArrayList<Rect> rects = positions.get(span);
      if (rects != null)
         for (Rect rect : rects) {
            if (rect.contains(x, y))
               return true;
         }
      return false;
   }

   public void selectBubble(int x, int y) {
      for (BubbleSpan span : spans) {
         if (spanContains(span, x, y)) {
            span.setPressed(true, (Spannable)layout.getText());
            selectedSpan = span;
            return;
         }
      }
   }

   public void setTextPaint(TextPaint textPaint) {
      this.textPaint = textPaint;
   }

   public void setText(Spannable text) {
      this.text = text;
      // save bubbles to array
      spans.clear();
      for (BubbleSpan span : text.getSpans(0, text.length(), BubbleSpan.class)) {
         spans.add(span);
      }
      if (getWidth() > 0) {
         requestLayout();
      }
   }

   @Override
   protected void onDraw(Canvas canvas) {
      if (layout != null) {
         canvas.translate(getPaddingLeft(), getPaddingTop());
         layout.draw(canvas);
      }
   }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);

      int width = widthSize;
      build(width);
      int height = layout == null ? 0 : layout.getHeight() + getPaddingTop() + getPaddingBottom();

      this.setMeasuredDimension(width, height);
   }

   private void build(int width) {
      positions.clear();
      width = width - getPaddingLeft() - getPaddingRight();
      if (width <= 0 || TextUtils.isEmpty(text)) {
         layout = null;
         return;
      }
      // render + save positions of bubbles
      for (BubbleSpan span : spans) {
         span.resetWidth(width);
      }
      layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.25f, 1, false);
      // add bubbles from the text and create positions for them
      for (BubbleSpan span : spans) {
         positions.put(span, span.rect(this));
      }
   }

   @Override
   public Point getCursorPosition(int pos) {
      int line = layout.getLineForOffset(pos);
      int baseline = layout.getLineBaseline(line);
      int ascent = layout.getLineAscent(line);
      float x = layout.getPrimaryHorizontal(pos);
      float y = baseline + ascent;
      return new Point((int)x+getPaddingLeft(), (int)y+getPaddingTop());
   }

   @Override
   public int getLine(int pos) {
      return layout.getLineForOffset(pos);
   }

   @Override
   public Spannable getSpannable() {
      return ((Spannable)layout.getText());
   }

   @Override
   public int getLineEnd(int line) {
      return layout.getLineEnd(line);
   }

   @Override
   public int getLineHeight() {
      return layout.getLineBottom(0);
   }

   public interface OnBubbleClickedListener {
      public void onBubbleClicked(View view, BubbleSpan bubbleSpan);
   }

   public void setOnBubbleClickedListener(OnBubbleClickedListener listener) {
      this.listener = listener;
   }
}
