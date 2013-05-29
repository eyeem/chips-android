package com.eyeem.chips;

import android.graphics.*;
import android.text.*;
import android.text.style.StyleSpan;
import android.view.View;

public class AwesomeBubble {
   String text;
   private Rect rect;
   StaticLayout textLayout;
   boolean isPressed;
   boolean isFullWidth;
   // EyeemAlbum album;
   LinearGradient text_shader;
   BubbleStyle style;
   TextPaint text_paint;

   public AwesomeBubble (String text, int containerWidth, BubbleStyle style, TextPaint text_paint) {
      this.style = style;
      this.text_paint = text_paint;
      this.text = text;
      SpannableStringBuilder main = new SpannableStringBuilder();
      main.append(text);
      main.setSpan(new StyleSpan(Typeface.BOLD), 0, main.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      text_paint.setTextSize(style.textSize);

      int maximum_w = containerWidth - 2*style.bubblePadding;
      int desired_w = (int)StaticLayout.getDesiredWidth(main, text_paint);
      int best_w = Math.min(maximum_w, desired_w);
      textLayout = new StaticLayout(main, text_paint, best_w, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
      if (desired_w > maximum_w)
         makeOneLiner();
      //this.isFullWidth = (best_w + 2*style.bubblePadding >= containerWidth);
      setPosition(0, 0);
   }

   public AwesomeBubble resetWidth(int width) {
      textLayout = new StaticLayout(textLayout.getText(), text_paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
      return this;
   }

   public AwesomeBubble makeOneLiner() {
      text_paint.setTextSize(style.textSize);
      int width = textLayout.getWidth();
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
      return textLayout.getWidth() + 2*style.bubblePadding;
   }

   public int getHeight() {
      return textLayout.getHeight() + 2*style.bubblePadding;
   }

   public void setPosition(int x, int y) {
      rect = new Rect(x, y, x+textLayout.getWidth()+2*style.bubblePadding, y+textLayout.getHeight()+2*style.bubblePadding);
   }

   public void draw(Canvas canvas) {
      if (isPressed && style.pressed != null) {
         style.pressed.setBounds(rect);
         style.pressed.draw(canvas);
      } else if (!isPressed && style.active != null){
         style.active.setBounds(rect);
         style.active.draw(canvas);
      }
      canvas.translate(rect.left+style.bubblePadding, rect.top+style.bubblePadding);
      text_paint.setTextSize(style.textSize);
      text_paint.setColor(isPressed ? style.textPressedColor : style.textColor);
      text_paint.setShader(text_shader);
      text_paint.setAntiAlias(true);
      textLayout.draw(canvas);
      canvas.translate(-rect.left-style.bubblePadding, -rect.top-style.bubblePadding);
   }

   public int h_spacing() {
      //return style.nextNeedsSpacing ? myStyles.h_spacing : 0;
      return 0;
   }

   public void setPressed(boolean value) {
      this.isPressed = value;
   }

   public String text() {
      return text;
   }

   public void action(View view) {
            /*if (album == null)
               return;
            if (EyeemAlbum.TYPE_TAG.equals(album.type))
               Track.button("topic tag");
            else if (EyeemAlbum.TYPE_VENUE.equals(album.type))
               Track.button("location tag");
            else if (EyeemAlbum.TYPE_CITY.equals(album.type))
               Track.button("city name");
            else if (EyeemAlbum.TYPE_COUNTRY.equals(album.type))
               Track.button("country name");
            else if (EyeemAlbum.TYPE_EVENT.equals(album.type))
               Track.button("event name");
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Intent myStreamIntent = new Intent(view.getContext(), AlbumViewPagerActivity.class);
            myStreamIntent.putExtra(AlbumPhotosFragment.EXTRA_KEY_ALBUM_ID, album.albumId);
            view.getContext().startActivity(myStreamIntent);*/
   }

   public Rect rect() {
      return rect;
   }
}
