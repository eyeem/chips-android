package com.eyeem.chips;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;

import java.util.ArrayList;

public class AutocompletePopover extends RelativeLayout {

   ListView lv;
   RelativeLayout root;
   ChipsEditText et;
   Adapter adapter = new Adapter();
   View tri;

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
      lv = (ListView)findViewById(R.id.suggestions);
      tri = findViewById(R.id.triangle);
      lv.setAdapter(adapter);
      lv.setOnItemClickListener(onItemClickListener);
      setVisibility(View.GONE);
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
      int verticaloffset = -(int)(et.getHeight() - p.y - 2*et.getTextSize());
      int h = root().getHeight() - et.getHeight() - verticaloffset;
      ((RelativeLayout.LayoutParams)getLayoutParams()).topMargin = et.getHeight() + verticaloffset;
      getLayoutParams().height = h;
      ((LayoutParams)tri.getLayoutParams()).leftMargin = p.x;
   }

   public void show() {
      reposition();
      setVisibility(View.VISIBLE);
   }

   public void hide() {
      setVisibility(View.GONE);
      et.endManualMode();
   }

   public static class Adapter extends BaseAdapter {

      ArrayList<String> items = new ArrayList<String>();
      LayoutInflater li;

      public Adapter() {
         //Resources r = ApplicationController.instance().getResources();
         //_16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
      }

      public void setItems(ArrayList<String> items) {
         this.items = items;
         notifyDataSetChanged();
      }

      @Override public int getCount() { return items.size(); }
      @Override public Object getItem(int position) { return items.get(position); }
      @Override public long getItemId(int position) { return position; }

      @Override
      public View getView(int position, View c, ViewGroup parent) {
         ((ListView)parent).setBackgroundColor(0x77000000);
         ((ListView)parent).setDivider(null);
         ((ListView)parent).setSelector(new ColorDrawable(0x0));
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

         return c;
      }

      public static class ViewHolder {
         TextView title;
      }
   }

   public AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         et.manualModeOn = false;
         String textToAdd = adapter.items.get(position);
         if (et.lastEditAction != null) {
            try {
               Editable t = et.getText();
               if (t.subSequence(et.lastEditAction.start, et.lastEditAction.end()).toString().equals(et.lastEditAction.text)) {
                  t.delete(et.lastEditAction.start, et.lastEditAction.end());
               }
            } catch (Exception e) {}
         }
         et.addBubble(textToAdd, et.getSelectionStart());
         if (et.getSelectionEnd() == et.length()) {
            et.append(" ");
         }
         hide();
      }
   };
}
