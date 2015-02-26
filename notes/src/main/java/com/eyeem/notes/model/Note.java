package com.eyeem.notes.model;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.eyeem.chips.BubbleSpan;
import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.ChipsEditText;
import com.eyeem.chips.Linkify;
import com.eyeem.chips.Utils;
import com.eyeem.notes.R;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by vishna on 03/02/15.
 */
public class Note {

   @SerializedName("id")
   public String id;

   @SerializedName("text")
   public String text;

   @SerializedName("entities")
   public Linkify.Entities entities;

   public Note() {}

   public Note(String id, String text, Linkify.Entities entities) {
      this.id = id;
      this.text = text;
      this.entities = entities;
   }

   public SpannableStringBuilder textSpan(BubbleStyle style, ChipsEditText et) {
      boolean isEmpty = TextUtils.isEmpty(text);
      SpannableStringBuilder ssb = new SpannableStringBuilder(
         isEmpty ? "" : text
      );

      int width = 0; // FIXME not so great
      if (entities == null)
         return ssb;
      for (Linkify.Entity entity : entities) {
         switch (entity.type) {
            case Linkify.Entity.ALBUM:
            case Linkify.Entity.VENUE:
            case Linkify.Entity.CITY:
            case Linkify.Entity.COUNTRY:
            case Linkify.Entity.PERSON:
            case Linkify.Entity.EMAIL:
            case Linkify.Entity.MENTION:
            case Linkify.Entity.URL:
            case Linkify.Entity.UNKNOWN:
               com.eyeem.chips.Utils.bubblify(ssb, entity.text, entity.start, entity.end,
                  width, style, et, entity);
               break;
            default:
               // NOOP
         }
      }
      return ssb;
   }

   public static Note from(String id, SpannableString ss) {
      Note out = new Note();
      out.id = id;
      out.entities = new Linkify.Entities();

      HashMap<Class<?>, Utils.FlatteningFactory> factories = new HashMap<>();
      factories.put(BubbleSpan.class, new Note.Flatten(out.entities));
      out.text = Utils.flatten(ss, factories);

      return out;
   }

   public static BubbleStyle defaultBubbleStyle(Context context, float textSize) {
      BubbleStyle bubbleStyle = BubbleStyle.build(context, R.style.note_default_style);
      bubbleStyle.setTextSize((int) textSize);
      return bubbleStyle;
   }

   private static class Flatten implements Utils.FlatteningFactory {

      Linkify.Entities entities;

      Flatten(Linkify.Entities entities) {
         this.entities = entities;
      }

      @Override public String out(String in, Object span) {
         return in == null ? null : in.trim();
      }

      @Override public void afterReplaced(int start, int end, String replacementText, Object span) {
         Linkify.Entity entity = new Linkify.Entity();

         if (!(span instanceof BubbleSpan)) return;

         BubbleSpan bubbleSpan = (BubbleSpan) span;

         entity.start = start;
         entity.end = end;
         if (bubbleSpan.data() instanceof Linkify.Entity) {
            Linkify.Entity oldEntity = (Linkify.Entity) bubbleSpan.data();
            entity.text = oldEntity.text;
            entity.id = oldEntity.id;
            entity.type = oldEntity.type;
         } else {
            entity.text = replacementText;
            entity.id = String.valueOf(System.currentTimeMillis());
            entity.type = Linkify.Entity.UNKNOWN;
         }

         entities.add(entity);
      }
   }
}
