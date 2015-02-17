package com.eyeem.chipsedittextdemo.model;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.Linkify;
import com.google.gson.annotations.SerializedName;

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

   public SpannableStringBuilder textSpan(int textSize, Context context) {
      boolean isEmpty = TextUtils.isEmpty(text);
      SpannableStringBuilder ssb = new SpannableStringBuilder(
         isEmpty ? "" : text
      );

      if (noteBubbleStyle == null) {
         int padding = Math.round((float)textSize * (0.05f));

         noteBubbleStyle = new BubbleStyle(
            context.getResources().getDrawable(com.eyeem.chips.R.drawable.greentext_background_active),
            context.getResources().getDrawable(com.eyeem.chips.R.drawable.greentext_background_pressed),
            textSize, 0xffebe0f5, 0xffebe0f5, padding);
      }

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
               com.eyeem.chips.Utils.bubblify(ssb, entity.text, entity.start, entity.end,
                  width, noteBubbleStyle, null, entity);
               break;
            default:
               // NOOP
         }
      }
      return ssb;
   }

   static BubbleStyle noteBubbleStyle;
}
