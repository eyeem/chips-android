package com.eyeem.notes.utils;

import android.util.Log;

import com.eyeem.chips.Linkify;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by vishna on 17/02/15.
 */
public class LinkifyDeserialiser implements JsonDeserializer<Linkify.Entities> {
   @Override public Linkify.Entities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

      Linkify.Entities entities = new Linkify.Entities();

      JsonArray array = (JsonArray) json;

      for (JsonElement element : array) {
         try {
            JsonObject o = (JsonObject) element;

            Linkify.Entity entity = new Linkify.Entity();
            entity.start = o.get("start").getAsInt();
            entity.end = o.get("end").getAsInt();
            entity.id = o.get("data").getAsString();
            entity.text = o.get("displayText").getAsString();
            entity.type = sMap.get(o.get("type").getAsString());
            entities.add(entity);
         } catch (Exception e) {
            Log.w(LinkifyDeserialiser.class.getSimpleName(), "deserialize", e);
            continue;
         }
      }
      return entities;
   }

   private final static HashMap<String, Integer> sMap;

   static {
      sMap = new HashMap<>();
      sMap.put("url", Linkify.Entity.URL);
   }
}
