package com.eyeem.chips;

import android.content.Context;
import android.graphics.Point;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;

import java.util.ArrayList;

public class AutocompletePopover extends RelativeLayout {

   LinearLayout ll;
   RelativeLayout root;
   ChipsEditText et;
   Adapter adapter;
   ImageView tri;


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
      ll = (LinearLayout)findViewById(R.id.suggestions_container);
      adapter = new Adapter(ll);
      adapter.onItemClickListener = onItemClickListener;
      tri = (ImageView)findViewById(R.id.triangle);
      setVisibility(View.GONE);
      OnClickListener x = new OnClickListener() {
         @Override
         public void onClick(View v) {
            et.onXPressed();
            et.cancelManualMode();
         }
      };
      findViewById(R.id.x).setOnClickListener(x);
      findViewById(R.id.x_border).setOnClickListener(x);
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
      LinearLayout ll;

      public Adapter(LinearLayout ll) {
         //Resources r = ApplicationController.instance().getResources();
         //_16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
         this.ll = ll;
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
         ll.removeAllViews();
         for (int i = 0; i < items.size(); i++) {
            View view = getView(i, null, ll);
            final int pos = i;
            ll.addView(view);
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
}
