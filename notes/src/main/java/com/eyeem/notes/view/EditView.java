package com.eyeem.notes.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eyeem.chips.AutocompletePopover;
import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.ChipsEditText;
import com.eyeem.notes.R;
import com.eyeem.notes.model.Note;
import com.eyeem.notes.mortarflow.Ass;
import com.eyeem.notes.screen.Edit;

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
public class EditView extends RelativeLayout {

   @Inject Edit.Presenter presenter;
   @Inject List<String> suggestions;
   @Inject Note note;

   @InjectView(R.id.chipsMultiAutoCompleteTextview1) @Getter ChipsEditText et;
   @InjectView(R.id.popover) AutocompletePopover popover;
   @InjectView(R.id.edit) Button edit;

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
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
   }

   public void setup() {
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

      BubbleStyle bubbleStyle = Note.defaultBubbleStyle(getContext(), et.getTextSize());
      et.setText(note.textSpan(bubbleStyle, et));
      et.setCurrentBubbleStyle(bubbleStyle);
   }

   @OnClick(R.id.edit) public void toggleEdit(View view) {
      et.resetAutocompleList();
      et.startManualMode();
      popover.show();
      et.postDelayed(() -> et.showKeyboard(), 100);
   }

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


}
