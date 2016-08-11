package com.eyeem.notes.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.ChipsEditText;
import com.eyeem.notes.R;
import com.eyeem.notes.model.Note;
import com.eyeem.notes.mortarflow.Ass;
import com.eyeem.notes.mortarflow.HandlesBack;
import com.eyeem.notes.screen.Edit;
import com.eyeem.notes.utils.KeyboardDetector;
import com.eyeem.notes.widget.AutocompletePopover;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import lombok.Getter;

import static mortar.MortarScope.getScope;
import static com.eyeem.notes.mortarflow.Utils.DAGGER_SERVICE;

/**
 * Created by vishna on 27/01/15.
 */
public class EditView extends RelativeLayout implements HandlesBack {

   @Inject Edit.Presenter presenter;
   @Inject List<String> suggestions;
   @Inject Note note;

   @InjectView(R.id.chipsMultiAutoCompleteTextview1) @Getter ChipsEditText et;
   @InjectView(R.id.popover) AutocompletePopover popover;
   @InjectView(R.id.edit) Button edit;

   KeyboardDetector keyboardDetector;

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
      getScope(getContext()).<Edit.Component>getService(DAGGER_SERVICE).inject(this);
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
      keyboardDetector.attachToView(this, new KeyboardDetector.KeyboardListener() {
         @Override public void onKeyboardShow(int height) {

         }

         @Override public void onKeyboardHide(int height) {
            // popover.hide();
         }
      });
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
      keyboardDetector.detachFromView();
   }

   public void setup() {
      et.setMaxBubbleCount(25);
      et.setLineSpacing(1.0f, 1.25f);
      popover.setChipsEditText(et);

      final ArrayList<String> availableItems = new ArrayList<String>(suggestions);
      popover.setResolver(new AutocompletePopover.Resolver() {
         @Override
         public ArrayList<String> getSuggestions(String query) throws Exception {
            return new ArrayList<String>();
         }

         @Override
         public ArrayList<String> getDefaultSuggestions() {
            return availableItems;
         }
      });

      BubbleStyle bubbleStyle = Note.defaultBubbleStyle(getContext(), et.getTextSize());
      et.setText(note.textSpan(bubbleStyle, et));
      et.setCurrentBubbleStyle(bubbleStyle);

      popover.setOnVisibilityChanged(visible -> edit.setVisibility(visible ? GONE : VISIBLE));
      keyboardDetector = new KeyboardDetector();
   }

   @OnClick(R.id.edit) public void toggleEdit(View view) {
      et.resetAutocompleList();
      et.startManualMode();
      popover.show();
      et.postDelayed(() -> et.showKeyboard(), 100);
   }

///// boiler plate code that makes saving state work http://trickyandroid.com/saving-android-view-state-correctly/
   @Override
   public Parcelable onSaveInstanceState() {
      return Ass.onSave(this, super.onSaveInstanceState());
   }

   @Override
   public void onRestoreInstanceState(Parcelable state) {
      super.onRestoreInstanceState(Ass.onLoad(this, state));
   }

   @Override
   protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
      dispatchFreezeSelfOnly(container);
   }

   @Override
   protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
      dispatchThawSelfOnly(container);
   }

   @Override public boolean onBackPressed() {
      if (popover.isShown()) {
         popover.hide();
         return true;
      }
      return false;
   }
}
