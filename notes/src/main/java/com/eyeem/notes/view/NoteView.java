package com.eyeem.notes.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.eyeem.notes.R;
import com.eyeem.notes.mortarflow.HandlesBack;
import com.eyeem.notes.screen.Edit;
import com.eyeem.notes.screen.Note;
import com.eyeem.notes.screen.Preview;
import com.eyeem.notes.widget.ScreenPagerAdapter;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import mortar.MortarScope;

import static mortar.MortarScope.getScope;
import static com.eyeem.notes.mortarflow.Utils.DAGGER_SERVICE;

/**
 * Created by vishna on 16/02/15.
 */
public class NoteView extends LinearLayout implements ViewPager.OnPageChangeListener, HandlesBack {

   @InjectView(R.id.screens_pager) @Getter ViewPager pager;
   ScreenPagerAdapter pagerAdapter;

   @Inject Note.Presenter presenter;

   public NoteView(Context context) {
      super(context);
      init();
   }

   public NoteView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public NoteView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP) public NoteView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
      init();
   }

   private void init() {
      getScope(getContext()).<Note.Component>getService(DAGGER_SERVICE).inject(this);
   }

   @Override protected void onFinishInflate() {
      super.onFinishInflate();
      ButterKnife.inject(this, this);

      pagerAdapter = new ScreenPagerAdapter(getContext());
      pagerAdapter.addScreen(new Edit(), new Preview());
      pager.setAdapter(pagerAdapter);
      pager.setOnPageChangeListener(this);
   }

   @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      // no-op
   }

   @Override public void onPageSelected(int position) {
      Log.d(NoteView.class.getSimpleName(), "onPageSelected = " + position);
      presenter.onPageSelected(position);
   }

   @Override public void onPageScrollStateChanged(int state) {
      String description = "";

      switch (state) {
         case ViewPager.SCROLL_STATE_IDLE:
            description = "idle";
            break;
         case ViewPager.SCROLL_STATE_DRAGGING:
            description = "dragging";
            break;
         case ViewPager.SCROLL_STATE_SETTLING:
            description = "settling";
            break;
      }

      Log.d(NoteView.class.getSimpleName(), description);
   }

   @Override protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      presenter.takeView(this);
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
   }

   @Override public boolean onBackPressed() {
      int currentPosition = pager.getCurrentItem();

      View view = pagerAdapter.getViewForPosition(currentPosition);
      if (view instanceof HandlesBack) {
         return ((HandlesBack)view).onBackPressed();
      }

      return false;
   }
}
