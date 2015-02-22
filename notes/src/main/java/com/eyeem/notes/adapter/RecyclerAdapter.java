package com.eyeem.notes.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.eyeem.storage.Storage;

/**
 * Created by vishna on 15/11/13.
 */
public abstract class RecyclerAdapter<T, VH extends android.support.v7.widget.RecyclerView.ViewHolder>
   extends RecyclerView.Adapter<VH> implements Storage.Subscription {

   protected Storage<T>.List items;

   public RecyclerAdapter(Storage<T>.List items) {
      this.items = items;
   }

   public void replace(Storage<T>.List items) {
      if (this.items != null) {
         this.items.unsubscribe(this);
      }
      this.items = items;
      if (this.items != null) {
         this.items.subscribe(this);
      }
      notifyDataSetChanged();
   }

   @Override
   public int getItemCount() {
      return items == null ? 0 : items.size();
   }

   public T getItem(int position) {
      return items.get(position);
   }

   @Override
   public long getItemId(int position) {
      return Math.abs(items.idForPosition(position).hashCode());
   }

   @Override public void onUpdate(Action action) {
      notifyDataSetChanged();
      if (emptyView != null) {
         emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
      }
   }

   @Override
   public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
      super.registerAdapterDataObserver(observer);
      if (items != null) items.subscribe(this);
   }

   @Override
   public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
      super.unregisterAdapterDataObserver(observer);
      if (items != null) items.unsubscribe(this);
   }

   ///// seems empty view support was dropped thus putting this in
   private View emptyView;

   public void setEmptyView(View view) {
      this.emptyView = view;
   }
}