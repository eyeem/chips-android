package com.eyeem.chips;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ChipsEditText extends EditText {

   ArrayList<String> availableItems = new ArrayList<String>();
   ArrayList<String> filteredItems = new ArrayList<String>();
   EditAction lastEditAction;
   AutocompletePopover popover;
   AutocompleteManager manager;
   boolean autoShow;
   Paint bmpPaint;
   int maxBubbleCount = -1;

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

      bmpPaint = new Paint();
      bmpPaint.setFilterBitmap(true);
   }


   CursorDrawable cursorDrawable;
   boolean hijacked;

   public void hijackCursor() throws Exception {
      // we need to access TextView.mEditor (private) and from there
      // replace drawables in mCursorDrawable array. This way we provide
      // our own Drawable where we can custom the way cursor is displayed
      if (hijacked)
         return;
      Field field_mEditor = TextView.class.getDeclaredField("mEditor");
      field_mEditor.setAccessible(true);
      Object value_mEditor = field_mEditor.get(this);
      Field field_mCursorDrawable = value_mEditor.getClass().getDeclaredField("mCursorDrawable");
      field_mCursorDrawable.setAccessible(true);
      Drawable[] cursorDrawable = (Drawable[])field_mCursorDrawable.get(value_mEditor);
      float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getContext().getResources().getDisplayMetrics());
      if (this.cursorDrawable == null)
         this.cursorDrawable = new CursorDrawable(this, getTextSize()*1.5f, width);
      for (int i=0; i<cursorDrawable.length; i++) {
         if (cursorDrawable[i] == this.cursorDrawable) {
            hijacked = true;
            break;
         } else if (cursorDrawable[i] != null) {
            cursorDrawable[i] = this.cursorDrawable;
            hijacked = true;
            break;
         }
      }
      if (!hijacked) {
         cursorDrawable[0] = this.cursorDrawable;
      }
   }

   @Override
   public boolean onPreDraw() {
      boolean value = super.onPreDraw();
      try {
         hijackCursor();
      } catch (Exception e) {
         // unfortunately stuck with shitty cursor
      }
      return value;
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      while (!redrawStack.isEmpty()) {
         BubbleSpan span = redrawStack.remove(0);
         span.redraw(canvas);
      }
   }

   public void resetAutocompleList() {
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
      makeChip(start, start+text.length());
   }

   public void makeChip(int start, int end) {
      int maxWidth = getWidth() - getPaddingLeft() - getPaddingRight();
      Utils.bubblify(getText(), null, start, end, maxWidth, DefaultBubbles.get(0, getContext()), this, null);
   }

   @Override
   public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
      InputConnection connection = super.onCreateInputConnection(outAttrs);
      int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
      if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
         // clear the existing action
         outAttrs.imeOptions ^= imeActions;
         // set the DONE action
         outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
      }
      if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
         outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
      }
      return connection;
   }

   boolean manualModeOn;
   int manualStart;

   public void setMaxBubbleCount(int maxBubbleCount) {
      this.maxBubbleCount = maxBubbleCount;
   }

   public boolean canAddMoreBubbles() {
      return maxBubbleCount == -1 || getText().getSpans(0, getText().length(), BubbleSpan.class).length < maxBubbleCount;
   }

   public void startManualMode() {
      if (!canAddMoreBubbles())
         return;
      lastEditAction = null;
      manualModeOn = true;
      manualStart = getSelectionStart();
   }

   public void endManualMode() {
      boolean madeChip = false;
      if (manualStart < getSelectionEnd() && manualModeOn) {
         makeChip(manualStart, getSelectionEnd());
         madeChip = true;
      }
      manualModeOn = false;
      popover.hide();
      if (madeChip && getSelectionEnd() == getText().length()) {
         getText().append(" ");
      }
   }

   public void cancelManualMode() {
      if (manualStart < getSelectionEnd() && manualModeOn) {
         getText().delete(manualStart, getSelectionEnd());
      }
      manualModeOn = false;
      popover.hide();
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
               makeChip(manualStart, end);
            }
         } else if (!manualModeOn && manipulatedSpan != null) {
            int start = s.getSpanStart(manipulatedSpan);
            int end = s.getSpanEnd(manipulatedSpan);
            if (start > -1 && end > -1)
               s.delete(start, end);
            manipulatedSpan = null;
            manualModeOn = false;
         }
         popover.reposition();
      }
   };

   protected void setAvailableItems(ArrayList<String> items) {
      availableItems = items;
      filter();
   }

   private void filter() {
      filteredItems.clear();
      if (lastEditAction != null) {
         String text = lastEditAction.text.toLowerCase();
         if (!TextUtils.isEmpty(text))
            for (String item : availableItems) {
               if ((text.length() > 1 && item.toLowerCase().startsWith(text))
                  || (manualModeOn && item.toLowerCase().contains(text) && text.length() > 3)) {
                  filteredItems.add(item);
               }
            }
      }
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
               return true;
            } else if (actionId == EditorInfo.IME_ACTION_DONE && !manualModeOn) {
               hideKeyboard();
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
      popover.hide();
   }

   public void showKeyboard() {
      InputMethodManager inputMgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMgr.showSoftInput(this, InputMethodManager.SHOW_FORCED);
   }

   public Point getInnerCursorPosition() {
      int pos = getSelectionStart();
      Layout layout = getLayout();
      if (layout == null) {
         return new Point(0, 0);
      }
      int line = layout.getLineForOffset(pos);
      int baseline = layout.getLineBaseline(line);
      int ascent = layout.getLineAscent(line);
      float x = layout.getPrimaryHorizontal(pos);
      float y = baseline + ascent;
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

   public ArrayList<BubbleSpan> redrawStack = new ArrayList<BubbleSpan>();

   private TextWatcher hashWatcher = new TextWatcher() {
      String before;
      String after;

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         before = s.toString();
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
         after = s.toString();
      }

      @Override
      public void afterTextChanged(Editable s) {
         if (after.length() > before.length() && after.lastIndexOf('#') > before.lastIndexOf('#') && !manualModeOn) {
            int lastIndex = after.lastIndexOf('#');
            s.delete(lastIndex, lastIndex+1);
            startManualMode();
            resetAutocompleList();
            popover.show();
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
}
