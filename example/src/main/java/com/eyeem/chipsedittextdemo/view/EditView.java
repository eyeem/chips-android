package com.eyeem.chipsedittextdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eyeem.chips.AutocompletePopover;
import com.eyeem.chips.BubbleSpan;
import com.eyeem.chips.ChipsEditText;
import com.eyeem.chips.ChipsTextView;
import com.eyeem.chips.DefaultBubbles;
import com.eyeem.chips.Linkify;
import com.eyeem.chips.Regex;
import com.eyeem.chips.Utils;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.screen.Edit;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import mortar.Mortar;

/**
 * Created by vishna on 27/01/15.
 */
public class EditView extends RelativeLayout {

   public static final int MIN_FONT_SIZE = 10;
   public static final int MAX_FONT_SIZE = 24;

   @Inject Edit.Presenter presenter;
   @Inject Picasso picasso;
   @Inject List<String> suggestions;

   @InjectView(R.id.chipsMultiAutoCompleteTextview1) ChipsEditText et;
   @InjectView(R.id.chipsTextView) ChipsTextView tv;
   @InjectView(R.id.popover) AutocompletePopover popover;
   @InjectView(R.id.edit) Button edit;
   @InjectView(R.id.seek_bar) SeekBar textSizeSeekBar;
   @InjectView(R.id.spacing_seek_bar) SeekBar spacingSizeSeekBar;
   @InjectView(R.id.text_size) TextView fontSize;
   @InjectView(R.id.spacing_size) TextView spacingSize;
   @InjectView(R.id.debug_check) CheckBox debugCheck;

   public EditView(Context context) {
      super(context);
      init();
   }

   public EditView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public EditView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP) public EditView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
      init();
   }

   private void init() {
      Mortar.getScope(getContext()).<Edit.Component>getObjectGraph().inject(this);
      setSaveEnabled(true);
   }

   @Override protected void onFinishInflate() {
      super.onFinishInflate();
      ButterKnife.inject(this, this);
      setup();
   }

   @Override protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      presenter.takeView(this);
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
   }

   public void setup() {
      picasso
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
      textSizeSeekBar.setMax(MAX_FONT_SIZE - MIN_FONT_SIZE);
      textSizeSeekBar.setProgress(MAX_FONT_SIZE - 18);
      textSizeSeekBar.setOnSeekBarChangeListener(seekListener);
      spacingSizeSeekBar.setMax(10);
      spacingSizeSeekBar.setProgress(1);
      spacingSizeSeekBar.setOnSeekBarChangeListener(seekListener);

      et.setAutocomplePopover(popover);
      et.setMaxBubbleCount(4);
      et.setLineSpacing(1.0f, 1.25f);
      popover.setChipsEditText(et);


      final ArrayList<String> availableItems = new ArrayList<String>(suggestions);
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

   @OnClick(R.id.edit) public void toggleEdit(View view) {
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

   @OnClick(R.id.tag_setup) public void tagSetup(View view) {
      Toast.makeText(getContext(), Utils.tag_setup(et), Toast.LENGTH_LONG).show();
   }

   @OnClick(R.id.check) public void update(View view) {
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
            DefaultBubbles.get(DefaultBubbles.GRAY_WHITE_TEXT, getContext(), tv.getTextSize()), null, e);
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
         Toast.makeText(getContext(), "onHashTyped, start = "+start, Toast.LENGTH_SHORT).show();
      }
   };

///// boiler plate code that makes saving state work http://trickyandroid.com/saving-android-view-state-correctly/
   @Override
   public Parcelable onSaveInstanceState() {
      Parcelable superState = super.onSaveInstanceState();
      SavedState ss = new SavedState(superState);
      ss.childrenStates = new SparseArray();
      for (int i = 0; i < getChildCount(); i++) {
         getChildAt(i).saveHierarchyState(ss.childrenStates);
      }
      return ss;
   }

   @Override
   public void onRestoreInstanceState(Parcelable state) {
      SavedState ss = (SavedState) state;
      super.onRestoreInstanceState(ss.getSuperState());
      for (int i = 0; i < getChildCount(); i++) {
         getChildAt(i).restoreHierarchyState(ss.childrenStates);
      }
   }

   @Override
   protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
      dispatchFreezeSelfOnly(container);
   }

   @Override
   protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
      dispatchThawSelfOnly(container);
   }

   static class SavedState extends BaseSavedState {
      SparseArray childrenStates;

      SavedState(Parcelable superState) {
         super(superState);
      }

      private SavedState(Parcel in, ClassLoader classLoader) {
         super(in);
         childrenStates = in.readSparseArray(classLoader);
      }

      @Override
      public void writeToParcel(Parcel out, int flags) {
         super.writeToParcel(out, flags);
         out.writeSparseArray(childrenStates);
      }

      public static final ClassLoaderCreator<SavedState> CREATOR
         = new ClassLoaderCreator<SavedState>() {
         @Override
         public SavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new SavedState(source, loader);
         }

         @Override
         public SavedState createFromParcel(Parcel source) {
            return createFromParcel(null);
         }

         public SavedState[] newArray(int size) {
            return new SavedState[size];
         }
      };
   }
}
