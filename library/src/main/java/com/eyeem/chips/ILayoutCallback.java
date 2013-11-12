package com.eyeem.chips;

import android.graphics.Point;
import android.text.Spannable;

public interface ILayoutCallback {
   public Point getCursorPosition(int pos);
   public int getLine(int pos);
   public Spannable getSpannable();
   public int getLineEnd(int line);
   public int getLineHeight();
}
