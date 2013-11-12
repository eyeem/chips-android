package com.eyeem.chips;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Spannable;

import java.util.ArrayList;

public interface BubbleSpan {
   public void setPressed(boolean value, Spannable s);
   public void resetWidth(int width);
   public ArrayList<Rect> rect(ILayoutCallback callback);
   public void redraw(Canvas canvas);
   public void setData(Object data);
   public Object data();
}