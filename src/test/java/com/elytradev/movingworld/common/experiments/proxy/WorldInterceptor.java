package com.elytradev.movingworld.common.experiments.proxy;

        import net.sf.cglib.proxy.MethodInterceptor;
        import net.sf.cglib.proxy.MethodProxy;

        import java.lang.reflect.Method;

/**
 * Created by darkevilmac on 3/17/2017.
 */
public class WorldInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return null;
    }
}
