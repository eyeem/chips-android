package com.eyeem.notes.mortarflow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.eyeem.notes.R;

import flow.Flow;
import flow.path.Path;
import flow.path.PathContainer;
import flow.path.PathContainerView;

/**
 * Created by vishna on 29/01/15.
 */
public class FramePathContainerView extends FrameLayout implements HandlesBack, HandlesUp, PathContainerView {

   private final PathContainer container;
   private boolean disabled;

   public FramePathContainerView(Context context, AttributeSet attrs) {
      super(context, attrs);
      container = new SimplePathContainer(R.id.main_content_tag, Path.contextFactory(new MortarContextFactory()));
   }

   @Override public ViewGroup getContainerView() {
      return this;
   }

   @Override public void dispatch(Flow.Traversal traversal, final Flow.TraversalCallback callback) {
      disabled = true;
      container.executeTraversal(this, traversal, new Flow.TraversalCallback() {
         @Override public void onTraversalCompleted() {
            callback.onTraversalCompleted();
            disabled = false;
         }
      });
   }

   @Override public boolean onUpPressed() {
      return UpAndBack.onUpPressed(getCurrentChild());
   }

   @Override public boolean onBackPressed() {
      return UpAndBack.onBackPressed(getCurrentChild());
   }

   @Override public ViewGroup getCurrentChild() {
      return (ViewGroup) getContainerView().getChildAt(0);
   }
}
