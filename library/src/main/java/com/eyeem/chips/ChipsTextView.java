package com.eyeem.chips;

import android.content.Context;
import android.graphics.Canvas;
import android.text.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * ChipsTextView
 */
public class ChipsTextView extends View {

   public static boolean DEBUG;

   OnBubbleClickedListener listener;

   // default config
   boolean animating;
   LayoutBuild.Config _defaultConfig;

   public ChipsTextView(Context context) {
      super(context);
   }

   public ChipsTextView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public ChipsTextView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override public boolean onTouchEvent(MotionEvent event) {
      final LayoutBuild layoutBuild = getLayoutBuild();
      if (layoutBuild == null) return false;
      int action = event.getAction();
      int x = (int) event.getX();
      int y = (int) event.getY();

      x += getScrollX();
      y += getScrollY();

      // TODO probably needs an offset by padding left,top
      x -= getPaddingLeft();
      y -= getPaddingTop();

      boolean retValue = layoutBuild.onTouchEvent(action, x, y, listener, this);
      invalidate();
      return retValue;
   }

   public void setText(final Spannable text) {
      if (text == null) {
         setLayoutBuild(null);
         return;
      }

      final LayoutBuild currentLayout = getLayoutBuild();

      if (currentLayout != null && text.equals(currentLayout.getSpannable())) return;

      doBuild(text);
   }

   public boolean buildInProgress;

   private void doBuild(final Spannable text) {
      if (buildInProgress) return;
      buildInProgress = true;

      if (maxAvailableWidth > 0) {
         LayoutBuild futureLayout = new LayoutBuild(text, defaultConfig());
         futureLayout.build(maxAvailableWidth);
         setLayoutBuild(futureLayout);
         buildInProgress = false;
      } else {
         addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
               removeOnLayoutChangeListener(this);
               // otherwise layouting gets discarded
               post(new Runnable() {
                  @Override public void run() {
                     buildInProgress = false;
                     doBuild(text);
                  }
               });
            }
         });
      }
   }

   @Override protected void onDraw(Canvas canvas) {
      final LayoutBuild layout = getLayoutBuild();
      if (layout == null)
         return;
      canvas.translate(getPaddingLeft(), getPaddingTop());
      layout.draw(canvas);
   }

   int maxAvailableWidth = 0;

   @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);
      int heightSize = MeasureSpec.getSize(heightMeasureSpec);

      int width = widthSize;
      int height = heightSize;

      width = width - getPaddingLeft() - getPaddingRight();
      maxAvailableWidth = width;

      if (!animating) {
         final LayoutBuild lb = getLayoutBuild();
         StaticLayout layout = lb != null ? lb.layout() : null;
         height = layout == null ? 0 : layout.getHeight() + getPaddingTop() + getPaddingBottom();

         if (layout != null && layout.getLineCount() > 0) {
            // subtract last line's spacing
            height -= (layout.getLineBottom(0) - layout.getPaint().getFontMetricsInt().descent - layout.getLineBaseline(0));

            // support width wrap content
            if (layout.getLineCount() == 1 && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
               width = (int) (layout.getLineWidth(0) + getPaddingLeft() + getPaddingRight());
            }
         }
      }

      this.setMeasuredDimension(width, height);
   }

   public interface OnBubbleClickedListener {

      public void onBubbleClicked(View view, BubbleSpan bubbleSpan);
   }
   public void setOnBubbleClickedListener(OnBubbleClickedListener listener) {
      this.listener = listener;
   }

   public void expand(boolean animate) {
      final LayoutBuild l = getLayoutBuild();

      if (l == null || !l.isTruncated())
         return;
      l.setTruncated(false);
      if (!animate) {
         requestLayout();
         return;
      }
      ResizeAnimation expandAnimation = new ResizeAnimation(
         getHeight() + l.expandedLayout().getHeight() - l.truncatedLayout().getHeight()
      );
      expandAnimation.setDuration(400);
      expandAnimation.setAnimationListener(new Animation.AnimationListener() {
         @Override public void onAnimationStart(Animation animation) { animating = true; }
         @Override public void onAnimationEnd(Animation animation) { animating = false; }
         @Override public void onAnimationRepeat(Animation animation) {}
      });
      startAnimation(expandAnimation);
   }

   public class ResizeAnimation extends Animation {

      final int startHeight;

      final int targetHeight;

      public ResizeAnimation(int targetHeight) {
         this.targetHeight = targetHeight;
         startHeight = getHeight();
      }

      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
         int newHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
         getLayoutParams().height = newHeight;
         requestLayout();
      }
      @Override
      public void initialize(int width, int height, int parentWidth, int parentHeight) {
         super.initialize(width, height, parentWidth, parentHeight);
      }
      @Override
      public boolean willChangeBounds() {
         return true;
      }

   }

   private LayoutBuild _layoutBuild;

   public LayoutBuild getLayoutBuild() {
      return _layoutBuild;
   }

   public void setLayoutBuild(LayoutBuild layoutBuild) {
      this._layoutBuild = layoutBuild;
      requestLayout();
   }

   // TODO configuration via xml params
   private LayoutBuild.Config defaultConfig() {
      if (_defaultConfig == null) {
         _defaultConfig = new LayoutBuild.Config();
      }
      return _defaultConfig;
   }

   @Deprecated public void setTruncated(boolean truncated) {
      defaultConfig().truncated = truncated;
   }

   @Deprecated public ChipsTextView setTextPaint(TextPaint textPaint) {
      defaultConfig().textPaint = textPaint;
      return this;
   }

   @Deprecated public void setLineSpacing(float value) {
      defaultConfig().lineSpacing = value;
   }

   @Deprecated public int getTextSize() {
      return (int)defaultConfig().textPaint.getTextSize();
   }

   @Deprecated public void setMaxLines(int maxLines, Spannable moreText) {
      defaultConfig().truncated = true;
      defaultConfig().moreText = moreText;
      defaultConfig().maxLines = maxLines;
   }
}
