package com.eyeem.chipsedittextdemo.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by vishna on 17/02/15.
 */
public class Assets {

   public static String from(Context context, String filename) {

      BufferedReader in = null;

      try {
         StringBuilder buf = new StringBuilder();
         InputStream json = context.getAssets().open(filename);
         in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
         String str;

         while ((str = in.readLine()) != null) {
            buf.append(str);
         }

         in.close();
         in = null;
         return buf.toString();
      } catch (Exception e) {
         return null;
      } finally {
         if (in != null) try {
            in.close();
         } catch (IOException e) {}
      }
   }

}
