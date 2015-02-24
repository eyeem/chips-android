package com.eyeem.notes.utils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by vishna on 18/02/15.
 */
public class RxBus {

   private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

   public void send(Object o) {
      _bus.onNext(o);
   }

   public Observable<Object> toObserverable() {
      return _bus;
   }

}
