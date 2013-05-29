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
public class ChipsTextView extends View {

   BubbleSpan selectedSpan;
   ArrayList<BubbleSpan> spans = new ArrayList<BubbleSpan>();
   HashMap<BubbleSpan, Rect> positions = new HashMap<BubbleSpan, Rect>();
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
            if (selectedSpan != null) selectedSpan.bubble.setPressed(boundsOf(selectedSpan).contains(x,y));
            break;
         case MotionEvent.ACTION_UP:
            if (listener != null && selectedSpan != null && boundsOf(selectedSpan).contains(x,y)) {
               listener.onBubbleClicked(this, selectedSpan);
            }
            // fall through
         case MotionEvent.ACTION_CANCEL:
            if (selectedSpan != null) selectedSpan.bubble.setPressed(false);
            selectedSpan = null;
            break;

         default:
            break;
      }
      invalidate();
      return true;
   }

   Rect boundsOf(BubbleSpan span) {
      return positions.get(span);
   }

   public void selectBubble(int x, int y) {
      for (BubbleSpan span : spans) {
         if (boundsOf(span).contains(x, y)) {
            span.bubble.setPressed(true);
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
      requestLayout();
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
      int height = layout == null ? 0 : layout.getHeight() + getPaddingTop();

      this.setMeasuredDimension(width, height);
   }

   private void build(int width) {
      if (width == 0 || TextUtils.isEmpty(text))
         layout = null;
      // render + save positions of bubbles
      // TODO rebuild bubbles
      layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.25f, 1, false);
      // add bubbles from the text and create positions for them
      int paddingLeft = getPaddingLeft();
      int paddingTop = getPaddingTop();
      for (BubbleSpan span : spans) {
         int start = ((Spannable)layout.getText()).getSpanStart(span);
         Point startPoint = getCursorPosition(start);
         Rect position = new Rect(span.bubble.rect());
         position.offset(startPoint.x+paddingLeft, startPoint.y+paddingTop);
         positions.put(span, position);
      }
   }

   private Point getCursorPosition(int pos) {
      int line = layout.getLineForOffset(pos);
      int baseline = layout.getLineBaseline(line);
      int ascent = layout.getLineAscent(line);
      float x = layout.getPrimaryHorizontal(pos);
      float y = baseline + ascent;
      return new Point((int)x, (int)y);
   }

   public interface OnBubbleClickedListener {
      public void onBubbleClicked(View view, BubbleSpan bubbleSpan);
   }

   public void setOnBubbleClickedListener(OnBubbleClickedListener listener) {
      this.listener = listener;
   }
}
