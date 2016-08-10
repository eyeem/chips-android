package com.eyeem.notes.widget;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vishna on 10/08/16.
 */
public class AutocompleteHelper {

   String latestQuery;

   Handler ui;
   Handler bg;
   AutocompletePopover.Resolver resolver;
   AutocompletePopover.Adapter adapter;

   AutocompleteHelper(AutocompletePopover.Resolver resolver, AutocompletePopover.Adapter adapter) {
      this.resolver = resolver;
      this.adapter = adapter;

      ui = new Handler();
      HandlerThread ht = new HandlerThread("AutocompleteHelper.Thread", Thread.MIN_PRIORITY);
      ht.start();
      bg = new Handler(ht.getLooper());
   }

   public void search(String query) {
      this.latestQuery = query;
      ui.removeCallbacks(searchRunnable);
      ui.postDelayed(searchRunnable, 300);
   }

   public Runnable searchRunnable = new Runnable() {
      @Override public void run() {
         final String query = latestQuery;
         if (queriesSoFar.containsKey(query)) {
            adapter.setItems(queriesSoFar.get(query));
         } else {
            bg.removeCallbacksAndMessages(null);
            bg.post(new QueryRunnable(AutocompleteHelper.this, query));
         }
      }
   };

   public static class QueryRunnable implements Runnable {
      WeakReference<AutocompleteHelper> _autocompleteHelper;
      String query;

      public QueryRunnable(AutocompleteHelper autocompleteHelper, String query) {
         this._autocompleteHelper = new WeakReference<>(autocompleteHelper);
         this.query = query;
      }

      @Override public void run() {
         AutocompleteHelper h = _autocompleteHelper.get();
         if (h == null) {
            return;
         }
         try {
            final ArrayList<String> results = TextUtils.isEmpty(query) ? h.resolver.getDefaultSuggestions() : h.resolver.getSuggestions(query);
            h.queriesSoFar.put(query, results);
            final AutocompletePopover.Adapter adapter = h.adapter;
            h.ui.post(new Runnable() {
               @Override public void run() {
                  adapter.setItems(results);
               }
            });
         } catch (Exception e) {}
      }
   }

   private HashMap<String, ArrayList<String>> queriesSoFar = new HashMap<>();
}
