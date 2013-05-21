package com.eyeem.chips;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
      addTextChangedListener(autocompleteWatcher);
      setOnEditorActionListener(editorActionListener);

      bubbleStyles = new AwesomeBubbles.BubbleStyles(getContext());

      bmpPaint = new Paint();
      bmpPaint.setFilterBitmap(true);

   }

   AwesomeBubbles.BubbleStyles bubbleStyles;
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

   public void resetAutocompleList() {
      manager.search("");
   }

   public void setAutocomplePopover(AutocompletePopover popover) {
      this.popover = popover;
   }

   public void makeChip(int start, int end) {
      String text = getText().toString();
      text = text.substring(start, end);

      // create bitmap drawable for imagespan
      TextPaint tp = new TextPaint();
      int maxWidth = getWidth() - getPaddingRight() - getPaddingLeft();
      BubbleDrawable bmpDrawable = new BubbleDrawable((int)getTextSize(), text, bubbleStyles.get(0), maxWidth, tp);
      bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), (int)getTextSize());

      final int certainOffsetValue = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getContext().getResources().getDisplayMetrics());

      // create and set imagespan
      ImageSpan[] spansToClear = getText().getSpans(start, end, ImageSpan.class);
      for (ImageSpan span : spansToClear)
         getText().removeSpan(span);
      getText().setSpan(new ImageSpan(bmpDrawable) {
         @Override
         public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            //super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            // TODO reuse code from AwesomeBubbles
            bottom -= certainOffsetValue;
            BubbleDrawable b = (BubbleDrawable)getDrawable();
            canvas.save();

            int transY = bottom - b.getBounds().bottom;

            int line = getLayout().getLineForOffset(start);
            int lineStart = getLayout().getLineStart(line);
            int lineEnd = getLayout().getLineEnd(line);
            if (lineStart == start && lineEnd == end) {
               // if there's no text around ImageSpan android will misalign
               // FIXME this magic number must be justified somehow by fontMetrics etc
               transY += (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getContext().getResources().getDisplayMetrics());
            } else {
               transY -= paint.getFontMetricsInt().descent;
            }

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
         }
      }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
   }

   public void addBubble(String text, int start) {
      getText().insert(start, text);
      makeChip(start, start+text.length());
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

   public void startManualMode() {
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
      ImageSpan manipulatedSpan;

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         manipulatedSpan = null;
         if (after < count && !manualModeOn) {
            ImageSpan[] spans = ((Spannable)s).getSpans(start, start+count, ImageSpan.class);
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
      //inputMgr.toggleSoftInput(InputMethodManager.SHOW_, 0);
      inputMgr.showSoftInput(this, InputMethodManager.SHOW_FORCED);
   }

   public Point getInnerCursorPosition() {
      int pos = getSelectionStart();
      Layout layout = getLayout();
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
}
