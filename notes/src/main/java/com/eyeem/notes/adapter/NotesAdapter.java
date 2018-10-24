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

import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.ChipsTextView;
import com.eyeem.chips.LayoutBuild;
import com.eyeem.notes.R;
import com.eyeem.notes.event.NoteClickedEvent;
import com.eyeem.notes.experimental.CacheOnScroll;
import com.eyeem.notes.model.Note;
import com.eyeem.storage.Storage;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by vishna on 03/02/15.
 */
public class NotesAdapter extends RecyclerAdapter<Note, NoteHolder> {

   BubbleStyle bubbleStyle;
   LayoutBuild.Config layoutConfig;
   int width = 0;
   CacheOnScroll cacheOnScroll;
   Bus bus;

   public NotesAdapter(Storage<Note>.List list, CacheOnScroll cacheOnScroll, Bus bus) {
      super(list);
      this.cacheOnScroll = cacheOnScroll;
      this.bus = bus;
   }

   Context appContext;

   @Override public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      NoteHolder noteHolder =  new NoteHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.note_row, parent, false), bus);
      if(layoutConfig == null){
         // hacky. Ahead loader calculates with the config of the 1st inflated view
         // TODO: maybe a better way to handle that?
         layoutConfig = noteHolder.textView.defaultConfig();
         this.cacheOnScroll.setAheadLoader(new LayoutAheadLoader());
      }

      if (width <= 0) {
         width = parent.getWidth() - noteHolder.textView.getPaddingLeft() - noteHolder.textView.getPaddingRight();
      }

      if (appContext == null) {
         appContext = parent.getContext().getApplicationContext();
      }

      if (bubbleStyle == null) {
         bubbleStyle = Note.defaultBubbleStyle(appContext, layoutConfig.textPaint.getTextSize());
      }

      return noteHolder;
   }

   @Override public void onBindViewHolder(NoteHolder holder, final int position) {

      Note note = getItem(position);
      // holder.setNote(note).textView.setLayoutBuild(cacheOnScroll.get(note.id));
   }

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

         Spannable spannable = new SpannableString(found.textSpan(bubbleStyle, null));

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
