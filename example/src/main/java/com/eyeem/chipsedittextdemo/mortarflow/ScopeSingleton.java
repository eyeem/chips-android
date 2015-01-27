package com.eyeem.chipsedittextdemo.mortarflow;

import javax.inject.Scope;

@Scope
public @interface ScopeSingleton {
   Class<?> value();
}
