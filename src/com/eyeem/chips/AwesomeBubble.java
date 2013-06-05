package com.eyeem.chips;

import android.graphics.*;
import android.text.*;
import android.text.style.StyleSpan;

public class AwesomeBubble {
   String text;
   private Rect rect;
   StaticLayout textLayout;
   boolean isPressed;
   LinearGradient text_shader;
   BubbleStyle style;
   TextPaint text_paint;
   int containerWidth = 0;

   public AwesomeBubble (String text, int containerWidth, BubbleStyle style, TextPaint text_paint) {
      this.style = style;
      this.text_paint = text_paint;
      this.text = text;
      resetWidth(containerWidth);
   }

   public AwesomeBubble resetWidth(int containerWidth) {
      if (this.containerWidth == containerWidth)
         return this;
      this.containerWidth = containerWidth;
      SpannableStringBuilder main = new SpannableStringBuilder();
      main.append(text);
      main.setSpan(new StyleSpan(Typeface.BOLD), 0, main.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      text_paint.setTextSize(style.textSize);

      int maximum_w = containerWidth - 4*style.bubblePadding;
      int desired_w = (int)StaticLayout.getDesiredWidth(main, text_paint);
      int best_w = Math.max(Math.min(maximum_w, desired_w), 0);
      textLayout = new StaticLayout(main, text_paint, best_w, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
      text_shader = null;
      if (desired_w > maximum_w)
         makeOneLiner(maximum_w);
      setPosition(0, 0);
      return this;
   }

   public AwesomeBubble makeOneLiner(int width) {
      text_paint.setTextSize(style.textSize);
      int i = text_paint.breakText(textLayout.getText().toString(), true, (float)width, null);
      while (i > 0 && textLayout.getLineCount() > 1) {
         textLayout = new StaticLayout(textLayout.getText().subSequence(0, i), text_paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
         i--;
      }
      text_shader = new LinearGradient(width-style.bubblePadding*4, 0, width, 0,
         new int[]{style.textColor, 0x00ffffff},
         new float[]{0, 1}, Shader.TileMode.CLAMP);
      return this;
   }

   public int getWidth() {
      return textLayout == null ? 0 : textLayout.getWidth() + 4*style.bubblePadding;
   }

   public int getHeight() {
      return textLayout == null ? 0 : textLayout.getHeight() + 2*style.bubblePadding;
   }

   public void setPosition(int x, int y) {
      rect = new Rect(x, y, x + getWidth(), y + getHeight());
   }

   public void draw(Canvas canvas) {
      if (textLayout == null)
         return;
      if (isPressed && style.pressed != null) {
         style.pressed.setBounds(rect);
         style.pressed.draw(canvas);
      } else if (!isPressed && style.active != null){
         style.active.setBounds(rect);
         style.active.draw(canvas);
      }
      canvas.translate(rect.left+2*style.bubblePadding, rect.top+style.bubblePadding);
      text_paint.setTextSize(style.textSize);
      text_paint.setColor(isPressed ? style.textPressedColor : style.textColor);
      text_paint.setShader(text_shader);
      text_paint.setAntiAlias(true);
      textLayout.draw(canvas);
      canvas.translate(-rect.left-2*style.bubblePadding, -rect.top-style.bubblePadding);
   }

   public void setPressed(boolean value) {
      this.isPressed = value;
   }

   public String text() {
      return text;
   }

   public Rect rect() {
      return rect;
   }
}
