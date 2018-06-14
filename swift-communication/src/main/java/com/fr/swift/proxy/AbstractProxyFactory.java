package com.fr.swift.proxy;

import com.fr.swift.Invoker;
import com.fr.swift.ProxyFactory;
import com.fr.swift.URL;
import com.fr.swift.invoker.InvokerInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * This class created on 2018/6/8
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Invoker<T> invoker) {

        InvocationHandler invocationHandler = new InvokerInvocationHandler(invoker);
        Class interfaceClass = invoker.getInterface();
        T t = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]
                {interfaceClass}, invocationHandler);
        return t;
    }

    @Override
    public <T> T getProxy(T proxy, Class<T> type, URL url) {
        Invoker invoker = getInvoker(proxy, type, url);
        T t = (T) getProxy(invoker);
        return t;
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url, boolean sync) {
        return null;
    }
}
