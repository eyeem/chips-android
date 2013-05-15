package com.eyeem.chips;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AutocompleteManager {
   Resolver resolver;

   public void setResolver(Resolver resolver) {
      this.resolver = resolver;
   }

   public interface Resolver {
      public ArrayList<String> getSuggestions(String query) throws Exception;
      public void update(String query, ArrayList<String> results);
   }

   public void search(String query) {
      if (query != null) {
         if (query.trim().length() < 3)
            new SearchTask("").start(); // autocomplete with defaults
         else
            new SearchTask(query).start();

      }
   }

   private AtomicInteger searchVenueTaskCount = new AtomicInteger(0);
   private HashMap<String, ArrayList<String>> queriesSoFar = new HashMap<String, ArrayList<String>>();

   private class SearchTask extends AsyncTask<Void, Void, ArrayList<String>> {

      private String query = null;

      private SearchTask(String query) {
         this.query = query;
      }

      @Override
      protected void onPreExecute() {
         super.onPreExecute();
         searchVenueTaskCount.incrementAndGet();
      }

      @Override
      protected ArrayList<String> doInBackground(Void... params) {
         try {
            ArrayList<String> results = resolver.getSuggestions(query);
            if (results != null && !TextUtils.isEmpty(query))
               queriesSoFar.put(query, results);
            return results;
         } catch (Exception e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onCancelled() {
         super.onCancelled();
         decrement();
      }

      @Override
      protected void onPostExecute(ArrayList<String> results) {
         super.onPostExecute(results);
         if (resolver != null && results != null) {
            resolver.update(query, results);
         }
         decrement();
      }

      private void decrement() {
         searchVenueTaskCount.decrementAndGet();
      }

      public void start() {
         boolean alreadyQueriedSomethingSimilar = false;
         for (java.util.Map.Entry<String, ArrayList<String>> e : queriesSoFar.entrySet()) {
            if (query.startsWith(e.getKey()) && e.getValue().size() == 0) {
               // further queries make no sense
               alreadyQueriedSomethingSimilar = true;
               break;
            } else if (query.equals(e.getKey()) && e.getValue().size() > 0) {
               // we already made this query
               alreadyQueriedSomethingSimilar = true;
               if (resolver != null) {
                  resolver.update(query, e.getValue());
               }
               break;
            }
         }
         if (!alreadyQueriedSomethingSimilar) {
            this.execute((Void)null);
         }
      }
   }
}
