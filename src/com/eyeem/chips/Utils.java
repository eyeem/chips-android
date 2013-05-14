package com.eyeem.chips;

import android.text.Editable;
import android.text.SpannableStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class Utils {
   public static String flatten(CharSequence charSequence, HashMap<Class<?>, FlatteningFactory> factories) {
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
}
