package com.elytradev.movingworld.common.experiments.proxy;

import com.elytradev.movingworld.client.experiments.EntityPlayerSPProxy;
import com.elytradev.movingworld.common.experiments.interact.EntityPlayerMPProxy;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.objenesis.ObjenesisHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Pretty much entirely magic, really horrible code that
 */
public class ContainerInterceptor implements MethodInterceptor {

    public static Object createProxy(Object realObject) {
        MethodInterceptor interceptor = new ContainerInterceptor();
        Enhancer e = new Enhancer();
        e.setUseCache(false);
        e.setSuperclass(realObject.getClass());
        e.setCallbackType(interceptor.getClass());
        Class classForProxy = e.createClass();
        Enhancer.registerCallbacks(classForProxy, new Callback[]{interceptor});
        Object createdProxy = ObjenesisHelper.newInstance(classForProxy);

        // Attempt to move data to prevent inital NPEs. We're also instantiating from absolutely nothing so this is probably the best thing we can do...
        for (Field realField : FieldUtils.getAllFieldsList(realObject.getClass())) {
            if (Modifier.isStatic(realField.getModifiers()))
                continue;
            realField.setAccessible(true);

            try {
                realField.set(createdProxy, realField.get(realObject));
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }

        // Done.

        return createdProxy;
    }

    private EntityPlayer getProxy(Object[] args) {
        Object arg0 = null;
        if (args.length != 0) {
            arg0 = args[0];
        }

        if (arg0 != null && arg0 instanceof EntityPlayer) {
            EntityPlayer playerProxy = null;

            EntityPlayer playerIn = (EntityPlayer) arg0;
            if (playerIn instanceof EntityPlayerMP && EntityPlayerMPProxy.hasProxy((EntityPlayerMP) playerIn)) {
                playerProxy = EntityPlayerMPProxy.getProxyForPlayer((EntityPlayerMP) playerIn, null, false);
            } else if (playerIn instanceof EntityPlayerSP && EntityPlayerSPProxy.hasProxy((EntityPlayerSP) playerIn)) {
                playerProxy = EntityPlayerSPProxy.getProxyForPlayer((EntityPlayerSP) playerIn, null, false);
            }

            return playerProxy;
        }

        return null;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object result = methodProxy.invokeSuper(obj, args);

        if (obj instanceof Container && (
                method.getName().equals("canInteractWith")
                        || method.getName().equals("func_75145_c"))) {
            if ((Boolean) result == true)
                return result;

            EntityPlayer proxyPlayer = getProxy(args);
            Boolean proxyCan = proxyPlayer != null ? (Boolean) methodProxy.invokeSuper(obj, new Object[]{proxyPlayer}) : false;

            System.out.println("Intercepted canInteractWith, " + proxyCan + " " + proxyPlayer);

            return proxyCan;
        }

        return result;
    }
}
