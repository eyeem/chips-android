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

import java.util.ArrayList;

public class ChipsEditText extends MultilineEditText {

   ArrayList<String> availableItems = new ArrayList<String>();
   ArrayList<String> filteredItems = new ArrayList<String>();
   EditAction lastEditAction;
   AutocompletePopover popover;
   AutocompleteManager manager;
   boolean autoShow;
   int maxBubbleCount = -1;
   public CharSequence savedHint;

   private BubbleStyle currentBubbleStyle;

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
      manager = new AutocompleteManager();
      manager.setResolver(new AutocompleteManager.Resolver() {
         @Override
         public ArrayList<String> getSuggestions(String query) throws Exception {
            if (resolver == null)
               return null;
            return resolver.getSuggestions(query);
         }

         @Override
         public ArrayList<String> getDefaultSuggestions() {
            return resolver.getDefaultSuggestions();
         }

         @Override
         public void update(String query, ArrayList<String> results) {
            setAvailableItems(results);
         }
      });
      addTextChangedListener(hashWatcher);
      addTextChangedListener(autocompleteWatcher);
      setOnEditorActionListener(editorActionListener);

      setCursorVisible(false);

      // default bubble & cursor style
      setCurrentBubbleStyle(DefaultBubbles.get(DefaultBubbles.GRAY_WHITE_TEXT, getContext(), (int) getTextSize()));
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

   Runnable cursorRunnable = new Runnable() {
      @Override
      public void run() {
         cursorBlink = !cursorBlink;
         postInvalidate();
         postDelayed(cursorRunnable, 500);
      }
   };
   boolean cursorBlink;
   CursorDrawable cursorDrawable;

   @Override
   public boolean onPreDraw() {
      CharSequence hint = getHint();
      boolean empty = TextUtils.isEmpty(getText());
      if (manualModeOn && empty) {
         if (!TextUtils.isEmpty(hint)) {
            setHint("");
         }
      } else if (!manualModeOn && empty && !TextUtils.isEmpty(savedHint)) {
         setHint(savedHint);
      };
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
      manager.search("");
   }

   public void setAutocomplePopover(AutocompletePopover popover) {
      this.popover = popover;
   }

   public void addBubble(String text, int start) {
      if (start > getText().length()) {
         start = getText().length();
      }
      getText().insert(start, text);
      makeChip(start, start+text.length(), true);
      onBubbleCountChanged();
   }

   boolean finalizing;

   public void makeChip(int start, int end, boolean finalize) {
      if (finalizing)
         return;
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
            return;
            // possibly some other entity (Random Shit Keyboard™) is changing
            // the text here in the meanwhile resulting in a crash
         }
      }
      //int textSize = (int)(getTextSize() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics()));
      Utils.bubblify(getText(), finalText, start, end, maxWidth, getCurrentBubbleStyle(), this, null);
      finalizing = false;
   }

   boolean manualModeOn;
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
      manualModeOn = true;
      manualStart = getSelectionStart();
   }

   public boolean hasBubbleAt(int position) {
      return getText().getSpans(position, position+1, BubbleSpanImpl.class).length > 0;
   }

   public void endManualMode() {
      boolean madeChip = false;
      if (manualStart < getSelectionEnd() && manualModeOn) {
         makeChip(manualStart, getSelectionEnd(), true);
         madeChip = true;
         onBubbleCountChanged();
      }
      manualModeOn = false;
      if(popover != null) popover.hide();
      if (madeChip && getSelectionEnd() == getText().length()) {
         getText().append(" ");
         restartInput();
         setSelection(getText().length());
      }
   }

   public void cancelManualMode() {
      if (manualStart < getSelectionEnd() && manualModeOn) {
         getText().delete(manualStart, getSelectionEnd());
      }
      manualModeOn = false;
      if(popover != null) popover.hide();
   }

   TextWatcher autocompleteWatcher = new TextWatcher() {
      ReplacementSpan manipulatedSpan;

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         manipulatedSpan = null;
         if (after < count && !manualModeOn) {
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
            if (manualModeOn && manualStart < start) {
               // we do dis cause android gives us latest word and we operate on a sentence
               count += start - manualStart;
               start = manualStart;
            }
            textForAutocomplete = s.toString().substring(start, start+count);
            if (resolver != null)
               manager.search(textForAutocomplete);
            if (!TextUtils.isEmpty(textForAutocomplete)) {
               showAutocomplete(new EditAction(textForAutocomplete, start, before, count));
            }
         } catch (Exception e) {
         }
      }

      @Override
      public void afterTextChanged(Editable s) {
         if (manualModeOn) {
            int end = getSelectionStart();
            if (end < manualStart) {
               manualModeOn = false;
            } else {
               makeChip(manualStart, end, false);
            }
         } else if (!manualModeOn && manipulatedSpan != null) {
            int start = s.getSpanStart(manipulatedSpan);
            int end = s.getSpanEnd(manipulatedSpan);
            if (start > -1 && end > -1) {
               s.delete(start, end);
            }
            onBubbleCountChanged();
            manipulatedSpan = null;
            manualModeOn = false;
         }
         if(popover != null) 
            if (manualModeOn)
               popover.reposition();
            else
               popover.hide();
      }
   };

   protected void setAvailableItems(ArrayList<String> items) {
      if(popover != null) popover.scrollToTop();
      availableItems = items;
      filter();
   }

   private void filter() {
      ArrayList<String> availableItems = new ArrayList<String>();
      if (this.availableItems != null) {
         for (String item : this.availableItems)
            availableItems.add(item.trim().toLowerCase());
      }
      if (availableItems != null && availableItems.size() > 0) {
         BubbleSpan[] spans = getText().getSpans(0, getText().length(), BubbleSpan.class);
         for (BubbleSpan span : spans) {
            int start = getText().getSpanStart(span);
            int end = getText().getSpanEnd(span);
            if (start == -1 || end == -1 || end <= start || (manualStart == start && manualModeOn))
               continue;
            String text = getText().subSequence(start, end).toString().trim().toLowerCase();
            availableItems.remove(text);
         }
      }
      filteredItems.clear();
      if (lastEditAction != null) {
         String text = lastEditAction.text.toLowerCase();
         if (!TextUtils.isEmpty(text) && availableItems != null)
            for (String item : availableItems) {
               if ((text.length() > 1 && item.toLowerCase().startsWith(text))
                  || (manualModeOn && item.toLowerCase().contains(text) && text.length() > 3)) {
                  filteredItems.add(item);
               }
            }
      }
      if(popover != null) 
         if (filteredItems.size() > 0) {
            popover.setItems(filteredItems);
            if (shouldShow()) {
               popover.show();
            }
         } else {
            if (!manualModeOn) {
               popover.hide();
            }
            popover.setItems(availableItems);
         }
   }

   private boolean shouldShow() {
      return autoShow || manualModeOn;
   }

   public void showAutocomplete(EditAction editAction) {
      lastEditAction = editAction;
      filter();
   }

   TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
         if (keyEvent == null) {
            // CustomViewAbove seems to send enter keyevent for some reason (part one)
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED)
               actionId = EditorInfo.IME_ACTION_DONE;
            if (actionId == EditorInfo.IME_ACTION_DONE && manualModeOn) {
               endManualMode();
               onActionDone();
               return true;
            } else if (actionId == EditorInfo.IME_ACTION_DONE && !manualModeOn) {
               hideKeyboard();
               onActionDone();
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
      if(popover != null) popover.hide();
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

   public void setAutocompleteResolver(AutocompleteResolver resolver) {
      this.resolver = resolver;
   }

   private AutocompleteResolver resolver;

   public interface AutocompleteResolver {
      public ArrayList<String> getSuggestions(String query) throws Exception;
      public ArrayList<String> getDefaultSuggestions();
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
            if (manualModeOn || canAddMoreBubbles())
               s.delete(lastIndex, lastIndex + 1);

            if (manualModeOn && manualStart < lastIndex) {
               // here we end previous hashtag
               endManualMode();
            }

            if (canAddMoreBubbles()) {
               // we start adding a new hash tag
               startManualMode();
               if(popover != null) popover.show();
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

   ArrayList<Listener> listeners = new ArrayList<Listener>();

   protected void onBubbleCountChanged() {
      for (Listener listener : listeners)
         listener.onBubbleCountChanged();
   }

   protected void onActionDone() {
      for (Listener listener : listeners)
         listener.onActionDone();
   }

   protected void onBubbleSelected(int position) {
      for (Listener listener : listeners)
         listener.onBubbleSelected(position);
   }

   protected void onXPressed() {
      for (Listener listener : listeners)
         listener.onXPressed();
   }

   protected void onHashTyped(boolean start) {
      for (Listener listener : listeners)
         listener.onHashTyped(start);
   }

   public void addListener(Listener listener) {
      listeners.add(listener);
   }

   public interface Listener {
      public void onBubbleCountChanged();
      public void onActionDone();
      public void onBubbleSelected(int position);
      public void onXPressed();
      public void onHashTyped(boolean start);
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
}
