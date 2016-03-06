package com.moorango.medli.lib.eventbus;

import com.moorango.medli.lib.eventbus.events.Event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by cmac147 on 7/9/15.
 */
public class EventBus {

    public static final String methodName = "onEvent";
    private static EventBus instance;
    private ConcurrentHashMap<Class<Event>, List<Subscriber>> subscriberHash;
    private ConcurrentHashMap<Class<? extends Object>, Method[]> methodHash;

    private EventBus() {
        subscriberHash = new ConcurrentHashMap<Class<Event>, List<Subscriber>>();
        methodHash = new ConcurrentHashMap<Class<? extends Object>, Method[]>();
    }

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    private Method[] getMethodList(Class<? extends Object> classObject) {
        Method[] methodList = methodHash.get(classObject);
        if (methodList == null) {
            methodList = classObject.getMethods();
            methodHash.put(classObject, methodList);
        }
        return methodList;
    }

    private void removeExistingInstance(List<Subscriber> subscribers, Object object) {
        if (object != null) {
            Subscriber newSubscriber = new Subscriber(object);
            for (int i = 0; i < subscribers.size(); i++) {
                Subscriber thisSubscriber = subscribers.get(i);
                if ((thisSubscriber != null) && (thisSubscriber.getName().equals(newSubscriber.getName()))) {
                    subscribers.remove(i--);
                }
            }
        }
    }

    public synchronized void register(Object subscriber) {
        register(subscriber, false);
    }

    public synchronized void register(Object subscriber, Boolean checkDuplicate) {
        Method[] methodList = getMethodList(subscriber.getClass());
        for (Method method : methodList) {
            if (method.getName().equals(methodName)) {
                Class<Event> key = getParameterHash(method.getParameterTypes());
                List<Subscriber> subscriberList = subscriberHash.get(key);
                if (subscriberList == null) {
                    subscriberList = new ArrayList<Subscriber>();
                    subscriberHash.put(key, subscriberList);
                } else if (checkDuplicate) {
                    removeExistingInstance(subscriberList, subscriber);
                }
                subscriberList.add(new Subscriber(subscriber));
            }
        }
    }

    public synchronized void post(Event event) {
        Class<Event> key = (Class<Event>) event.getClass();
        List<Subscriber> subscribers = subscriberHash.get(key);
        if (subscribers != null) {
            Iterator<Subscriber> iterator = subscribers.iterator();
            while (iterator.hasNext()) {
                Subscriber subscriber = iterator.next();
                if (!subscriber.callEvent(event)) {
                    iterator.remove();
                }
            }
        }
    }

    private Class<Event> getParameterHash(Class<?>[] RequestParams) {

        return (Class<Event>) RequestParams[0];
    }
}
