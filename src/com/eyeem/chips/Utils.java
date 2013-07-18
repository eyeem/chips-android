package com.eyeem.chips;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

public class Utils {
   public static String flatten(EditText editor, HashMap<Class<?>, FlatteningFactory> factories) {
      try {
         return flatten(editor.getText(), factories);
      } catch (IndexOutOfBoundsException ioobe) {
         // known platform issue https://code.google.com/p/android/issues/detail?id=5164
         // workaround is to set seletion to the end
         editor.setSelection(editor.getText().length(), editor.getText().length());
         return flatten(editor.getText(), factories);
      }
   }

   private static String flatten(CharSequence charSequence, HashMap<Class<?>, FlatteningFactory> factories) {
      Editable out = new SpannableStringBuilder(charSequence);
      for (Map.Entry<Class<?>, FlatteningFactory> e : factories.entrySet()) {
         Object spans[] = out.getSpans(0, out.length(), e.getKey());
         for (Object span : spans) {
            int start = out.getSpanStart(span);
            int end = out.getSpanEnd(span);
            String in = out.subSequence(start, end).toString();
            out.replace(start, end, e.getValue().out(in));
         }
      }
      return out.toString();
   }

   public interface FlatteningFactory {
      public String out(String in);
   }

   public static void bubblify(Editable editable, String text, int start, int end,
                               int maxWidth, BubbleStyle bubbleStyle, ChipsEditText et, Object data) {
      if (text == null) {
         text = editable.toString();
         if (start < 0)
            start = 0;
         if (end > text.length())
            end = text.length();
         text = text.substring(start, end);
      }

      // create bitmap drawable for ReplacementSpan
      TextPaint tp = new TextPaint();
      AwesomeBubble bubble = new AwesomeBubble(text, maxWidth, bubbleStyle, tp);

      // create and set ReplacementSpan
      ReplacementSpan[] spansToClear = editable.getSpans(start, end, ReplacementSpan.class);
      for (ReplacementSpan span : spansToClear)
         editable.removeSpan(span);
      BubbleSpan span = et == null ? new BubbleSpanImpl(bubble) : new BubbleSpanImpl(bubble, et);
      span.setData(data);
      editable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
   }

   public static void tapify(Editable editable, int start, int end, int activeColor, int inactiveColor, Object data) {
      BubbleSpan span = new TapableSpan(activeColor, inactiveColor);
      span.setData(data);
      editable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      span.setPressed(false, editable);
   }
}
