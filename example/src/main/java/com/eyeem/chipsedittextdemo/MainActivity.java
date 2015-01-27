package com.eyeem.chipsedittextdemo;

import android.animation.LayoutTransition;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;

import com.eyeem.chipsedittextdemo.mortarflow.FlowBundler;
import com.eyeem.chipsedittextdemo.mortarflow.FlowDep;
import com.eyeem.chipsedittextdemo.mortarflow.FramePathContainerView;
import com.eyeem.chipsedittextdemo.mortarflow.HandlesBack;
import com.eyeem.chipsedittextdemo.mortarflow.HandlesUp;
import com.eyeem.chipsedittextdemo.mortarflow.ScopeSingleton;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import flow.Flow;
import flow.HasParent;
import flow.Path;
import flow.PathContainerView;
import mortar.Mortar;
import mortar.MortarActivityScope;
import mortar.MortarScope;
import mortar.MortarScopeDevHelper;
import mortar.dagger2support.Dagger2;

public class MainActivity extends ActionBarActivity implements Flow.Dispatcher {

   @ScopeSingleton(Component.class)
   @dagger.Component(dependencies = App.Component.class)
   public interface Component extends FlowDep {
      void inject(MainActivity mainActivity);
   }

   @Inject FlowBundler flowBundler;

   @InjectView(R.id.container) FramePathContainerView container;
   /* package */ HandlesBack containerAsHandlesBack;
   /* package */ HandlesUp containerAsHandlesUp;
   @InjectView(R.id.toolbar) Toolbar toolbar;
   @InjectView(R.id.drawer) DrawerLayout drawer;

   private Flow flow;
   private MortarActivityScope activityScope;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      MortarScope parentScope = Mortar.getScope(getApplication());

      String scopeName = MainActivity.class.getName();
      activityScope = (MortarActivityScope) parentScope.findChild(scopeName);
      if (activityScope == null) {
         Component activityGraph = Dagger2.buildComponent(Component.class, parentScope.getObjectGraph());
         activityScope = Mortar.createActivityScope(parentScope, scopeName, activityGraph);
      }
      Dagger2.<Component>get(this).inject(this);
      flow = flowBundler.onCreate(savedInstanceState);

      activityScope.onCreate(savedInstanceState);
      setContentView(R.layout.root_layout);
      ButterKnife.inject(this);
      containerAsHandlesBack = container;
      containerAsHandlesUp = container;

      setSupportActionBar(toolbar);

//      // handle ActionBarDrawerToggle
//      ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
//      actionBarDrawerToggle.syncState();
//
//      // handle different Drawer States
//      drawer.setDrawerListener(actionBarDrawerToggle);
   }

   @Override public Object getSystemService(String name) {
      if (Mortar.isScopeSystemService(name)) {
         return activityScope;
      }
      if (Flow.isFlowSystemService(name)) {
         return flow;
      }
      return super.getSystemService(name);
   }

   @Override protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      activityScope.onSaveInstanceState(outState);
   }

   @Override public void onBackPressed() {
      boolean handled = Flow.get(container.getCurrentChild()).goBack();
      if (!handled) {
         finish();
      }
   }

   @Override protected void onDestroy() {
      super.onDestroy();

      // activityScope may be null in case isWrongInstance() returned true in onCreate()
      if (isFinishing() && activityScope != null) {
         MortarScope parentScope = Mortar.getScope(getApplication());
         parentScope.destroyChild(activityScope);
         activityScope = null;
      }
   }

   @Override protected void onResume() {
      super.onResume();
      flow.setDispatcher(this);
   }

   @Override protected void onPause() {
      super.onPause();
      flow.removeDispatcher(this);
   }

   @Override public void dispatch(Flow.Traversal traversal,
                                  Flow.TraversalCallback traversalCallback) {
      Path path = traversal.destination.current();
      container.dispatch(traversal, traversalCallback);

      //toolbar.setTitle(path.getClass().getSimpleName());
      getSupportActionBar().setTitle(path.getClass().getSimpleName());


      boolean hasUp = path instanceof HasParent;
      getSupportActionBar().setDisplayHomeAsUpEnabled(hasUp);
      getSupportActionBar().setHomeButtonEnabled(hasUp);
      // Log.d("MainActivity", MortarScopeDevHelper.scopeHierarchyToString(activityScope));
   }

   /** Inform the view about up events. */
   @Override public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == android.R.id.home) return containerAsHandlesUp.onUpPressed();
      return super.onOptionsItemSelected(item);
   }

   @Override public boolean onCreateOptionsMenu(Menu menu) {
      menu.add(R.string.action_mortar_hierarchy)
         .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
               Log.d(MainActivity.class.getSimpleName(), MortarScopeDevHelper.scopeHierarchyToString(activityScope));
               return true;
            }
         });
      return true;
   }
}
