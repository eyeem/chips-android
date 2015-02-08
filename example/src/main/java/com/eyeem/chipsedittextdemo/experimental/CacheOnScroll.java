package com.eyeem.chipsedittextdemo.experimental;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by vishna on 06/02/15.
 */
public class CacheOnScroll extends RecyclerView.OnScrollListener  {

//   LruCache<String, Output> cache;

   /**
    * number of items that are not yet visible but should be
    * prepared by this class for a likely display
    */
   int aheadCount;

   PausableThreadPoolExecutor executor;
   Scheduler scheduler;

   public CacheOnScroll(PausableThreadPoolExecutor executor) {
      this.executor = executor;
   }

   private int previousScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
   private boolean scrollingFirstTime = true;
   private int lastBuildPosition;

   @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

      int visibleItemCount = recyclerView.getChildCount();
      int totalItemCount = recyclerView.getLayoutManager().getItemCount();
      if (visibleItemCount == 0) return;
      View firstChild = recyclerView.getChildAt(0);
      View lastChild = recyclerView.getChildAt(visibleItemCount - 1);
      int firstVisibleItemPosition = recyclerView.getChildPosition(firstChild);
      int lastVisibleItemPosition = recyclerView.getChildPosition(lastChild);

      int middleVisibleItemPosition = (firstVisibleItemPosition + lastVisibleItemPosition)/2;

      if (scrollingFirstTime) {
         executor.resume();
         scrollingFirstTime = false;
      }

      if (!isScrolling(newState) && isScrolling(previousScrollState)) {
         executor.resume();
      }

      if (isScrolling(newState) && !isScrolling(previousScrollState)) {
         executor.pause();
      }

      previousScrollState = newState;
   }

   protected boolean isScrolling(int scrollState) {
      return scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
   }

   // TODO extend schedulerForId... meaning based if it's in cache or not you get ui scheduler or other scheduler
   public Scheduler scheduler() {
      if (scheduler == null) {
         scheduler = Schedulers.from(executor);
      }
      return scheduler;
   }
}
