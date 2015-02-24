package com.eyeem.notes.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.eyeem.notes.mortarflow.ScreenScoper;

import java.util.ArrayList;
import java.util.List;

import flow.Layouts;
import flow.Path;
import mortar.MortarScope;

import static mortar.MortarScope.getScope;

/**
 * Created by vishna on 16/02/15.
 */
public class ScreenPagerAdapter extends PagerAdapter {

   private final Context mContext;
   private final List<Path> screens;
   private ScreenScoper screenScoper;

   public ScreenPagerAdapter(Context context) {
      mContext = context;
      this.screens = new ArrayList<>();
      this.screenScoper = new ScreenScoper();
   }

   public void addScreen(Path... newScreens) {
      for (Path newScreen : newScreens) {
         screens.add(newScreen);
      }
      notifyDataSetChanged();
   }

   protected Context getContext()
   {
      return mContext;
   }

   @Override public Object instantiateItem(ViewGroup container, int position) {
      Path screen = screens.get(position);
      String childName = screen.getClass().getSimpleName() + "#" + position;

      MortarScope newChildScope =  screenScoper.getScreenScope(mContext, childName, screen);

      Context childContext = newChildScope.createContext(mContext);
      View newChild = Layouts.createView(childContext, screen);
      container.addView(newChild);
      return newChild;
   }

   @Override public void destroyItem(ViewGroup container, int position, Object object) {
      View view = ((View) object);
      MortarScope childScope = getScope(view.getContext());
      container.removeView(view);
      childScope.destroy();
   }

   @Override public int getCount() {
      return screens.size();
   }

   public final Path getItem(int position) {
      return screens.get(position);
   }

   @Override public boolean isViewFromObject(View view, Object object) {
      return view.equals(object);
   }
}
