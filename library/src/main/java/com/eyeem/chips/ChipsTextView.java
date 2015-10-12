package com.eyeem.chips;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * ChipsTextView
 */
public class ChipsTextView extends View {

   OnBubbleClickedListener listener;

   // default config
   boolean animating;
   boolean dynamicTextWidth;
   boolean consumeAllTouchEvents;
   LayoutBuild.Config _defaultConfig;

   public ChipsTextView(Context context) {
      super(context);
   }

   public ChipsTextView(Context context, AttributeSet attrs) {
      super(context, attrs);
      processAttributes(context, attrs);
   }

   public ChipsTextView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      processAttributes(context, attrs);
   }

   @Override public boolean onTouchEvent(MotionEvent event) {
      final LayoutBuild layoutBuild = getLayoutBuild();
      if (layoutBuild == null || !defaultConfig().clickable) return false;
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
      return consumeAllTouchEvents || retValue;
   }

   /**
    * Use this method to set the text synchronously (slow). Useful when you don't know the
    * width of the text ahead of time. The size of the layout and the layout itself will be
    * figured out and calculated during onMeasure.
    * @param text
    */
   public void setText(final Spannable text) {
      if (text == null) {
         setLayoutBuild((LayoutBuild)null);
         dynamicTextWidth = false;
         return;
      }

      dynamicTextWidth = true;

      final LayoutBuild currentLayout = getLayoutBuild();

      if (currentLayout != null && text.equals(currentLayout.getSpannable())) return;

      doBuild(text);
   }

   private void doBuild(final Spannable text) {
      LayoutBuild futureLayout = new LayoutBuild(text, defaultConfig());
      futureLayout.build(maxAvailableWidth, false);
      setLayoutBuild(futureLayout);
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

      maxAvailableWidth = width - getPaddingLeft() - getPaddingRight();

      if (!animating && maxAvailableWidth > 0) {
         final LayoutBuild lb = getLayoutBuild();

         if (lb != null && (lb.buildWidth <= 0 || (dynamicTextWidth && lb.buildWidth != maxAvailableWidth)))  {
            lb.build(maxAvailableWidth, false); // <-- this takes time and runs on UI thread, avoid using this
         }

         height = lb == null ? 0 : lb.layoutHeight() + getPaddingTop() + getPaddingBottom();

         // support width wrap content
         if (lb != null && lb.isSingleLine() && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            width = (int) (lb.lineWidth(0) + getPaddingLeft() + getPaddingRight());
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

   public void setConsumeAllTouchEvents(boolean value) {
      this.consumeAllTouchEvents = value;
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
         getHeight() + l.expansionHeight()
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
      //StaticLayout sl = layoutBuild != null ? layoutBuild.layout() : null;

      int currentW = getWidth() - getPaddingLeft() - getPaddingRight();
      int currentH = getHeight() - getPaddingTop() - getPaddingBottom();

      if (layoutBuild != null && layoutBuild.getBuildWidth() == currentW && layoutBuild.layoutHeight() == currentH) { // if size hasn't changed just invalidate
         invalidate();
      } else {
         requestLayout();
      }
   }

   Subscription lastSubscription;

   public void setLayoutBuild(Observable<LayoutBuild> layoutBuildObservable) {

      if (lastSubscription != null) {
         lastSubscription.unsubscribe();
         _layoutBuild = null;
         lastSubscription = null;
         invalidate();
      }

      if (layoutBuildObservable == null) {
         return;
      }

      lastSubscription = layoutBuildObservable
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe(new LayoutSubscription(this));
   }

   private void processAttributes(Context context, AttributeSet attrs){

      LayoutBuild.Config config = defaultConfig();
      
      config.textPaint = new TextPaint();
      config.textPaint.setAntiAlias(true);

      Resources r = context.getResources();
      TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChipsTextView, 0 ,0);
      float textSize = ta.getDimension(R.styleable.ChipsTextView_ctvTextSize, r.getDimension(R.dimen.default_chips_text_size));
      int textColor = ta.getColor(R.styleable.ChipsTextView_ctvTextColor, r.getColor(R.color.default_chips_text_color));
      config.textPaint.setTextSize(textSize);
      config.textPaint.setColor(textColor);

      TypedValue outValue = new TypedValue();
      r.getValue(R.dimen.default_chips_line_spacing, outValue, true);
      config.lineSpacing = ta.getFloat(R.styleable.ChipsTextView_lineSpacing, outValue.getFloat());

      config.truncated = ta.getBoolean(R.styleable.ChipsTextView_truncated, false);
      if (config.truncated) {
         config.maxLines = ta.getInt(R.styleable.ChipsTextView_maxLines, r.getInteger(R.integer.default_chips_max_lines));
         String moreText = ta.getString(R.styleable.ChipsTextView_moreText);
         if (moreText == null)
            moreText = r.getString(R.string.default_chips_more_text);
         int moreTextColor = ta.getColor(R.styleable.ChipsTextView_moreTextColor, r.getColor(R.color.default_chips_more_text_color));
         int moreTextColorActive = ta.getColor(R.styleable.ChipsTextView_moreTextColorActive, r.getColor(R.color.default_chips_more_text_color_active));
         SpannableStringBuilder moreTextSpan = new SpannableStringBuilder(moreText);
         Utils.tapify(moreTextSpan, 0, moreTextSpan.length(), moreTextColorActive, moreTextColor, new Truncation());
         config.moreText = moreTextSpan;
      }

      config.debug = ta.getBoolean(R.styleable.ChipsTextView_debug, false);

      config.clickable = ta.getBoolean(R.styleable.ChipsTextView_clickable, true);

      ta.recycle();

   }

   public LayoutBuild.Config defaultConfig() {
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

   @Deprecated public ChipsTextView setDebug(boolean value) {
      defaultConfig().debug = value;
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

   @Deprecated public void setClickable(boolean clickable) {
      defaultConfig().clickable = clickable;
   }

   private static class LayoutSubscription implements Action1<LayoutBuild> {

      final WeakReference<ChipsTextView> _tv;

      LayoutSubscription(ChipsTextView textView) {
         _tv = new WeakReference<ChipsTextView>(textView);
      }

      @Override public void call(LayoutBuild layoutBuild) {
         ChipsTextView tv = _tv.get();
         if (tv == null) return;
         tv.setLayoutBuild(layoutBuild);
      }
   }

   /**
    * Marker class. Marks a truncation bubble/area
    */
   public static class Truncation {
   }
}
