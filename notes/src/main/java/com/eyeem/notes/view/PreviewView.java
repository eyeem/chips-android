package com.eyeem.notes.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eyeem.chips.BubbleSpan;
import com.eyeem.chips.ChipsEditText;
import com.eyeem.chips.ChipsTextView;
import com.eyeem.chips.DefaultBubbles;
import com.eyeem.chips.Linkify;
import com.eyeem.chips.Regex;
import com.eyeem.chips.Utils;
import com.eyeem.notes.R;
import com.eyeem.notes.screen.Preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import lombok.Getter;
import mortar.dagger2support.DaggerService;

import static mortar.MortarScope.getScope;

/**
 * Created by vishna on 18/02/15.
 */
public class PreviewView extends RelativeLayout {
   public static final int MIN_FONT_SIZE = 10;
   public static final int MAX_FONT_SIZE = 24;

   @Inject Preview.Presenter presenter;

   @InjectView(R.id.chipsTextView) @Getter ChipsTextView tv;
   @InjectView(R.id.seek_bar) SeekBar textSizeSeekBar;
   @InjectView(R.id.spacing_seek_bar) SeekBar spacingSizeSeekBar;
   @InjectView(R.id.text_size) TextView fontSize;
   @InjectView(R.id.spacing_size) TextView spacingSize;
   @InjectView(R.id.debug_check) CheckBox debugCheck;

   public PreviewView(Context context) {
      super(context);
      init();
   }

   public PreviewView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public PreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP) public PreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
      init();
   }

   private void init() {
      getScope(getContext()).<Preview.Component>getService(DaggerService.SERVICE_NAME).inject(this);
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

      // chips debug
      debugCheck = (CheckBox)findViewById(R.id.debug_check);
      debugCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            tv.setDebug(isChecked);
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
            Toast.makeText(view.getContext(), ((Linkify.Entity) bubbleSpan.data()).id, Toast.LENGTH_LONG).show();
         }
      });
      tv.requestFocus();
      updateTextProperties();
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
      fontSize.setText(String.format("font size: %ddp %.2fpx", calculatedProgress, _dp));
      spacingSize.setText(String.format("line spacing: %.2f", lineSpacing));
      tv.setLineSpacing(lineSpacing);
      presenter.populateNote();
   }

   SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         updateTextProperties();
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
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

      public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
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
