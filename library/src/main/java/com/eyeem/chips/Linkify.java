package com.eyeem.chips;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Standarizes url link handling... sort of.
 * User: vishna
 * Date: 3/11/13
 * Time: 5:37 PM
 */
public class Linkify {
   public static final Pattern USER_REGEX = Pattern.compile("@([A-Za-z0-9_]+)");
   public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}", Pattern.CASE_INSENSITIVE);

   public static Entities computeEntities(String ss){

      Entities entities = new Entities();
      Matcher matcher = USER_REGEX.matcher(ss);
      while (matcher.find()) {
         final String user = matcher.group(1);
         int i = matcher.start()-1;
         if (i == -1 || (i > 0 && Character.isWhitespace(ss.charAt(i)))) {
            entities.add(new Entity(matcher.start(), matcher.end(), user, matcher.group(), Entity.MENTION));
         }
      }

      matcher = Regex.VALID_URL.matcher(ss);
      while (matcher.find()) {
         final String _url = matcher.group();
         int i = matcher.start() - 1 + _url.indexOf(_url.trim());
         if (i == -1 || (i > 0 && Character.isWhitespace(ss.charAt(i)))) {
            String url = _url.trim();
            if (url != null && !url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
               url = "http://" + url;
            }
            entities.add(new Entity(matcher.start(), matcher.end(), url, matcher.group(), Entity.URL));
         }
      }

      matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(ss);
      while (matcher.find()) {
         final String email = matcher.group();
         int i = matcher.start()-1;
         if (i == -1 || (i > 0 && Character.isWhitespace(ss.charAt(i)))) {
            entities.add(new Entity(matcher.start(), matcher.end(), email, matcher.group(), Entity.EMAIL));
         }
      }

      return entities;
   }

   public static class Entities extends ArrayList<Entity> {
      @Override
      public boolean add(Entity a) {
         // TODO this should be some smart binary interpolation alogrithm
         for (Entity b : this) {
            // check if entities intersect
            if (a.start >= b.start && a.start < b.end)
               return false;
            if (a.end > b.start && a.end <= b.end)
               return false;
         }
         return super.add(a);
      }

      @Override
      public boolean addAll(Collection<? extends Entity> collection) {
         for (Entity e : collection)
            add(e);
         return true;
      }

      /**
       * This works on assumptions that mPhoto.entities are grouped and
       * ordered e.g. album|album|link|link|mention|person|person|city
       * They should be cause this is what Linkify.computeEntites does
       * @param type of album that is required
       * @return Ordered array of album entites of one type
       */
      public ArrayList<Linkify.Entity> subEntities (int type) {
         ArrayList<Linkify.Entity> albums = new ArrayList<Linkify.Entity>();
         for (Linkify.Entity entity : this) {
            if (entity.type == type)
               albums.add(entity);
         }
         return albums;
      }
   }

   public static class Entity implements Serializable {
      public static final int EMAIL = 0;
      public static final int PERSON = 1;
      public static final int URL = 2;
      public static final int ALBUM = 3;
      public static final int VENUE = 4;
      public static final int CITY = 5;
      public static final int COUNTRY = 6;
      public static final int MENTION = 7;
      public static final int UNKNOWN = 8;

      public int start;
      public int end;
      public String id;
      public String text;
      public int type;
      public Serializable data;

      public Entity() {};
      public Entity(int start, int end, String id, String text, int type) {
         this.start = start;
         this.end = end;
         this.id = id;
         this.text = text;
         this.type = type;
      }
   }
}

