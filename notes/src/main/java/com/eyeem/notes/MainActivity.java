package com.eyeem.notes;

import android.content.Context;
import android.os.Bundle;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;

import com.eyeem.notes.core.StorageDep;
import com.eyeem.notes.core.AppDep;
import com.eyeem.notes.mortarflow.FlowBundler;
import com.eyeem.notes.mortarflow.FlowDep;
import com.eyeem.notes.mortarflow.FramePathContainerView;
import com.eyeem.notes.mortarflow.HandlesBack;
import com.eyeem.notes.mortarflow.HandlesUp;
import com.eyeem.notes.mortarflow.ScopeSingleton;
import com.eyeem.notes.screen.ActionBarOwner;
import com.squareup.otto.Bus;

import java.util.List;

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

import static com.eyeem.notes.mortarflow.Utils.createComponent;
import static mortar.MortarScope.buildChild;
import static mortar.MortarScope.findChild;
import static mortar.MortarScope.getScope;

public class MainActivity extends ActionBarActivity implements Flow.Dispatcher, ActionBarOwner.Activity {

   @ScopeSingleton(Component.class)
   @dagger.Component(dependencies = App.Component.class)
   public interface Component extends FlowDep, AppDep, StorageDep {
      void inject(MainActivity mainActivity);
   }

   @Inject FlowBundler flowBundler;
   @Inject ActionBarOwner.Presenter actionBarPresenter;
   @Inject Bus bus;

   @InjectView(R.id.container) FramePathContainerView container;
   /* package */ HandlesBack containerAsHandlesBack;
   /* package */ HandlesUp containerAsHandlesUp;
   @InjectView(R.id.toolbar) Toolbar toolbar;
   @InjectView(R.id.drawer) DrawerLayout drawer;

   private Flow flow;
   private List<ActionBarOwner.MenuAction> menuActions;

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
      actionBarPresenter.takeView(this);
   }

   private String getScopeName() {
      return getClass().getName();
   }

   @Override public Object getSystemService(String name) {
      MortarScope activityScope = findChild(getApplicationContext(), getScopeName());

      App.Component appComponent = getScope(getApplicationContext()).getService(DaggerService.SERVICE_NAME);

      if (activityScope == null) {
         activityScope = buildChild(getApplicationContext())
            .withService(BundleServiceRunner.SERVICE_NAME, new BundleServiceRunner())
            .withService(DaggerService.SERVICE_NAME, createComponent(Component.class, appComponent))
            .build(getScopeName());
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
      actionBarPresenter.dropView(this);

      // activityScope may be null in case isWrongInstance() returned true in onCreate()
      if (isFinishing()) {
         MortarScope activityScope = findChild(getApplicationContext(), getScopeName());
         if (activityScope != null) activityScope.destroy();
      }

      super.onDestroy();
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

      boolean hasUp = path instanceof HasParent;
      String title = path.getClass().getSimpleName();

      List<ActionBarOwner.MenuAction> actions = (path instanceof ActionBarOwner.MenuActions) ? ((ActionBarOwner.MenuActions) path).menuActions() : null;
      actionBarPresenter.setConfig(new ActionBarOwner.Config(false, hasUp, title, actions));

      container.dispatch(traversal, traversalCallback);
   }

   /** Inform the view about up events. */
   @Override public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == android.R.id.home) return containerAsHandlesUp.onUpPressed();
      return super.onOptionsItemSelected(item);
   }

   /** Configure the action bar menu as required by {@link ActionBarOwner.Activity}. */
   @Override public boolean onCreateOptionsMenu(Menu menu) {
      if (menuActions != null) {
         for (final ActionBarOwner.MenuAction menuAction : menuActions) {
            menu.add(menuAction.stringId)
               .setOnMenuItemClickListener(
                  (MenuItem menuItem) -> { bus.post(menuAction); return true; }
               );
         }
      }
      menu.add(R.string.action_mortar_hierarchy)
         .setOnMenuItemClickListener(
            (MenuItem menuItem) -> {
               MortarScope activityScope = findChild(getApplicationContext(), getScopeName());
               Log.d(MainActivity.class.getSimpleName(), MortarScopeDevHelper.scopeHierarchyToString(activityScope));
               return true;
            }
         );
      return true;
   }


///// action bar owner
   @Override public void setShowHomeEnabled(boolean enabled) {
      ActionBar actionBar = getSupportActionBar();
      actionBar.setDisplayShowHomeEnabled(enabled);
   }

   @Override public void setUpButtonEnabled(boolean enabled) {
      ActionBar actionBar = getSupportActionBar();
      actionBar.setDisplayHomeAsUpEnabled(enabled);
      actionBar.setHomeButtonEnabled(enabled);
   }

   @Override public void setTitle(CharSequence title) {
      getSupportActionBar().setTitle(title);
   }

   @Override public void setMenu(List<ActionBarOwner.MenuAction> actions) {
      if (actions != menuActions) {
         menuActions = actions;
         invalidateOptionsMenu();
      }
   }

   @Override public Context getContext() {
      return this;
   }
}
