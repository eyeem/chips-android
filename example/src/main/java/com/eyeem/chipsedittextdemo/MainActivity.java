package com.eyeem.chipsedittextdemo;

import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;

import com.eyeem.chipsedittextdemo.adapter.StorageDep;
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
import mortar.MortarScope;
import mortar.MortarScopeDevHelper;
import mortar.bundler.BundleServiceRunner;
import mortar.dagger2support.DaggerService;

import static mortar.MortarScope.buildChild;
import static mortar.MortarScope.findChild;
import static mortar.MortarScope.getScope;
import static mortar.dagger2support.DaggerService.createComponent;

public class MainActivity extends ActionBarActivity implements Flow.Dispatcher {

   @ScopeSingleton(Component.class)
   @dagger.Component(dependencies = App.Component.class)
   public interface Component extends FlowDep, StorageDep {
      void inject(MainActivity mainActivity);
   }

   @Inject FlowBundler flowBundler;

   @InjectView(R.id.container) FramePathContainerView container;
   /* package */ HandlesBack containerAsHandlesBack;
   /* package */ HandlesUp containerAsHandlesUp;
   @InjectView(R.id.toolbar) Toolbar toolbar;
   @InjectView(R.id.drawer) DrawerLayout drawer;

   private Flow flow;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      BundleServiceRunner.getBundleServiceRunner(this).onCreate(savedInstanceState);

      DaggerService.<Component>getDaggerComponent(this).inject(this);
      flow = flowBundler.onCreate(savedInstanceState);

      setContentView(R.layout.root_layout);
      ButterKnife.inject(this);
      containerAsHandlesBack = container;
      containerAsHandlesUp = container;

      setSupportActionBar(toolbar);
   }

   private String getScopeName() {
      return getClass().getName();
   }

   @Override public Object getSystemService(String name) {
      MortarScope activityScope = findChild(getApplicationContext(), getScopeName());

      App.Component appComponent = getScope(getApplicationContext()).getService(DaggerService.SERVICE_NAME);

      if (activityScope == null) {
         activityScope = buildChild(getApplicationContext(), getScopeName())
            .withService(BundleServiceRunner.SERVICE_NAME, new BundleServiceRunner())
            .withService(DaggerService.SERVICE_NAME, createComponent(Component.class, appComponent))
            .build();
      }

      if (activityScope.hasService(name)) return activityScope.getService(name);

      if (Flow.isFlowSystemService(name)) {
         return flow;
      }
      return super.getSystemService(name);
   }

   @Override protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      BundleServiceRunner.getBundleServiceRunner(this).onSaveInstanceState(outState);
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
      if (isFinishing()) {
         MortarScope activityScope = findChild(getApplicationContext(), getScopeName());
         if (activityScope != null) activityScope.destroy();
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
               MortarScope activityScope = findChild(getApplicationContext(), getScopeName());
               Log.d(MainActivity.class.getSimpleName(), MortarScopeDevHelper.scopeHierarchyToString(activityScope));
               return true;
            }
         });
      return true;
   }
}
