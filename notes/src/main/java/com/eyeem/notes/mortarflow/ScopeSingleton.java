package com.eyeem.notes.mortarflow;

import javax.inject.Scope;

@Scope
public @interface ScopeSingleton {
   Class<?> value();
}
