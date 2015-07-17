package com.moorango.medli.lib.eventbus;

import com.moorango.medli.lib.eventbus.events.Event;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Subscriber {

    WeakReference<Object> reference;

    public Subscriber(Object object) {
        reference = new WeakReference<Object>(object);
    }

    public Boolean callEvent(Event event) {
        if (reference != null && reference.get() != null) {
            Object object = reference.get();
            try {
                Method method =
                        object.getClass().getMethod(EventBus.methodName, new Class[]{event.getClass()});
                method.invoke(object, event);
                return true;
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Object getReference() {
        return (reference != null) ? reference.get() : null;
    }

    public String getName() {
        if (reference != null && reference.get() != null) {
            return reference.get().getClass().getSimpleName();
        }
        return "";
    }
}