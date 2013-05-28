package com.eyeem.chips;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * ChipsTextView
 */
public class ChipsTextView extends View {

   AwesomeBubble selectedBubble;
   ArrayList<AwesomeBubble> bubbles = new ArrayList<AwesomeBubble>();
   Spannable text;
   TextPaint textPaint;
   StaticLayout layout;

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
            if (selectedBubble != null) selectedBubble.setPressed(boundsOf(selectedBubble).contains(x,y));
            break;
         case MotionEvent.ACTION_UP:
            if (selectedBubble != null && boundsOf(selectedBubble).contains(x,y)) {
               selectedBubble.action(this);
            }
            // fall through
         case MotionEvent.ACTION_CANCEL:
            if (selectedBubble != null) selectedBubble.setPressed(false);
            selectedBubble = null;
            break;

         default:
            break;
      }
      invalidate();
      return true;
   }

   Rect boundsOf(AwesomeBubble bubble) {
      return new Rect(bubble.rect());
   }

   public void selectBubble(int x, int y) {
      for (AwesomeBubble bubble : bubbles) {
         if (boundsOf(bubble).contains(x, y)) {
            bubble.setPressed(true);
            selectedBubble = bubble;
            return;
         }
      }
   }

   public void setTextPaint(TextPaint textPaint) {
      this.textPaint = textPaint;
   }

   public void setText(Spannable text) {
      this.text = text;
      // TODO save bubbles to array
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
      int height = layout == null ? 0 : layout.getHeight() + getPaddingTop()/2;

      this.setMeasuredDimension(width, height);
   }

   private void build(int width) {
      if (width == 0 || TextUtils.isEmpty(text))
         layout = null;
      // render + save positions of bubbles
      // TODO rebuild bubbles
      layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 2.0f, 1, false);
      // TODO add bubbles from the text and create positions for them
      bubbles.clear();
   }
}
