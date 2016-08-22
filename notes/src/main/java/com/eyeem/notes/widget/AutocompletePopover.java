package com.eyeem.notes.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.asolutions.widget.RowLayout;
import com.eyeem.chips.BubbleSpan;
import com.eyeem.chips.BubbleSpanImpl;
import com.eyeem.chips.ChipsEditText;
import com.eyeem.notes.R;

import java.util.ArrayList;

/**
 * Created by vishna on 10/08/16.
 */
public class AutocompletePopover extends RelativeLayout implements
   ViewTreeObserver.OnGlobalLayoutListener, TextWatcher, ChipsEditText.BubbleTextWatcher, ChipsEditText.Listener {

   ViewGroup vg;
   RelativeLayout root;
   ChipsEditText et;
   Adapter adapter;
   ScrollView scrollView;
   InputMethodManager imm;
   int bgColor = 0xFF000000;
   double triAngle = Math.PI / 2.0; // 90 degrees
   Paint bgPaint;

   AutocompleteHelper helper;

   public AutocompletePopover(Context context) {
      super(context);
      init();
   }

   public AutocompletePopover(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public AutocompletePopover(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init();
   }

   void init() {
      LayoutInflater.from(getContext()).inflate(R.layout.autocomplete_popover, this, true);
      // below was a ListView but there were rendering issues on older Androids thus this ugly code
      scrollView = (ScrollView)findViewById(R.id.suggestions);
      vg = new RowLayout(getContext(), null);
      scrollView.addView(vg, -2, -2);
      adapter = new Adapter(vg);
      adapter.onItemClickListener = onItemClickListener;
      setVisibility(View.GONE);

      imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      bgPaint = new Paint();
      bgPaint.setAntiAlias(true);
      bgPaint.setColor(bgColor);
      bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);

      post(new Runnable() {
         @Override public void run() {
            root = (RelativeLayout) getParent();
            root.getViewTreeObserver().addOnGlobalLayoutListener(AutocompletePopover.this);
         }
      });
   }

   public void setResolver(Resolver resolver) {
      this.helper = new AutocompleteHelper(resolver, adapter);
   }

   public void setChipsEditText(ChipsEditText et) {
      this.et = et;
      this.et.addTextChangedListener(this);
      this.et.addBubbleTextWatcher(this);
      this.et.addListener(this);
   }

   public void reposition() {
      Point p = et.getCursorPosition();
      Point bOff = et.getCursorDrawable().bubble_offset();
      p.offset(-bOff.x, -bOff.y);
      p.y -= et.getScrollY();

      int topMargin = et.getTop() + p.y;
      if (topMargin > et.getBottom()) topMargin = et.getBottom();
      RelativeLayout.LayoutParams lp = ((RelativeLayout.LayoutParams)getLayoutParams());

      boolean topMarginChanged = lp.topMargin != topMargin;
      lp.topMargin = topMargin;
      lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
      xTriOffset = p.x + et.getLeft();

      // if top margin is different, then request layout, otherwise just invalidate
      if (topMarginChanged) {
         requestLayout();
      } else {
         invalidate();
      }
   }

   public void show() {
      if (!et.canAddMoreBubbles()) {
         return;
      }
      setVisibility(VISIBLE);
      reposition();
      if (onVisibilityChangedListener != null) {
         onVisibilityChangedListener.onVisibilityChanged(true);
      }
   }

   public void hide() {
      setVisibility(GONE);
      if (onVisibilityChangedListener != null) {
         onVisibilityChangedListener.onVisibilityChanged(false);
      }
   }

   @Override public void onGlobalLayout() {
      reposition();
   }

   @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
   @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
   @Override public void afterTextChanged(Editable s) {
      reposition();
   }

   @Override public void onType(String query) {
      if (helper != null) {
         helper.search(query);
      }
      reposition();
   }

   @Override public void onBubbleCountChanged() {
      adapter.setSelectedItems(getSelectedItems());
   }

   @Override public void onActionDone(boolean wasManualModeOn, String chipText) {
      if (wasManualModeOn) {
         // we just ended manual mode
         if (TextUtils.isEmpty(chipText) || chipText.trim().length() == 0) {
            // the bubble was empty so dismiss the overlay
            hide();
            // we also need to hide keyboard
            et.hideKeyboard();
         } else {
            // make us add another popover
            et.startManualMode();
         }
      } else {
         hide();
      }
   }
   @Override public void onHashTyped(boolean start) {
      show();
   }
   @Override public void onManualModeChanged(boolean enabled) {}

   public static class Adapter extends BaseAdapter {

      ArrayList<String> items = new ArrayList<String>();
      ArrayList<String> selectedItems = new ArrayList<String>();
      LayoutInflater li;
      ViewGroup vg;

      public Adapter(ViewGroup vg) {
         //Resources r = ApplicationController.instance().getResources();
         //_16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
         this.vg = vg;
      }

      public void setItems(ArrayList<String> items) {
         if (items == null) {
            items = new ArrayList<String>();
         }
         this.items = filterOut(items);
         notifyDataSetChanged();
      }

      public void setSelectedItems(ArrayList<String> selectedItems) {
         this.selectedItems = selectedItems;
         setItems(this.items);
      }

      private ArrayList<String> filterOut(final ArrayList<String> items) {
         final ArrayList<String> selectedItems = this.selectedItems;
         if (items == null || selectedItems == null) return items;
         ArrayList<String> filtered = new ArrayList<>();
         for (String item : items) {
            if (!selectedItems.contains(item)) {
               filtered.add(item);
            }
         }
         return filtered;
      }

      @Override public int getCount() { return items.size(); }
      @Override public Object getItem(int position) { return items.get(position); }
      @Override public long getItemId(int position) { return position; }

      @Override
      public View getView(final int position, View c, ViewGroup parent) {
         // boilerplate code
         if (li == null) {
            li = LayoutInflater.from(parent.getContext());
         }
         ViewHolder h;
         if (c == null) {
            h = new ViewHolder();
            c = li.inflate(R.layout.autocomplete_row, null);
            h.title = (TextView)c.findViewById(R.id.title);
            c.setTag(h);
         } else {
            h = (ViewHolder)c.getTag();
         }
         String text = items.get(position);
         // attach data
         h.title.setText(text);
         h.title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               onItemClickListener.onItemClick(null, v, position, 0);
            }
         });

         return c;
      }

      public static class ViewHolder {
         TextView title;
      }

      @Override
      public void notifyDataSetChanged() {
         vg.removeAllViews();
         for (int i = 0; i < items.size(); i++) {
            View view = getView(i, null, vg);
            final int pos = i;
            vg.addView(view);
         }
      }

      public AdapterView.OnItemClickListener onItemClickListener;
   }

   public AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         String textToAdd = adapter.items.get(position);

         if (et.isManualModeOn()) {
            et.cancelManualMode();
         }
         et.addBubble(textToAdd, et.getSelectionStart(), "CLICKED ITEM");
         et.append(" ");
      }
   };

   OnVisibilityChangedListener onVisibilityChangedListener;
   public void setOnVisibilityChanged(OnVisibilityChangedListener onVisibilityChangedListener) {
      this.onVisibilityChangedListener = onVisibilityChangedListener;
   }

   public interface OnVisibilityChangedListener {
      public void onVisibilityChanged(boolean visible);
   }

   public void scrollToTop() {
      scrollView.scrollTo(0, 0);
   }

   public void setBackgroundColor(int color) {
      this.bgColor = color;
   }

   int xTriOffset;

   @Override
   protected void dispatchDraw(Canvas canvas) {
      int x_start = 0;
      int y_start = getPaddingTop();

      int x_end = getWidth();
      int y_end = getHeight();

      int tri_h = getPaddingTop();

      int tri_base = (int)(Math.tan(triAngle / 2) * tri_h);

      Path path = new Path();
      path.moveTo(x_start, y_start);

      path.lineTo(xTriOffset - tri_base, y_start);
      path.lineTo(xTriOffset, y_start - tri_h);
      path.lineTo(xTriOffset + tri_base, y_start);

      path.lineTo(x_end, y_start);
      path.lineTo(x_end, y_end);
      path.lineTo(x_start, y_end);

      path.close();

      canvas.drawPath(path, bgPaint);

      super.dispatchDraw(canvas);
   }

   ArrayList<String> getSelectedItems() {
      ArrayList<String> selectedTags = new ArrayList<>();

      try {
         BubbleSpan[] spans = et.getText().getSpans(0, et.getText().length(), BubbleSpan.class);
         for (BubbleSpan span : spans) {
            BubbleSpanImpl impl = (BubbleSpanImpl) span;
            String tag = impl.bubble.text().trim();
            if (!selectedTags.contains(tag)) {
               selectedTags.add(tag);
            }
         }
      } catch (Exception e) {}

      return selectedTags;
   }

   public interface Resolver {
      public ArrayList<String> getSuggestions(String query) throws Exception;
      public ArrayList<String> getDefaultSuggestions();
   }
}
