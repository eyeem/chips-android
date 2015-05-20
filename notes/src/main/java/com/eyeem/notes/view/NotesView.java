package com.eyeem.notes.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.eyeem.notes.R;
import com.eyeem.notes.adapter.NotesAdapter;
import com.eyeem.notes.event.NewNoteEvent;
import com.eyeem.notes.screen.Notes;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static mortar.MortarScope.getScope;
import static com.eyeem.notes.mortarflow.Utils.DAGGER_SERVICE;

/**
 * Created by vishna on 03/02/15.
 */
public class NotesView extends FrameLayout {

   @InjectView(R.id.fab_button) ImageButton fabButton;
   @InjectView(R.id.recycler_view) RecyclerView rv;

   @Inject Notes.Presenter presenter;
   @Inject NotesAdapter adapter;
   @Inject Bus bus;

   LinearLayoutManager llm;

   public NotesView(Context context) {
      super(context);
      init();
   }

   public NotesView(Context context, AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public NotesView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
   }

   @TargetApi(Build.VERSION_CODES.LOLLIPOP) public NotesView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
      init();
   }

   private void init() {
      getScope(getContext()).<Notes.Component>getService(DAGGER_SERVICE).inject(this);
      setSaveEnabled(true);
   }

   @Override protected void onFinishInflate() {
      super.onFinishInflate();
      ButterKnife.inject(this, this);
      llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
      rv.setLayoutManager(llm);
      rv.setAdapter(adapter);
      rv.setOnScrollListener(adapter.getCacheOnScroll());

      fabButton.setImageDrawable(new IconicsDrawable(getContext(), FontAwesome.Icon.faw_pencil).color(Color.WHITE).actionBarSize());
   }

   @Override protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      presenter.takeView(this);
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
   }

   @OnClick(R.id.fab_button) void onFabButton(View view) {
      bus.post(new NewNoteEvent());
   }
}
