package com.eyeem.chips;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.text.*;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ChipsEditText extends EditText {

   ArrayList<String> availableItems = new ArrayList<String>();
   ArrayList<String> filteredItems = new ArrayList<String>();
   EditAction lastEditAction;
   AutocompletePopover popover;

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
      addTextChangedListener(autocompleteWatcher);
      setOnEditorActionListener(editorActionListener);
   }

   public void setAutocomplePopover(AutocompletePopover popover) {
      this.popover = popover;
   }

   public void makeChip(int start, int end) {
      String text = getText().toString();
      text = text.substring(start, end);

      // inflate chips_edittext layout
      LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      TextView textView = (TextView) lf.inflate(R.layout.chips, null);
      textView.setText(text); // set text
      // capture bitmapt of genreated textview
      int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
      textView.measure(spec, spec);
      textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
      Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(b);
      canvas.translate(-textView.getScrollX(), -textView.getScrollY());
      textView.draw(canvas);
      textView.setDrawingCacheEnabled(true);
      Bitmap cacheBmp = textView.getDrawingCache();
      Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
      textView.destroyDrawingCache();  // destory drawable
      // create bitmap drawable for imagespan
      BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
      bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
      // create and set imagespan
      ImageSpan[] spansToClear = getText().getSpans(start, end, ImageSpan.class);
      for (ImageSpan span : spansToClear)
         getText().removeSpan(span);
      getText().setSpan(new ImageSpan(bmpDrawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
      manualModeOn = true;
      manualStart = getSelectionStart();
   }

   public void endManualMode() {
      if (manualStart > getSelectionEnd() && manualModeOn)
         makeChip(manualStart, getSelectionEnd());
      manualModeOn = false;
   }

   TextWatcher autocompleteWatcher = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
         String textForAutocomplete = null;
         try {
            textForAutocomplete = s.toString().substring(start, start+count);
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
         }
         popover.reposition();
      }
   };

   public void setAvailableItems(ArrayList<String> items) {
      availableItems = items;
      filter();
   }

   private void filter() {
      filteredItems.clear();
      if (lastEditAction != null) {
         String text = lastEditAction.text.toLowerCase();
         for (String item : availableItems) {
            if ((text.length() > 1 && item.toLowerCase().startsWith(text))
               || (manualModeOn && item.toLowerCase().contains(text) && text.length() > 3)) {
               filteredItems.add(item);
            }
         }
      }
      if (filteredItems.size() > 0) {
         popover.setItems(filteredItems);
         popover.show();
      } else {
         if (!manualModeOn) {
            popover.hide();
         }
         popover.setItems(availableItems);
      }
   }

   public void showAutocomplete(EditAction editAction) {
      lastEditAction = editAction;
      filter();
   }

   TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
         if (actionId == EditorInfo.IME_ACTION_DONE && manualModeOn) {
            endManualMode();
            return true;
         } else if (actionId == EditorInfo.IME_ACTION_DONE && !manualModeOn) {
            hideKeyboard();
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
      inputMgr.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
   }

   public Point getCursorPosition() {
      int pos = getSelectionStart();
      Layout layout = getLayout();
      int line = layout.getLineForOffset(pos);
      int baseline = layout.getLineBaseline(line);
      int ascent = layout.getLineAscent(line);
      float x = layout.getPrimaryHorizontal(pos);
      float y = baseline + ascent;
      Point p = new Point((int)x, (int)y);
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
}
