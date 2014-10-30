package com.eyeem.chipsedittextdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import android.app.Activity;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.eyeem.chips.*;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity {

   ChipsEditText et;
   ChipsTextView tv;
   RelativeLayout root;
   AutocompletePopover popover;
   Button edit;
   SeekBar textSizeSeekBar;
   SeekBar spacingSizeSeekBar;
   TextView fontSize;
   TextView spacingSize;
   CheckBox debugCheck;

   public static final int MIN_FONT_SIZE = 10;
   public static final int MAX_FONT_SIZE = 24;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.activity_main);

      Picasso
         .with(this)
         .load("http://cdn.eyeem.com/thumb/h/800/6df34d42fa813b926f24cf9d32d49eea779cc014-1405682234")
         .placeholder(new ColorDrawable(0xffaaaaaa))
         .into((ImageView) findViewById(R.id.bg));

      // chips debug
      debugCheck = (CheckBox)findViewById(R.id.debug_check);
      debugCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ChipsTextView.DEBUG = isChecked;
            updateTextProperties();
         }
      });

      // setting up chips exit text
      fontSize = (TextView) findViewById(R.id.text_size);
      et = (ChipsEditText) findViewById(R.id.chipsMultiAutoCompleteTextview1);
      tv = (ChipsTextView) findViewById(R.id.chipsTextView);
      root = (RelativeLayout) findViewById(R.id.root);
      edit = (Button) findViewById(R.id.edit);
      popover = (AutocompletePopover)findViewById(R.id.popover);
      textSizeSeekBar = (SeekBar)findViewById(R.id.seek_bar);
      textSizeSeekBar.setMax(MAX_FONT_SIZE - MIN_FONT_SIZE);
      textSizeSeekBar.setProgress(MAX_FONT_SIZE - 18);
      textSizeSeekBar.setOnSeekBarChangeListener(seekListener);
      spacingSizeSeekBar = (SeekBar)findViewById(R.id.spacing_seek_bar);
      spacingSizeSeekBar.setMax(10);
      spacingSizeSeekBar.setProgress(1);
      spacingSizeSeekBar.setOnSeekBarChangeListener(seekListener);
      spacingSize = (TextView) findViewById(R.id.spacing_size);

      et.setAutocomplePopover(popover);
      et.setMaxBubbleCount(4);
      et.setLineSpacing(1.0f, 1.25f);
      popover.setChipsEditText(et);


      final ArrayList<String> availableItems = new ArrayList<String>();
      availableItems.add("Changing the world");
      availableItems.add("Best startup ever");
      availableItems.add("Lars but not least");
      availableItems.add("My Ramzi");
      availableItems.add("Walking around");
      availableItems.add("Today's hot look");
      availableItems.add("In other news");
      availableItems.add("Screw that stuff");
      availableItems.add("x");
      availableItems.add("NADA NADA NADA");
      availableItems.add("...");
      availableItems.add("IPA");
      availableItems.add("#flowers#nature#hangingout#takingphotos#colors#hello world#flora#fauna");
      et.setAutocompleteResolver(new ChipsEditText.AutocompleteResolver() {
         @Override
         public ArrayList<String> getSuggestions(String query) throws Exception {
            return new ArrayList<String>();
         }

         @Override
         public ArrayList<String> getDefaultSuggestions() {
            return availableItems;
         }
      });
      et.addListener(chipsListener);

      TextPaint paint = new TextPaint();
      Resources r = getResources();
      float _dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
      paint.setAntiAlias(true);
      paint.setTextSize(_dp);
      paint.setColor(0xff000000); //black
      tv.setTextPaint(paint);

      tv.setOnBubbleClickedListener(new ChipsTextView.OnBubbleClickedListener() {
         @Override
         public void onBubbleClicked(View view, BubbleSpan bubbleSpan) {
            if (bubbleSpan.data() instanceof Truncation) {
               tv.expand(true);
            } else {
               Toast.makeText(view.getContext(), ((Linkify.Entity) bubbleSpan.data()).text, Toast.LENGTH_LONG).show();
            }
         }
      });
      SpannableStringBuilder moreText = new SpannableStringBuilder("... more");
      Utils.tapify(moreText, 0, moreText.length(), 0x77000000, 0xff000000, new Truncation());
      tv.setMaxLines(3, moreText);
      tv.requestFocus();
      updateTextProperties();

      et.getCursorDrawable().setColor(0xff00ff00);
   }

   public void updateTextProperties() {
      int calculatedProgress = MIN_FONT_SIZE + textSizeSeekBar.getProgress();
      float lineSpacing = 1.0f  + 0.25f * spacingSizeSeekBar.getProgress();

      TextPaint paint = new TextPaint();
      Resources r = getResources();
      float _dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, calculatedProgress, r.getDisplayMetrics());
      paint.setAntiAlias(true);
      paint.setTextSize(_dp);
      paint.setColor(0xff000000); //black
      tv.setTextPaint(paint);
      fontSize.setText(String.format("%ddp %.2fpx", calculatedProgress, _dp));
      spacingSize.setText(String.format("%.2f", lineSpacing));
      tv.setLineSpacing(lineSpacing);
      update(tv);
   }

   public void toggleEdit(View view) {
      et.resetAutocompleList();
      et.startManualMode();
      popover.show();
      et.postDelayed(new Runnable() {
         @Override
         public void run() {
            et.showKeyboard();
         }
      }, 100);
   }

   public void tagSetup(View view) {
      Toast.makeText(this, Utils.tag_setup(et), Toast.LENGTH_LONG).show();
   }

   public void update(View view) {
      // first flatten the text
      HashMap<Class<?>, Utils.FlatteningFactory> factories = new HashMap<Class<?>, Utils.FlatteningFactory>();
      factories.put(BubbleSpan.class, new AlbumFlatten());
      String flattenedText = Utils.flatten(et, factories);

      // scan to find bubble matches and populate text view accordingly
      Linkify.Entities entities = new Linkify.Entities();
      if (!TextUtils.isEmpty(flattenedText)) {
         Matcher matcher = Regex.VALID_BUBBLE.matcher(flattenedText);
         while (matcher.find()) {
            String bubbleText = matcher.group(1);
            Linkify.Entity entity = new Linkify.Entity(matcher.start(), matcher.end(),
               bubbleText, bubbleText, Linkify.Entity.ALBUM);
            entities.add(entity);
         }
      }

      // now bubblify text edit
      SpannableStringBuilder ssb = new SpannableStringBuilder(flattenedText);
      for (Linkify.Entity e : entities) {
         Utils.bubblify(ssb, e.text, e.start, e.end,
            tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight(),
            DefaultBubbles.get(DefaultBubbles.GRAY_WHITE_TEXT, this, tv.getTextSize()), null, e);
      }
      tv.setText(ssb);
   }

   public static class AlbumFlatten implements Utils.FlatteningFactory {
      @Override
      public String out(String in) {
         if (in != null && in.startsWith("#")) {
            in = in.substring(1, in.length());
         }
         return "[a:"+in+"]";
      }
   }

   SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         updateTextProperties();
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
   };

   // marker class
   public static class Truncation {}

   ChipsEditText.Listener chipsListener = new ChipsEditText.Listener() {
      @Override
      public void onBubbleCountChanged() {

      }

      @Override
      public void onActionDone() {

      }

      @Override
      public void onBubbleSelected(int position) {

      }

      @Override
      public void onXPressed() {

      }

      @Override
      public void onHashTyped(boolean start) {
         Toast.makeText(MainActivity.this, "onHashTyped, start = "+start, Toast.LENGTH_SHORT).show();
      }
   };
}
