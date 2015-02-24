package com.eyeem.notes.experimental;

import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
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
         checkCacheDisplacement(middleVisibleItemPosition);
      }

      if (!isScrolling(newState) && isScrolling(previousScrollState)) {
         executor.resume();
         checkCacheDisplacement(middleVisibleItemPosition);
      }

      if (isScrolling(newState) && !isScrolling(previousScrollState)) {
         executor.pause();
         cancelCacheCheck();
      }

      previousScrollState = newState;
   }

   protected boolean isScrolling(int scrollState) {
      return scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING;
   }

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
       * @param position
       * @param radius
       * @return
       */
      List<String> idsAround(int position, int radius);
   }

   public Observable<Type> get(final String id) {
      final Type cachedType = cache.get(id);

      // TODO weakreference this clojure
      Observable<Type> typeObservable = Observable.create((Subscriber<? super Type> subscriber) -> {

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
      })
      .subscribeOn(cachedType == null ? scheduler() : AndroidSchedulers.mainThread());

      return typeObservable;
   }

   private Subscription checkCacheDisplacementSubscription;

   private void cancelCacheCheck() {
      if (checkCacheDisplacementSubscription != null) {
         checkCacheDisplacementSubscription.unsubscribe();
         checkCacheDisplacementSubscription = null;
      }
   }

   private void checkCacheDisplacement(int middlePosition) {
      if (aheadLoader == null) return;
      cancelCacheCheck();

      List<String> ids = aheadLoader.idsAround(middlePosition, aheadCount/2 - 1);

      // check number of items already available in cache
      List<Observable<Type>> observables = new ArrayList<>();
      for (final String id : ids) {
         if (cache.get(id) == null) {
            observables.add(get(id).subscribeOn(scheduler()).observeOn(scheduler()));
         }
      }

      checkCacheDisplacementSubscription = Observable.merge(observables).subscribeOn(scheduler()).observeOn(scheduler()).subscribe();
   }
}
