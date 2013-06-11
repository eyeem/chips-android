package com.eyeem.chips;

import android.os.Handler;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AutocompleteManager {
   Resolver resolver;
   String latestQuery;
   Handler handler;
   boolean tryToBeSmart;

   AutocompleteManager() {
      handler = new Handler();
      tryToBeSmart = true;
   }

   public void setResolver(Resolver resolver) {
      this.resolver = resolver;
   }

   public interface Resolver {
      public ArrayList<String> getSuggestions(String query) throws Exception;
      public ArrayList<String> getDefaultSuggestions();
      public void update(String query, ArrayList<String> results);
   }

   public void search(String query) {
      latestQuery = query;
      if (query != null) {
         if (query.trim().length() < 3)
            new SearchTask("").start(); // autocomplete with defaults
         else
            new SearchTask(query).start();

      }
   }

   private AtomicInteger searchVenueTaskCount = new AtomicInteger(0);
   private HashMap<String, ArrayList<String>> queriesSoFar = new HashMap<String, ArrayList<String>>();

   private class SearchTask {

      private String query = null;

      private SearchTask(String query) {
         if (query != null)
            this.query = query.trim();
      }

      protected void onPreExecute() {
         searchVenueTaskCount.incrementAndGet();
      }

      protected ArrayList<String> doInBackground() {
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

      protected void onCancelled() {
         decrement();
      }

      protected void onPostExecute(ArrayList<String> results) {
         if (resolver != null && results != null && (latestQuery.startsWith(query) || latestQuery.equals(query))) {
            resolver.update(query, results);
         }
         decrement();
      }

      private void execute() {
         onPreExecute();
         Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  final ArrayList<String> results = doInBackground();
                  handler.post(new Runnable() {
                     @Override
                     public void run() {
                        onPostExecute(results);
                     }
                  });
               } catch (Throwable t) {
                  onCancelled();
               }
            }
         });
         t.setPriority(Thread.MIN_PRIORITY);
         t.start();
      }

      private void decrement() {
         searchVenueTaskCount.decrementAndGet();
      }

      public void start() {
         if (TextUtils.isEmpty(query) && resolver != null) {
            resolver.update("", resolver.getDefaultSuggestions());
            return;
         }
         boolean alreadyQueriedSomethingSimilar = false;
         for (java.util.Map.Entry<String, ArrayList<String>> e : queriesSoFar.entrySet()) {
            if (tryToBeSmart && query.startsWith(e.getKey()) && e.getValue().size() == 0) {
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
            this.execute();
         }
      }
   }
}
