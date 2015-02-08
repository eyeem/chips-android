package com.eyeem.chipsedittextdemo.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eyeem.chips.ChipsTextView;
import com.eyeem.chips.LayoutBuild;
import com.eyeem.chipsedittextdemo.R;
import com.eyeem.chipsedittextdemo.experimental.CacheOnScroll;
import com.eyeem.chipsedittextdemo.model.Note;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vishna on 03/02/15.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

   private final static boolean TRUNCATE = true;

   List<Note> notes;
   TextPaint textPaint;
   LayoutBuild.Config layoutConfig;
   int width = 0;
   CacheOnScroll cacheOnScroll;

   public NotesAdapter(CacheOnScroll cacheOnScroll) {
      this.cacheOnScroll = cacheOnScroll;
   }

   @Override public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      NoteHolder noteHolder =  new NoteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_row, parent, false));

      if (textPaint == null) {
         textPaint = noteTextPaint(parent.getContext());
      }

      if (width <= 0) {
         width = parent.getWidth() - noteHolder.textView.getPaddingLeft() - noteHolder.textView.getPaddingRight();
      }

      return noteHolder;
   }

   @Override public void onBindViewHolder(NoteHolder holder, final int position) {
      final Context context = holder.textView.getContext();

      // lazy init config
      if (layoutConfig == null) {
         if (width > 0) {
            layoutConfig = new LayoutBuild.Config();
            layoutConfig.lineSpacing = 1.25f;
            layoutConfig.textPaint = textPaint;
            layoutConfig.truncated = true;
            if (TRUNCATE)  layoutConfig.maxLines = 2;
            SpannableStringBuilder moreText = new SpannableStringBuilder("...");
            com.eyeem.chips.Utils.tapify(moreText, 0, moreText.length(), 0xff000000, 0xff000000, new Truncation());
            layoutConfig.moreText = moreText;
         }
      }

      if (layoutConfig != null) {
         Observable textObservable = Observable.create(new Observable.OnSubscribe<LayoutBuild>() {
            @Override
            public void call(Subscriber<? super LayoutBuild> subscriber) {
               // FIXME possible context leak? no?
               Spannable spannable = new SpannableString(notes.get(position).textSpan((int) textPaint.getTextSize(), context));

               // TODO layout build
               LayoutBuild layoutBuild = new LayoutBuild(spannable, layoutConfig);
               layoutBuild.build(width);

               if (!subscriber.isUnsubscribed()) {
                  subscriber.onNext(layoutBuild);
               }
            }
         })
         .subscribeOn(cacheOnScroll.scheduler()) // TODO isInCache ? AndroidSchedulers.mainThread() : Schedulers.io();
         .observeOn(AndroidSchedulers.mainThread());
         holder.textView.setLayoutBuild(textObservable);
      }


      if (TRUNCATE)  holder.textView.setTruncated(true);
   }

   @Override public int getItemCount() {
      return notes == null ? 0 : notes.size();
   }

   static class NoteHolder extends RecyclerView.ViewHolder {

      @InjectView(R.id.note_text_view) ChipsTextView textView;

      public NoteHolder(View itemView) {
         super(itemView);
         ButterKnife.inject(this, itemView);
      }
   }

   public NotesAdapter setNotes(List<Note> notes) {
      this.notes = notes;
      notifyDataSetChanged();
      return this;
   }


   private static TextPaint _noteTextPaint;

   static TextPaint noteTextPaint(Context context){
      context = context.getApplicationContext();
      if (_noteTextPaint == null) {
         _noteTextPaint = new TextPaint();
         _noteTextPaint.setAntiAlias(true);
         Resources r = context.getResources();
         float _dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
         _noteTextPaint.setTextSize(_dp);
         _noteTextPaint.setColor(0xFF000000);
      }
      return _noteTextPaint;
   }

   private static class Truncation {
   } // marker class

   public CacheOnScroll getCacheOnScroll() {
      return cacheOnScroll;
   }
}
