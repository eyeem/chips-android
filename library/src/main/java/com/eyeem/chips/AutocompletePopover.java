package com.eyeem.chips;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.asolutions.widget.RowLayout;

import java.util.ArrayList;

public class AutocompletePopover extends RelativeLayout {

   ViewGroup vg;
   RelativeLayout root;
   ChipsEditText et;
   Adapter adapter;
   ScrollView scrollView;
   InputMethodManager imm;
   int bgColor = 0xFF000000;
   double triAngle = Math.PI / 2.0; // 90 degrees
   Paint bgPaint;

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
      boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
      if (isLandscape) {
         vg = new RowLayout(getContext(), null);
      } else {
         vg = new LinearLayout(getContext());
         ((LinearLayout) vg).setOrientation(LinearLayout.VERTICAL);
      }
      scrollView.addView(vg, -2, -2);
      adapter = new Adapter(vg);
      adapter.onItemClickListener = onItemClickListener;
      setVisibility(View.GONE);
      OnClickListener x = new OnClickListener() {
         @Override
         public void onClick(View v) {
            et.onXPressed();
            et.cancelManualMode();
         }
      };
      findViewById(R.id.x_border).setOnClickListener(x);

      imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      bgPaint = new Paint();
      bgPaint.setAntiAlias(true);
      bgPaint.setColor(bgColor);
      bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
   }

   public void setChipsEditText(ChipsEditText et) {
      this.et = et;
   }

   public void setItems(ArrayList<String> items) {
      adapter.setItems(items);
   }

   public RelativeLayout root() {
      if (root == null) {
         root = (RelativeLayout) getParent();
         root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
               reposition();
            }
         });
      }
      return root;
   }

   public void reposition() {
      Point p = et.getCursorPosition();
      Point bOff = et.cursorDrawable.bubble_offset();
      p.offset(-bOff.x, -bOff.y);
      p.y -= et.getScrollY();

      int topMargin = et.getTop() + p.y;
      if (topMargin > et.getBottom()) topMargin = et.getBottom();
      ((RelativeLayout.LayoutParams)getLayoutParams()).topMargin = topMargin;
      getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
      xTriOffset = p.x + et.getLeft();
      invalidate();
   }

   public void show() {
      if (!et.canAddMoreBubbles())
         return;
      reposition();
      setVisibility(View.VISIBLE);
   }

   public boolean isHidden() {
      return getVisibility() == View.GONE;
   }

   public void hide() {
      setVisibility(View.GONE);
      if (et.manualModeOn)
         et.endManualMode();
      if (onHideListener != null)
         onHideListener.onHide(this);
   }

   public static class Adapter extends BaseAdapter {

      ArrayList<String> items = new ArrayList<String>();
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
         this.items = items;
         notifyDataSetChanged();
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
         et.onBubbleSelected(position);
         et.manualModeOn = false;
         et.muteHashWatcher(true);
         String textToAdd = adapter.items.get(position);
         if (et.lastEditAction != null) {
            try {
               Editable t = et.getText();
               if (t.subSequence(et.lastEditAction.start, et.lastEditAction.end()).toString().equals(et.lastEditAction.text)) {
                  t.delete(et.lastEditAction.start, et.lastEditAction.end());
               }
            } catch (Exception e) {}
         }
         et.addBubble(textToAdd, et.manualStart);
         if (et.getSelectionEnd() == et.length() || et.getSelectionEnd() + 1 == et.length()) {
            et.append(" ");

            imm.restartInput(et); // reset suggestions
         }
         hide();
         et.muteHashWatcher(false);
      }
   };

   OnHideListener onHideListener;
   public void setOnHideListener(OnHideListener onHideListener) {
      this.onHideListener = onHideListener;
   }

   public interface OnHideListener {
      public void onHide(View view);
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
}
