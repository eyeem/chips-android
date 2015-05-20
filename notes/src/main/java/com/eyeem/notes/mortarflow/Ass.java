package com.eyeem.notes.mortarflow;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vishna on 17/04/15.
 */
public class Ass {

   public static Parcelable onSave(ViewGroup view, Parcelable superState) {
      SavedState ss = new SavedState(superState);
      ss.childrenStates = new SparseArray();
      for (int i = 0; i < view.getChildCount(); i++) {
         view.getChildAt(i).saveHierarchyState(ss.childrenStates);
      }
      return ss;
   }

   public static Parcelable onLoad(ViewGroup view, Parcelable state) {
      SavedState ss = (SavedState) state;
      for (int i = 0; i < view.getChildCount(); i++) {
         view.getChildAt(i).restoreHierarchyState(ss.childrenStates);
      }
      return ss.getSuperState();
   }

   static class SavedState extends View.BaseSavedState {
      SparseArray childrenStates;

      SavedState(Parcelable superState) {
         super(superState);
      }

      private SavedState(Parcel in, ClassLoader classLoader) {
         super(in);
         childrenStates = in.readSparseArray(classLoader);
      }

      @Override
      public void writeToParcel(Parcel out, int flags) {
         super.writeToParcel(out, flags);
         out.writeSparseArray(childrenStates);
      }

      public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
         @Override
         public SavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new SavedState(source, loader);
         }

         @Override
         public SavedState createFromParcel(Parcel source) {
            return createFromParcel(null);
         }

         public SavedState[] newArray(int size) {
            return new SavedState[size];
         }
      };
   }
}
