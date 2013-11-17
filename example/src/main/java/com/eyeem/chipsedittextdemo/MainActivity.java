package com.eyeem.chipsedittextdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import android.app.Activity;

import android.content.res.Resources;
import android.os.Bundle;

import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.eyeem.chips.*;

public class MainActivity extends Activity {

   ChipsEditText et;
   ChipsTextView tv;
   RelativeLayout root;
   AutocompletePopover popover;
   Button edit;
   SeekBar seekBar;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.activity_main);


      // setting up chips exit text
      et = (ChipsEditText) findViewById(R.id.chipsMultiAutoCompleteTextview1);
      tv = (ChipsTextView) findViewById(R.id.chipsTextView);
      root = (RelativeLayout) findViewById(R.id.root);
      edit = (Button) findViewById(R.id.edit);
      popover = (AutocompletePopover)findViewById(R.id.popover);
      seekBar = (SeekBar)findViewById(R.id.seek_bar);
      seekBar.setMax(24 - 10);
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int calculatedProgress = 10 + progress;
            //Log.i("MainActivity", "progress = " + calculatedProgress);
            TextPaint paint = new TextPaint();
            Resources r = getResources();
            float _dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, calculatedProgress, r.getDisplayMetrics());
            paint.setAntiAlias(true);
            paint.setTextSize(_dp);
            paint.setColor(0xff000000); //black
            tv.setTextPaint(paint);
            update(tv);
         }

         @Override public void onStartTrackingTouch(SeekBar seekBar) {}
         @Override public void onStopTrackingTouch(SeekBar seekBar) {}
      });

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
            Toast.makeText(view.getContext(), ((Linkify.Entity)bubbleSpan.data()).text, Toast.LENGTH_LONG).show();
         }
      });
      root.requestFocus();
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
         Utils.bubblify(ssb, e.text, e.start, e.end, 0, DefaultBubbles.get(0, this, tv.getTextSize()), null, e);
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
}
