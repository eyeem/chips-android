package com.eyeem.chips;

import android.content.Context;
import android.graphics.*;
import android.text.*;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ChipsEditText extends MultilineEditText {

   EditAction lastEditAction;
   boolean autoShow;
   int maxBubbleCount = -1;
   public CharSequence savedHint;

   private BubbleStyle currentBubbleStyle;
   private Rect visibleRect;
   private Point activitySize;

   public ChipsEditText(Context context) {
      super(context);
      init();
   }

   public ChipsEditText(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public ChipsEditText(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init();
   }

   void init() {
      visibleRect = new Rect();
      activitySize = Utils.activitySize(Utils.findActivity(getContext()));
      addTextChangedListener(hashWatcher);
      addTextChangedListener(autocompleteWatcher);
      setOnEditorActionListener(editorActionListener);

      setCursorVisible(false);

      // default bubble & cursor style
      // setCurrentBubbleStyle(DefaultBubbles.get(DefaultBubbles.GRAY_WHITE_TEXT, getContext(), (int) getTextSize()));
      this.savedHint = getHint();
   }

   @Override
   protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      post(cursorRunnable);
   }

   @Override
   protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      removeCallbacks(cursorRunnable);
   }

   Runnable cursorRunnable = new CursorRunnable(this);

   static class CursorRunnable implements Runnable {

      WeakReference<ChipsEditText> _et;

      public CursorRunnable(ChipsEditText et) {
         this._et = new WeakReference<>(et);
      }

      @Override public void run() {
         ChipsEditText et = _et.get();
         if (et == null) return;
         et.cursorBlink = !et.cursorBlink;
         if (et.areWeVisible()) { // skip invalidation if we're not visible, pointless and causes unnecessary redraws
            et.postInvalidate();
         }
         et.postDelayed(this, 500);
      }
   }

   boolean cursorBlink;
   CursorDrawable cursorDrawable;

   protected void setManualModeOn(boolean value) {
      this._manualModeOn = value;
      onManualModeChanged(value);
   }

   @Override
   public boolean onPreDraw() {
      CharSequence hint = getHint();
      boolean empty = TextUtils.isEmpty(getText());
      if (_manualModeOn && empty) {
         if (!TextUtils.isEmpty(hint)) {
            setHint("");
         }
      } else if (!_manualModeOn && empty && !TextUtils.isEmpty(savedHint)) {
         setHint(savedHint);
      }
      return super.onPreDraw();
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      if (isFocused()) {
         cursorDrawable.draw(canvas, cursorBlink);
      }
   }

   public void resetAutocompleList() {
      lastEditAction = null;
      onBubbleType("");
   }

   public void addBubble(String text, int start, Object data) {
      if (start > getText().length()) {
         start = getText().length();
      }
      getText().insert(start, text);
      makeChip(start, start+text.length(), true, data);
      onBubbleCountChanged();
   }

   boolean finalizing;

   public String makeChip(int start, int end, boolean finalize, Object data) {
      if (finalizing)
         return null;
      int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
      String finalText = null;
      if (finalize) {
         finalizing = true;
         try {
            getText().insert(start, " ");
            getText().insert(end + 1, " ");
            end += 2;
            finalText = getText().subSequence(start + 1, end - 1).toString();
         } catch (java.lang.IndexOutOfBoundsException e) {
            finalizing = false;
            return null;
            // possibly some other entity (Random Shit Keyboard™) is changing
            // the text here in the meanwhile resulting in a crash
         }
      }
      //int textSize = (int)(getTextSize() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics()));
      Utils.bubblify(getText(), finalText, start, end, maxWidth, getCurrentBubbleStyle(), this, data);
      finalizing = false;
      return finalText;
   }

   boolean _manualModeOn;
   int manualStart;

   public void setMaxBubbleCount(int maxBubbleCount) {
      this.maxBubbleCount = maxBubbleCount;
   }

   public boolean canAddMoreBubbles() {
      return maxBubbleCount == -1 || getBubbleCount() < maxBubbleCount;
   }

   public int getBubbleCount() {
      try {
         return getText().getSpans(0, getText().length(), BubbleSpan.class).length;
      } catch (Exception e) {
         return 0;
      }
   }

   public void startManualMode() {
      resetAutocompleList();
      if (!canAddMoreBubbles())
         return;
      int i = getSelectionStart() - 1;
      if (i >= 0 &&
         (!Character.isWhitespace(getText().charAt(i)) || hasBubbleAt(i))) {
         getText().insert(i+1, " ");
      }
      lastEditAction = null;
      setManualModeOn(true);
      manualStart = getSelectionStart();
   }

   public boolean hasBubbleAt(int position) {
      return getText().getSpans(position, position+1, BubbleSpanImpl.class).length > 0;
   }

   public String endManualMode() {
      boolean madeChip = false;
      String chipText = null;
      if (manualStart < getSelectionEnd() && _manualModeOn) {
         chipText = makeChip(manualStart, getSelectionEnd(), true, null);
         madeChip = true;
         onBubbleCountChanged();
      }
      setManualModeOn(false);
      onBubbleType("");
      if (madeChip && getSelectionEnd() == getText().length()) {
         getText().append(" ");
         restartInput();
         setSelection(getText().length());
      }
      return chipText;
   }

   public void cancelManualMode() {
      if (manualStart < getSelectionEnd() && _manualModeOn) {
         getText().delete(manualStart, getSelectionEnd());
      }
      setManualModeOn(false);
      onBubbleType("");
   }

   TextWatcher autocompleteWatcher = new TextWatcher() {
      ReplacementSpan manipulatedSpan;

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         manipulatedSpan = null;
         if (after < count && !_manualModeOn) {
            ReplacementSpan[] spans = ((Spannable)s).getSpans(start, start+count, ReplacementSpan.class);
            if (spans.length == 1) {
               manipulatedSpan = spans[0];
            } else {
               manipulatedSpan = null;
            }
         }
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
         if (!shouldShow())
            return;
         String textForAutocomplete = null;
         try {
            if (_manualModeOn && manualStart < start) {
               // we do dis cause android gives us latest word and we operate on a sentence
               count += start - manualStart;
               start = manualStart;
            }
            textForAutocomplete = s.toString().substring(start, start+count);
            onBubbleType(textForAutocomplete);
//            if (!TextUtils.isEmpty(textForAutocomplete)) {
//               showAutocomplete(new EditAction(textForAutocomplete, start, before, count));
//            }
         } catch (Exception e) {
         }
      }

      @Override
      public void afterTextChanged(Editable s) {
         if (_manualModeOn) {
            int end = getSelectionStart();
            if (end < manualStart) {
               setManualModeOn(false);
            } else {
               makeChip(manualStart, end, false, null);
            }
         } else if (!_manualModeOn && manipulatedSpan != null) {
            int start = s.getSpanStart(manipulatedSpan);
            int end = s.getSpanEnd(manipulatedSpan);
            if (start > -1 && end > -1) {
               s.delete(start, end);
            }
            onBubbleCountChanged();
            manipulatedSpan = null;
            setManualModeOn(false);
         }
      }
   };

   private boolean shouldShow() {
      return autoShow || _manualModeOn;
   }

   TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
         if (keyEvent == null) {
            // CustomViewAbove seems to send enter keyevent for some reason (part one)
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED)
               actionId = EditorInfo.IME_ACTION_DONE;
            if (actionId == EditorInfo.IME_ACTION_DONE && _manualModeOn) {
               String text = endManualMode();
               onActionDone(true, text);
               return true;
            } else if (actionId == EditorInfo.IME_ACTION_DONE && !_manualModeOn) {
               hideKeyboard();
               onActionDone(false, null);
               return true;
            }
         } else if (keyEvent != null && actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            // CustomViewAbove seems to send enter keyevent for some reason  (part two)
            return true;
         }
         return false;
      }
   };

   public void hideKeyboard() {
      InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(getWindowToken(), 0);
      onBubbleType("");
   }

   public void showKeyboard() {
      InputMethodManager inputMgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMgr.showSoftInput(this, InputMethodManager.SHOW_FORCED);
   }

   public void restartInput() {
      InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.restartInput(this);
   }

   public Point getInnerCursorPosition() {
      int pos = getSelectionStart();
      Layout layout = getLayout();
      if (layout == null) {
         return new Point(0, 0);
      }
      int line = layout.getLineForOffset(pos);
      int baseline = layout.getLineBaseline(line);
      //int ascent = layout.getLineAscent(line);
      float x = layout.getPrimaryHorizontal(pos);
      float y = baseline /*+ ascent*/;
      return new Point((int)x, (int)y);
   }

   public Point getCursorPosition() {
      Point p = getInnerCursorPosition();
      p.offset(getPaddingLeft(), getPaddingTop());
      return p;
   }

   public static class EditAction {
      String text;
      int start;
      int before;
      int count;
      int end() {
         return start + count;
      }

      public EditAction(String text, int start, int before, int count) {
         this.text = text;
         this.start = start;
         this.before = before;
         this.count = count;
      }
   }

   boolean muteHashWatcher;
   void muteHashWatcher(boolean value) {
      muteHashWatcher = value;
   }

   private TextWatcher hashWatcher = new TextWatcher() {
      String before;
      String after;

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         if (muteHashWatcher)
            return;
         before = s.toString();
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
         if (muteHashWatcher)
            return;
         after = s.toString();
      }

      @Override
      public void afterTextChanged(Editable s) {
         if (muteHashWatcher)
            return;
         if (after.length() > before.length() && after.lastIndexOf('#') > before.lastIndexOf('#')) {
               int lastIndex = after.lastIndexOf('#');
            if (_manualModeOn || canAddMoreBubbles())
               s.delete(lastIndex, lastIndex + 1);

            if (_manualModeOn && manualStart < lastIndex) {
               // here we end previous hashtag
               endManualMode();
            }

            if (canAddMoreBubbles()) {
               // we start adding a new hash tag
               startManualMode();
               onBubbleType("");
               onHashTyped(true);
            } else {
               // no more hash tags allowed
               onHashTyped(false);
            }
         }
      }
   };

   int previousWidth = 0;

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);
      if (previousWidth != widthSize) {
         previousWidth = widthSize;
         int maxBubbleWidth = widthSize - getPaddingLeft() - getPaddingTop();
         Editable e = getText();
         BubbleSpan[] spans = e.getSpans(0, getText().length(), BubbleSpan.class);
         for (int i = 0; i < spans.length; i++) {
            BubbleSpan span = spans[i];
            span.resetWidth(maxBubbleWidth);
            int start = getText().getSpanStart(spans[i]);
            int end = getText().getSpanEnd(spans[i]);
            e.removeSpan(span);
            e.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
      }
   }

   ArrayList<Listener> listeners = new ArrayList<>();
   ArrayList<BubbleTextWatcher> bubbleTextWatchers = new ArrayList<>();

   protected void onBubbleCountChanged() {
      for (Listener listener : listeners)
         listener.onBubbleCountChanged();
   }

   protected void onActionDone(boolean wasManualModeOn, String text) {
      for (Listener listener : listeners)
         listener.onActionDone(wasManualModeOn, text);
   }

   protected void onHashTyped(boolean start) {
      for (Listener listener : listeners)
         listener.onHashTyped(start);
   }

   protected void onManualModeChanged(boolean value) {
      for (Listener listener : listeners)
         listener.onManualModeChanged(value);
   }

   protected void onBubbleType(String value) {
      for (BubbleTextWatcher watcher : bubbleTextWatchers)
         watcher.onType(value);
   }

   public void addListener(Listener listener) {
      listeners.add(listener);
   }

   public void removeListener(Listener listener) {
      listeners.remove(listener);
   }

   public void addBubbleTextWatcher(BubbleTextWatcher watcher) {
      bubbleTextWatchers.add(watcher);
   }

   public void removeBubbleTextWatcher(BubbleTextWatcher watcher) {
      bubbleTextWatchers.remove(watcher);
   }

   public boolean isManualModeOn() { return _manualModeOn; }

   public interface Listener {
      public void onBubbleCountChanged();
      public void onActionDone(boolean wasManualModeOn, String text);
      public void onHashTyped(boolean start);
      public void onManualModeChanged(boolean enabled);
   }

   public CursorDrawable getCursorDrawable() {
      return this.cursorDrawable;
   }

   public BubbleStyle getCurrentBubbleStyle() {
      return currentBubbleStyle;
   }

   public void setCurrentBubbleStyle(BubbleStyle currentBubbleStyle) {
      if (this.currentBubbleStyle == currentBubbleStyle) return;
      this.currentBubbleStyle = currentBubbleStyle;
      float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getContext().getResources().getDisplayMetrics());
      this.cursorDrawable = new CursorDrawable(this, getTextSize()*1.5f, width, getContext());
   }

   public SpannableString snapshot() {
      return new SpannableString(getText());
   }

   public interface BubbleTextWatcher {
      public void onType(String query);
   }

   protected boolean areWeVisible() {
      return Utils.areWeInScreen(this, visibleRect, activitySize);
   }
}
