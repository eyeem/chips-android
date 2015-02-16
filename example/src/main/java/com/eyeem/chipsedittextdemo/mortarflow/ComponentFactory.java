package com.eyeem.chipsedittextdemo.mortarflow;

import android.content.Context;

public interface ComponentFactory {
   /**
    * createComponent(withComponent.value(), parentComponent)
    */
   Object createDaggerComponent(Context context, Object parentComponent);
}
