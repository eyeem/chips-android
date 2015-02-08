package com.eyeem.chipsedittextdemo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.adapter.NotesAdapter;
import com.eyeem.chipsedittextdemo.model.Note;
import com.eyeem.chipsedittextdemo.screen.Notes;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.Mortar;

/**
 * Created by vishna on 03/02/15.
 */
public class NotesView extends FrameLayout {

   @InjectView(R.id.recycler_view) RecyclerView rv;

   @Inject Notes.Presenter presenter;
   @Inject NotesAdapter adapter;

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
      Mortar.getScope(getContext()).<Notes.Component>getObjectGraph().inject(this);
      setSaveEnabled(true);
   }

   @Override protected void onFinishInflate() {
      super.onFinishInflate();
      ButterKnife.inject(this, this);
      llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
      rv.setLayoutManager(llm);
      rv.setAdapter(adapter);
      rv.setOnScrollListener(adapter.getCacheOnScroll());
   }

   @Override protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      presenter.takeView(this);
   }

   @Override protected void onDetachedFromWindow() {
      super.onDetachedFromWindow();
      presenter.dropView(this);
   }

   public void setNotes(List<Note> notes) {
      adapter.setNotes(notes);
   }
}
