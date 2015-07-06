package com.eyeem.chips;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vishna on 05/02/15.
 */
public class LayoutBuild implements ILayoutCallback {
   // TODO some test http://alvinalexander.com/java/jwarehouse/android/core/tests/coretests/src/android/text/StaticLayoutTest.java.shtml

   BubbleSpan selectedSpan;
   ArrayList<BubbleSpan> spans = new ArrayList<BubbleSpan>();
   HashMap<BubbleSpan, ArrayList<Rect>> positions = new HashMap<BubbleSpan, ArrayList<Rect>>();

   final Spannable text;
   final Spannable moreText;

   TextPaint textPaint;
   private StaticLayout truncatedLayout;
   private StaticLayout expandedLayout;
   private boolean truncated;
   float lineSpacing = 1.25f;
   int maxLines = 0;
   boolean debug;
   boolean spansPositioned;

   /**
    * Represents the value request via build() method
    */
   int buildWidth;

   public LayoutBuild(Spannable text, Config config) {
      this.text = text;
      lineSpacing = config.lineSpacing;
      maxLines = config.maxLines;
      textPaint = config.textPaint;
      moreText = config.moreText;
      truncated = config.truncated;
      debug = config.debug;
      buildWidth = 0;
   }

   private StaticLayout layout() {
      return truncated ? truncatedLayout : expandedLayout;
   }

   public int layoutHeight() {
      final StaticLayout l = layout();
      return l != null ? l.getHeight() : 0;
   }

   public int lineCount() {
      final StaticLayout l = layout();
      return l != null ? l.getLineCount() : 0;
   }

   public float lineWidth(int lineNumber) {
      final StaticLayout l = layout();
      return l != null ? l.getLineWidth(lineNumber) : 0;
   }

   public boolean isSingleLine() {
      return lineCount() == 1;
   }

   public int expansionHeight() {
      return expandedLayout.getHeight() - truncatedLayout.getHeight();
   }

   public int getBuildWidth() {
      return buildWidth;
   }

   public boolean spanContains(BubbleSpan span, int x, int y) {
      ArrayList<Rect> rects = positions.get(span);
      if (rects != null)
         for (Rect rect : rects) {
            if (rect.contains(x, y))
               return true;
         }
      return false;
   }

   public boolean onTouchEvent(int action, int x, int y, ChipsTextView.OnBubbleClickedListener listener, View view) {
      boolean retValue = false;

      if (!spansPositioned) {
         positionSpans();
      }

      switch (action) {
         case MotionEvent.ACTION_DOWN:
            retValue = selectBubble(x, y);
            break;
         case MotionEvent.ACTION_MOVE:
            if (selectedSpan != null) {
               selectedSpan.setPressed(spanContains(selectedSpan, x, y), (Spannable) layout().getText());
               retValue = true;
            }
            break;
         case MotionEvent.ACTION_UP:
            if (listener != null && selectedSpan != null && spanContains(selectedSpan, x, y)) {
               listener.onBubbleClicked(view, selectedSpan);
            }
            // fall through
         case MotionEvent.ACTION_CANCEL:
            if (selectedSpan != null) {
               retValue = true;
               // we might be returning to a different layout
               if ( -1 != ((Spannable)truncatedLayout.getText()).getSpanEnd(selectedSpan)) {
                  selectedSpan.setPressed(false, (Spannable)truncatedLayout.getText());
               }
               if ( -1 != ((Spannable)expandedLayout.getText()).getSpanEnd(selectedSpan)) {
                  selectedSpan.setPressed(false, (Spannable)expandedLayout.getText());
               }
            }
            selectedSpan = null;
            break;

         default:
            break;
      }
      return retValue;
   }

   public boolean selectBubble(int x, int y) {
      for (BubbleSpan span : spans) {
         if (spanContains(span, x, y)) {
            span.setPressed(true, (Spannable)layout().getText());
            selectedSpan = span;
            return true;
         }
      }
      return false;
   }

   private void resetSpans(int width) {
      final Spannable text = getSpannable();
      if (text == null) return;
      for (BubbleSpan span : text.getSpans(0, text.length(), BubbleSpan.class)) {
         span.resetWidth(width);
      }
   }

   private void recomputeSpans(final Spannable text) {
      spans.clear();
      for (BubbleSpan span : text.getSpans(0, text.length(), BubbleSpan.class)) {
         spans.add(span);
      }
   }

   public void draw(Canvas canvas) {
      if (layout() == null) return;
      layout().draw(canvas);
      if (debug) {
         int n = layout().getLineCount();
         for (int i = 0; i < n; i++) {
            Paint paint = new Paint();
            paint.setColor(Color.RED); // red
            paint.setStyle(Paint.Style.STROKE);
            Rect bounds = new Rect();
            layout().getLineBounds(i, bounds);
            canvas.drawRect(bounds, paint);

            //drawLine(canvas, bounds, paint, bounds.top + (int)layout.getPaint().getTextSize(), Color.BLUE);
            int baseLine = layout().getLineBaseline(i);
            drawLine(canvas, bounds, paint, baseLine, Color.GREEN);
            drawLine(canvas, bounds, paint, baseLine + layout().getPaint().getFontMetricsInt().descent, Color.BLUE);
         }
      }
   }

   public void build(int width) {
      build(width, true);
   }

   /* package */ void build(int width, boolean positionSpans) {
      this.buildWidth = width;
      this.spansPositioned = false;
      if (width <= 0 || TextUtils.isEmpty(text)) {
         truncatedLayout = expandedLayout = null;
         return;
      }
      // render + save positions of bubbles
      resetSpans(width);
      try {
         truncatedLayout = expandedLayout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, lineSpacing, 1, false);
         if (maxLines > 0 && truncatedLayout.getLineCount() > maxLines) {
            int lineEnd = truncatedLayout.getLineEnd(maxLines - 1);

            // ... more
            int offset = -1;
            StaticLayout sl = new StaticLayout(moreText, textPaint, width, Layout.Alignment.ALIGN_NORMAL, lineSpacing, 1, false);
            sl.getWidth();
            while (truncatedLayout.getLineCount() > maxLines && lineEnd > 0) {
               if (offset == -1 && truncatedLayout.getLineWidth(maxLines - 1) + sl.getLineWidth(0) > width) { // means we also need to truncate last line
                  offset = truncatedLayout.getOffsetForHorizontal(maxLines - 1, width - sl.getLineWidth(0));
                  lineEnd = offset;
               } else if (offset > 0) {
                  lineEnd--;
               }

               SpannableStringBuilder textTruncated = new SpannableStringBuilder(text.subSequence(0, lineEnd));
               textTruncated.append(moreText);
               truncatedLayout = new StaticLayout(textTruncated, textPaint, width, Layout.Alignment.ALIGN_NORMAL, lineSpacing, 1, false);
            }
         }
      } catch (java.lang.ArrayIndexOutOfBoundsException e) {
         // sometimes java.lang.ArrayIndexOutOfBoundsException happens here, seems to be jelly bean bug
         // workaround is too expensive to implement https://gist.github.com/pyricau/3424004
         return; // layout stays null, we show nothing (h == 0)
      }

      // add bubbles from the text and create positions for them
      if (positionSpans) {
         positionSpans();
      }
   }

   private void positionSpans() {
      recomputeSpans(getSpannable());
      positions.clear();
      for (BubbleSpan span : spans) {
         positions.put(span, span.rect(this));
      }
      spansPositioned = true;
   }

   @Override public Point getCursorPosition(int pos) {
      if (pos < 0 || pos > layout().getText().length()) return null;
      int line = layout().getLineForOffset(pos);
      int baseline = layout().getLineBaseline(line);
      int ascent = layout().getLineAscent(line);
      // Call position outside bounds and kill the thread yo!
      float x = layout().getPrimaryHorizontal(pos);
      float y = baseline + ascent;
      return new Point((int)x, (int)y);
   }

   @Override public int getLine(int pos) {
      return layout().getLineForOffset(pos);
   }

   @Override public Spannable getSpannable() {
      return (truncatedLayout != null && truncated) ? (Spannable) truncatedLayout.getText() : text;
   }

   @Override public int getLineEnd(int line) {
      return layout().getLineEnd(line);
   }

   @Override public int getLineHeight() {
      return layout().getLineBottom(0);
   }

   private void drawLine(Canvas canvas, Rect line, Paint paint, int height, int color) {
      paint.setColor(color);
      canvas.drawLine(line.left, height, line.right, height, paint);
   }

   public void setTruncated(boolean truncated) {
      if (this.truncated == truncated) return;
      this.truncated = truncated;
      positionSpans();
   }

   public boolean isTruncated() {
      return truncated;
   }

   public static class Config {
      public TextPaint textPaint;
      public float lineSpacing = 1.25f;
      public Spannable moreText;
      public int maxLines = 0;
      public boolean truncated;
      public boolean debug;
      public boolean clickable = true;
   }
}
