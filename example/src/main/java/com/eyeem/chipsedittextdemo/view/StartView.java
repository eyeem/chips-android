package com.eyeem.chipsedittextdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.screen.Edit;
import com.eyeem.chipsedittextdemo.screen.Notes;
import com.eyeem.chipsedittextdemo.screen.Start;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import flow.Flow;
import mortar.Mortar;

/**
 * Created by vishna on 02/02/15.
 */
public class StartView extends LinearLayout {

   @Inject Start.Presenter presenter;

   public StartView(Context context) {
      super(context);
      init();
   }

   public StartView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public StartView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP) public StartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
      init();
   }

   private void init() {
      Mortar.getScope(getContext()).<Start.Component>getObjectGraph().inject(this);
   }

   @Override protected void onFinishInflate() {
      super.onFinishInflate();
      ButterKnife.inject(this, this);
   }

   @Override protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      presenter.takeView(this);
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
   }

   @OnClick(R.id.editor) void onEditorClick(View view) {
      Flow.get(this).goTo(new Edit());
   }

   @OnClick(R.id.notes) void onNotesClick(View view) {
      Flow.get(this).goTo(new Notes());
   }
}
