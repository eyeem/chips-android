package com.eyeem.notes.mortarflow;

/*
 * Copyright 2014 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;

import flow.Flow;
import flow.History;
import flow.StateParceler;


/**
 * Handles Bundle persistence of a Flow.
 */
public abstract class FlowBundler {
   private static final String FLOW_KEY = "flow_key";

   private final StateParceler parceler;

   private Flow flow;

   protected FlowBundler(StateParceler parceler) {
      this.parceler = parceler;
   }

   public Flow onCreate(@Nullable Bundle savedInstanceState) {
      if (flow != null) return flow;

      History restoredHistory = null;
      if (savedInstanceState != null && savedInstanceState.containsKey(FLOW_KEY)) {
         restoredHistory = History.from(savedInstanceState.getParcelable(FLOW_KEY), parceler);
      }
      flow = new Flow(getColdStartHistory(restoredHistory));
      return flow;
   }

   public void onSaveInstanceState(Bundle outState) {
      History history = getHistoryToSave(flow.getHistory());
      if (history == null) return;
      outState.putParcelable(FLOW_KEY, history.getParcelable(parceler));
   }

   /**
    * Returns the history that should be archived by {@link #onSaveInstanceState}. Overriding
    * allows subclasses to handle cases where the current configuration is not one that should
    * survive process death.  The default implementation returns a HistoryToSave that specifies
    * that view state should be persisted.
    *
    * @return the stack to archive, or null to archive nothing
    */
   @Nullable protected History getHistoryToSave(History history) {
      return history;
   }

   /**
    * Returns the history to initialize the new flow.
    *
    * @param restoredHistory the backstack recovered from the bundle passed to {@link #onCreate},
    *                          or null if there was no bundle or no backstack was found
    */
   protected abstract History getColdStartHistory(@Nullable History restoredHistory);
}
