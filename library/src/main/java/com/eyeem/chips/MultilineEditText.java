package com.eyeem.chips;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: vishna
 * Date: 6/24/13
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultilineEditText extends EditText {
   public MultilineEditText(Context context) {
      super(context);
   }

   public MultilineEditText(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public MultilineEditText(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
      InputConnection connection = super.onCreateInputConnection(outAttrs);
      int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
      if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
         // clear the existing action
         outAttrs.imeOptions ^= imeActions;
         // set the DONE action
         outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
      }
      if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
         outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
      }
      return connection;
   }
}
