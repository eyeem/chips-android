package com.eyeem.chipsedittextdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.screen.Edit;
import com.eyeem.chipsedittextdemo.screen.Note;
import com.eyeem.chipsedittextdemo.screen.Notes;
import com.eyeem.chipsedittextdemo.widget.ScreenPagerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.dagger2support.DaggerService;

import static mortar.MortarScope.getScope;

/**
 * Created by vishna on 16/02/15.
 */
public class NoteView extends LinearLayout {

   @InjectView(R.id.screens_pager) ViewPager pager;
   ScreenPagerAdapter pagerAdapter;

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
      getScope(getContext()).<Note.Component>getService(DaggerService.SERVICE_NAME).inject(this);
   }

   @Override protected void onFinishInflate() {
      super.onFinishInflate();
      ButterKnife.inject(this, this);

      pagerAdapter = new ScreenPagerAdapter(getContext());
      pagerAdapter.addScreen(new Edit() /*TODO , new Preview()*/);
      pager.setAdapter(pagerAdapter);
   }
}
