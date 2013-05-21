package com.eyeem.chips;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

public class AwesomeBubbles extends View {

   //private EyeemPhoto mEyeemPhoto;
   private BubbleStyles myStyles;
   private BubbleLayout bubble_layout;
   private AbstractMap<Integer, BubbleLayout> cache;

   public AwesomeBubbles(Context context, AttributeSet attrs) {this(context, attrs, 0);}
   public AwesomeBubbles(Context context) {this(context, null);}
   public AwesomeBubbles(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   public AwesomeBubbles setCache(AbstractMap<Integer, BubbleLayout> cache) {
      this.cache = cache;
      return this;
   }

   /*public AwesomeBubbles setPhoto(EyeemPhoto photo) {
      this.mEyeemPhoto = photo;
      bubble_layout = build(getWidth(), mEyeemPhoto, cache, myStyles);
      requestLayout();
      return this;
   }*/

   public AwesomeBubbles setMyStyles(BubbleStyles myDrawables) {
      this.myStyles = myDrawables;
      return this;
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      int action = event.getAction();
      int x = (int) event.getX();
      int y = (int) event.getY();

      x += getScrollX();
      y += getScrollY();

      switch (action) {
         case MotionEvent.ACTION_DOWN:
            selectBubble(x, y);
            break;
         case MotionEvent.ACTION_MOVE:
            if (selectedBubble != null) selectedBubble.setPressed(selectedBubble.rect.contains(x,y));
            break;
         case MotionEvent.ACTION_UP:
            if (selectedBubble != null && selectedBubble.rect.contains(x,y)) {
               selectedBubble.action(this);
            }
            // fall through
         case MotionEvent.ACTION_CANCEL:
            if (selectedBubble != null) selectedBubble.setPressed(false);
            selectedBubble = null;
            break;

         default:
            break;
      }
      invalidate();
      return true;
   }

   private BubbleLayout.Bubble selectedBubble;

   public void selectBubble(int x, int y) {
      for (BubbleLayout.Bubble bubble : bubble_layout.bubbles) {
         if (bubble.rect.contains(x, y)) {
            bubble.setPressed(true);
            selectedBubble = bubble;
            return;
         }
      }
   }

   // TODO optimize further, build layouts in separate thread while listview is idle
   /**
    * Creates bubble layout, static as we need to be sure EyeemPhoto instance doesn't change.
    * @param maxWidth layout maximum width
    * @param eyeemPhoto photo that we want bubbles for
    * @param cache not necessary but improves rendering
    * @param myStyles set of preloaded drawables with bubble backgrounds etc.
    * @return generated layout or null
    */
   /*private static BubbleLayout build(int maxWidth, EyeemPhoto eyeemPhoto, AbstractMap<Integer, BubbleLayout> cache, BubbleStyles myStyles) {
      if (maxWidth == 0) {
         return null;
      }

      if (eyeemPhoto == null) {
         return null;
      }

      BubbleLayout bubble_layout = null;
      if (cache != null) {
         BubbleLayout tmp = cache.get(eyeemPhoto.photoId);
         if (tmp != null) {
            bubble_layout = tmp;
            if (bubble_layout.getMaxWidth() == maxWidth)
               return bubble_layout;
         }
      }
      if (bubble_layout == null) {
         bubble_layout = new BubbleLayout(myStyles);
      }
      bubble_layout.clear().setMaxWidth(maxWidth);

      HashSet<Integer> tags = new HashSet<Integer>();
      HashSet<Integer> venues = new HashSet<Integer>();
      HashSet<Integer> events = new HashSet<Integer>();
      HashSet<Integer> cites = new HashSet<Integer>();
      HashSet<Integer> counties = new HashSet<Integer>();

      int i=0;
      for (EyeemAlbum album : eyeemPhoto.albums) {
         if (album.type.equals(EyeemAlbum.TYPE_TAG))
            tags.add(i);
         if (album.type.equals(EyeemAlbum.TYPE_VENUE))
            venues.add(i);
         if (album.type.equals(EyeemAlbum.TYPE_EVENT))
            events.add(i);
         if (album.type.equals(EyeemAlbum.TYPE_CITY))
            cites.add(i);
         if (album.type.equals(EyeemAlbum.TYPE_COUNTRY))
            counties.add(i);

         i++;
      }

      if(i == 0){
         return null;
      }

      for (Integer tag:tags) {
         bubble_layout.append(eyeemPhoto.albums.get(tag), BubbleStyles.LILA);
      }

      Vector<Integer> places=new Vector<Integer>();

      for (Integer city:cites)
         places.add(city);

      for (Integer coutry:counties)
         places.add(coutry);

      boolean shouldBeWhite = TextUtils.isEmpty(eyeemPhoto.title);
      if (venues.size() > 0){
         bubble_layout.beginUnbreakable(); // at
         bubble_layout.append("at", shouldBeWhite ? BubbleStyles.GRAY_WHITE_TEXT : BubbleStyles.GRAY);

         for (Integer venue:venues)
            bubble_layout.append(eyeemPhoto.albums.get(venue), BubbleStyles.GREEN);
         bubble_layout.endUnbreakable();
      }

      if (places.size() > 0) {
         bubble_layout.ratherNewLine().beginUnbreakable();
         for (int j = 0; j < places.size(); j++) {
            bubble_layout.append(eyeemPhoto.albums.get(places.get(j)), BubbleStyles.CITY_COUNTRY);

            if (places.size() > 1 && j != places.size()-1) {
               bubble_layout.append(", ", BubbleStyles.CITY_COUNTRY);
            }
         }
         bubble_layout.endUnbreakable();
      }

      if ( events.size() > 0 ) {
         bubble_layout.beginUnbreakable();
         // TODO check if there could be more than one event

         for (Integer event:events)
            bubble_layout.append(eyeemPhoto.albums.get(event), BubbleStyles.PINK);

         bubble_layout.endUnbreakable();
      }
      if (cache != null) {
         cache.put(eyeemPhoto.photoId, bubble_layout);
      }
      return bubble_layout;
   }*/

   @Override
   protected void onDraw(Canvas canvas) {
      if (bubble_layout != null)
         bubble_layout.draw(canvas);
   }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);

      int width = widthSize;
      //bubble_layout = build(width, mEyeemPhoto, cache, myStyles);
      int height = bubble_layout == null ? 0 : bubble_layout.getHeight();

      this.setMeasuredDimension(width, height);
   }

   public final static TextPaint text_paint;

   static {
      text_paint = new TextPaint();
      text_paint.setColor(Color.WHITE);
      text_paint.setAntiAlias(true);
      text_paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
   }

   public static class BubbleLayout {

      public BubbleLayout (BubbleStyles myStyles) {
         this.myStyles = myStyles;
      }

      public class Bubble {
         String text;
         Rect rect;
         StaticLayout textLayout;
         int styleType;
         boolean isPressed;
         boolean isFullWidth;
         // EyeemAlbum album;
         LinearGradient text_shader;

         BubbleStyle style() {
            return myStyles.get(styleType);
         };

         public Bubble (String text, int maxWidth, int styleType) {
            this.styleType = styleType;
            this.text = text;
            SpannableStringBuilder main = new SpannableStringBuilder();
            main.append(text);
            main.setSpan(new StyleSpan(Typeface.BOLD), 0, main.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text_paint.setTextSize(style().textSize);

            int best_w = (int) Math.min(maxWidth-2*style().bubblePadding, StaticLayout.getDesiredWidth(main, text_paint));
            textLayout = new StaticLayout(main, text_paint, best_w, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
            this.isFullWidth = (best_w+2*style().bubblePadding >= max_width);
         }

         // public Bubble (EyeemAlbum album, int maxWidth, int style) {
         //   this(album.name, maxWidth, style);
         //   this.album = album;
         //}

         public Bubble resetWidth(int width) {
            textLayout = new StaticLayout(textLayout.getText(), text_paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
            return this;
         }

         public Bubble makeOneLiner() { // NOTE: unreversible
            text_paint.setTextSize(style().textSize);
            int width = textLayout.getWidth();
            int i = text_paint.breakText(textLayout.getText().toString(), true, (float)width, null);
            textLayout = new StaticLayout(textLayout.getText().subSequence(0, i), text_paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 1, false);
            text_shader = new LinearGradient(width-style().bubblePadding*4, 0, width, 0,
               new int[]{style().textColor, 0x00ffffff},
               new float[]{0, 1}, TileMode.CLAMP);
            return this;
         }

         public int getWidth() {
            return textLayout.getWidth() + 2*style().bubblePadding;
         }

         public int getHeight() {
            return textLayout.getHeight() + 2*style().bubblePadding;
         }

         public void setPosition(int x, int y) {
            rect = new Rect(x, y, x+textLayout.getWidth()+2*style().bubblePadding, y+textLayout.getHeight()+2*style().bubblePadding);
         }

         public void draw(Canvas canvas) {
            BubbleStyle style = style();
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
            textLayout.draw(canvas);
            canvas.translate(-rect.left-style.bubblePadding, -rect.top-style.bubblePadding);
         }

         public int h_spacing() {
            return style().nextNeedsSpacing ? myStyles.h_spacing : 0;
         }

         public void setPressed(boolean value) {
            this.isPressed = value;
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
      }

      private ArrayList<Bubble> bubbles = new ArrayList<Bubble>();
      int current_x;
      int current_y;
      int max_width;
      BubbleStyles myStyles;

      public BubbleLayout clear() {
         bubbles.clear();
         current_x = 0;
         current_y = 0;
         return this;
      }

      public BubbleLayout setMaxWidth(int max_width) {
         this.max_width = max_width;
         return this;
      }

      public int getMaxWidth() {
         return this.max_width;
      }

      private ArrayList<Bubble> unbreakables;

      public BubbleLayout beginUnbreakable() {
         unbreakables = new ArrayList<Bubble>();
         return this;
      }

      public BubbleLayout endUnbreakable() {
         int totalWidth = 0;
         for (int i=0; i<unbreakables.size(); i++) {
            Bubble bubble = unbreakables.get(i);
            totalWidth += bubble.getWidth();
            if (i != unbreakables.size()-1)
               totalWidth += bubble.h_spacing();
         }

         if (current_x + totalWidth > max_width) {
            current_x = 0;
            current_y = getHeight() + myStyles.v_spacing;
         }

         for (Bubble bubble : unbreakables) {
            if (current_x < max_width && current_x + bubble.getWidth()  > max_width) {
               bubble.resetWidth(max_width - current_x - 2*bubble.style().bubblePadding).makeOneLiner();
            }
            bubble.setPosition(current_x, current_y);
            bubbles.add(bubble);
            current_x += bubble.getWidth() + bubble.h_spacing();
            if (current_x > max_width) { // not so unbreakable
               current_x = 0;
               current_y = getHeight() + myStyles.v_spacing;
            }
         }

         unbreakables = null;
         return this;
      }

      /**
       * Won't create a new line if already created one somehow
       * @return
       */
      public BubbleLayout ratherNewLine() {
         if (current_x > 0 && getHeight() > 0) {
            current_x = 0;
            current_y = getHeight() + myStyles.v_spacing;
         }
         return this;
      }

      public BubbleLayout append(String text, int styleType) {
         Bubble bubble = new Bubble(text, max_width, styleType);
         return append(bubble);
      }

      //public BubbleLayout append(EyeemAlbum album, int styleType) {
      //   Bubble bubble = new Bubble(album, max_width, styleType);
      //   return append(bubble);
      //}

      public BubbleLayout append(Bubble bubble) {
         if (unbreakables != null) {
            unbreakables.add(bubble);
            return this;
         }

         if (bubble.isFullWidth) {
            bubble.makeOneLiner();
         }

         if (current_x + bubble.getWidth() + bubble.h_spacing() > max_width) {
            current_x = 0;
            current_y = getHeight() + myStyles.v_spacing;
         }
         bubble.setPosition(current_x, current_y);
         bubbles.add(bubble);
         current_x += bubble.getWidth() + bubble.h_spacing();
         return this;
      }

      public void draw(Canvas canvas) {
         for (Bubble bubble : bubbles)
            bubble.draw(canvas);
      }

      public int getHeight() {
         int h = 0;
         for (Bubble bubble : bubbles)
            h = Math.max(h, bubble.rect.bottom);
         return h;
      }
   }

   public static class BubbleStyle {
      Drawable active;
      Drawable pressed;
      int textSize;
      int textColor;
      int textPressedColor;
      int bubblePadding;
      boolean nextNeedsSpacing;
      public BubbleStyle(Drawable active, Drawable pressed, int textSize,
                         int textColor, int textPressedColor, int bubblePadding) {
         this(active, pressed, textSize, textColor, textPressedColor, bubblePadding, true);
      }
      public BubbleStyle(Drawable active, Drawable pressed, int textSize,
                         int textColor, int textPressedColor, int bubblePadding, boolean nextNeedsSpacing) {
         this.active = active;
         this.pressed = pressed;
         this.textSize = textSize;
         this.textColor = textColor;
         this.textPressedColor = textPressedColor;
         this.bubblePadding = bubblePadding;
         this.nextNeedsSpacing = nextNeedsSpacing;
      }
   }

   /**
    * Try to allocate once per activity life-cycle. Never make static
    * as drawables leak context.
    * @author vishna
    */
   public static class BubbleStyles {
      public static int LILA = 0;
      public static int GRAY = 1;
      public static int GRAY_WHITE_TEXT = 2;
      public static int PINK = 3;
      public static int GREEN = 4;
      public static int CITY_COUNTRY = 5;

      private BubbleStyle array[];

      public int v_spacing;
      public int h_spacing;

      public BubbleStyle get(int type) {
         return array[type];
      }

      public BubbleStyles(Context context) {
         context = context.getApplicationContext();
         int textSize = context.getResources().getDimensionPixelSize(R.dimen.bubble_text_size);
         int padding = context.getResources().getDimensionPixelSize(R.dimen.bubble_padding);
         v_spacing = context.getResources().getDimensionPixelSize(R.dimen.bubble_v_spacing);
         h_spacing = context.getResources().getDimensionPixelSize(R.dimen.bubble_h_spacing);

         // FIXME load colors from resources!!1
         array = new BubbleStyle[] {
            new BubbleStyle(
               context.getResources().getDrawable(R.drawable.lilatext_background_active),
               context.getResources().getDrawable(R.drawable.lilatext_background_pressed),
               textSize, 0xffebf5e0, 0xffebf5e0, padding), // LILA
            new BubbleStyle(
               context.getResources().getDrawable(R.drawable.greybubble_background),
               context.getResources().getDrawable(R.drawable.greybubble_background),
               textSize, 0xff404040, 0xff404040, padding), // GRAY
            new BubbleStyle(
               context.getResources().getDrawable(R.drawable.greybubble_background),
               context.getResources().getDrawable(R.drawable.greybubble_background),
               textSize, Color.WHITE, Color.WHITE, padding), // GRAY_WHITE_TEXT
            new BubbleStyle(
               context.getResources().getDrawable(R.drawable.pinktext_background_active),
               context.getResources().getDrawable(R.drawable.pinktext_background_pressed),
               textSize, 0xffffedff, 0xff574f57, padding), // PINK
            new BubbleStyle(
               context.getResources().getDrawable(R.drawable.greentext_background_active),
               context.getResources().getDrawable(R.drawable.greentext_background_pressed),
               textSize, 0xffebe0f5, 0xffebe0f5, padding), // GREEN
            new BubbleStyle(
               null, null,
               context.getResources().getDimensionPixelSize(R.dimen.bubble_country_text_size),
               0xffcccccc, 0xff000000, 0, false) // CITY_COUNTRY
         };
      }
   }
}
