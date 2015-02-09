package com.eyeem.chipsedittextdemo.experimental;

import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vishna on 06/02/15.
 */
public class CacheOnScroll<Type> extends RecyclerView.OnScrollListener  {

   LruCache<String, Type> cache;

   /**
    * number of items that are not yet visible but should be
    * prepared by this class for a likely display
    */
   int aheadCount;

   PausableThreadPoolExecutor executor;
   Scheduler scheduler;
   AheadLoader<Type> aheadLoader;

   public CacheOnScroll(PausableThreadPoolExecutor executor, int aheadCount) {
      this.executor = executor;
      this.aheadCount = aheadCount;
      this.cache = new LruCache<>(aheadCount);
   }

   public CacheOnScroll<Type> setAheadLoader(AheadLoader<Type> aheadLoader) {
      this.aheadLoader = aheadLoader;
      return this;
   }

   private int previousScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
   private boolean scrollingFirstTime = true;

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
      return scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING;
   }

   // TODO extend schedulerForId... meaning based if it's in cache or not you get ui scheduler or other scheduler
   private Scheduler scheduler() {
      if (scheduler == null) {
         scheduler = Schedulers.from(executor);
      }
      return scheduler;
   }

   public interface AheadLoader<Type> {
      /**
       * Builds a Type instance for a given id
       * @param id
       * @return
       */
      Type buildFor(String id);

      /**
       * Provides ids of surrounding items
       * @param id
       * @param radius
       * @return
       */
      List<String> idsAround(String id, int radius);
   }

   public Observable<Type> get(final String id) {
      final Type cachedType = cache.get(id);

      // TODO weakreference this clojure
      Observable<Type> typeObservable = Observable.create(new Observable.OnSubscribe<Type>() {
         @Override
         public void call(Subscriber<? super Type> subscriber) {

            if (!subscriber.isUnsubscribed() && cachedType != null) {
               subscriber.onNext(cachedType);
               return;
            }

            if (subscriber.isUnsubscribed()) return;

            Type uncachedType = aheadLoader != null ? aheadLoader.buildFor(id) : null;

            if (uncachedType != null) cache.put(id, uncachedType);

            if (!subscriber.isUnsubscribed()) {
               subscriber.onNext(uncachedType);
            }
         }
      })
      .subscribeOn(cachedType == null ? scheduler() : AndroidSchedulers.mainThread())
      .observeOn(AndroidSchedulers.mainThread());

      return typeObservable;
   }
}
