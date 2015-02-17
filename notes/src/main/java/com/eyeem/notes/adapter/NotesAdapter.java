package com.eyeem.notes.adapter;

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
import com.eyeem.notes.R;
import com.eyeem.notes.experimental.CacheOnScroll;
import com.eyeem.notes.model.Note;
import com.eyeem.storage.Storage;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by vishna on 03/02/15.
 */
public class NotesAdapter extends RecyclerAdapter<Note, NotesAdapter.NoteHolder> {

   private final static boolean TRUNCATE = true;

   TextPaint textPaint;
   LayoutBuild.Config layoutConfig;
   int width = 0;
   CacheOnScroll cacheOnScroll;

   public NotesAdapter(Storage<Note>.List list, CacheOnScroll cacheOnScroll) {
      super(list);
      this.cacheOnScroll = cacheOnScroll;
      this.cacheOnScroll.setAheadLoader(new LayoutAheadLoader());
   }

   Context appContext;

   @Override public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      NoteHolder noteHolder =  new NoteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_row, parent, false));

      if (textPaint == null) {
         textPaint = noteTextPaint(parent.getContext());
      }

      if (width <= 0) {
         width = parent.getWidth() - noteHolder.textView.getPaddingLeft() - noteHolder.textView.getPaddingRight();
      }

      if (appContext == null) {
         appContext = parent.getContext().getApplicationContext();
      }

      return noteHolder;
   }

   @Override public void onBindViewHolder(NoteHolder holder, final int position) {

      Note note = getItem(position);

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
         holder.textView.setLayoutBuild(cacheOnScroll.get(note.id));
      }

      if (TRUNCATE)  holder.textView.setTruncated(true);
   }

   static class NoteHolder extends RecyclerView.ViewHolder {

      @InjectView(R.id.note_text_view) ChipsTextView textView;

      public NoteHolder(View itemView) {
         super(itemView);
         ButterKnife.inject(this, itemView);
      }
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

   public class LayoutAheadLoader implements CacheOnScroll.AheadLoader<LayoutBuild> {
      @Override public LayoutBuild buildFor(String id) {
         Note found = null;

         // TODO some smart lambda
         for (Note note : items) { // find note
            if (note.id.equals(id)) {
               found = note;
               break;
            }
         }

         if (found == null) return null;

         Spannable spannable = new SpannableString(found.textSpan((int) textPaint.getTextSize(), appContext));

         LayoutBuild layoutBuild = new LayoutBuild(spannable, layoutConfig);
         layoutBuild.build(width);

         return layoutBuild;
      }

      @Override public List<String> idsAround(int centerIndex, int radius) {

         ArrayList<String> result = new ArrayList<>();

         if (centerIndex < 0 || centerIndex >= items.size()) return result;

         for (int r = 1; r < radius; r++) {

            int left = centerIndex - r;
            int right = centerIndex + r;

            if (left > 0) {
               result.add(items.get(left).id);
            }

            if (right < items.size()) {
               result.add(items.get(right).id);
            }
         }

         return result;
      }
   }
}
