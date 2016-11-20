package com.stabilise.core;

import java.util.Set;

import org.reflections.Reflections;

import com.stabilise.entity.component.Component;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.controller.IdleController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.collect.registry.DuplicatePolicy;
import com.stabilise.util.collect.registry.GeneralTypeFactory;
import com.stabilise.util.collect.registry.GeneralTypeFactory.ReflectiveFactory;
import com.stabilise.util.collect.registry.RegisterMe;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;


@Incomplete
@SuppressWarnings("unused")
public class GameData {
    
    private boolean registered = false;
    
    private TypeFactory<CPhysics> physics;
    private TypeFactory<CController> controllers;
    private TypeFactory<CCore> cores;
    private TypeFactory<Component> components;
    
    
    public void register() {
        if(registered) return;
        registered = true;
        
        Params params = new Params().reset();
        
        params.name = "EntityPhysicsComponentRegistry";
        physics = factory(params, CPhysics.class);
        
        params.name = "EntityControllerComponentRegistry";
        controllers = factory(params, CController.class);
        controllers.register(0, IdleController.class, () -> IdleController.INSTANCE);
        
        params.name = "EntityCoreComponentRegistry";
        cores = factory(params, CCore.class);
        
        params.name = "EntityComponentRegistry";
        params.excluded = new Class<?>[] { CPhysics.class, CController.class, CCore.class };
        components = factory(params, Component.class);
    }
    
    private <T> TypeFactory<T> factory(Params params, Class<T> type) {
        TypeFactory<T> fac = new TypeFactory<>(
                new RegistryParams(params.name, params.capacity, params.dupePolicy)
        );
        Set<Class<? extends T>> candidates = params.ref.getSubTypesOf(type);
        for(Class<? extends T> c : candidates) {
            if(params.excluded != null) {
                for(Class<?> c2 : params.excluded) {
                    if(c2.isAssignableFrom(c)) continue;
                }
            }
            RegisterMe a = c.getAnnotation(RegisterMe.class);
            if(a == null) continue;
            int id = a.value();
            if(params.unsafe)
                fac.registerUnsafe(id, c);
            else
                fac.register(id, c, new ReflectiveFactory<T>(c));
        }
        return fac;
    }
    
    private <T> GeneralTypeFactory<T> genFactory(Params params, Class<T> type) {
        GeneralTypeFactory<T> fac = new GeneralTypeFactory<>(
                new RegistryParams(params.name, params.capacity, params.dupePolicy),
                params.args
        );
        Set<Class<? extends T>> candidates = params.ref.getSubTypesOf(type);
        for(Class<? extends T> c : candidates) {
            if(params.excluded != null) {
                for(Class<?> c2 : params.excluded) {
                    if(c2.isAssignableFrom(c)) continue;
                }
            }
            RegisterMe a = c.getAnnotation(RegisterMe.class);
            if(a == null) continue;
            int id = a.value();
            fac.register(id, c);
        }
        return fac;
    }
    
    private static class Params {
        Reflections ref = new Reflections(Constants.GAME_PACKAGE);
        
        // See RegistryParams
        String name;
        int capacity;
        DuplicatePolicy dupePolicy;
        
        /** Constructor arguments. null for parameterless constructor. null by
         * default. */
        Class<?>[] args;
        /** true to use {@link TypeFactory#registerUnsafe(int, Class) unsafe}
         * construction. Ignored if args is non-null. false by default. */
        boolean unsafe;
        /** Excluded class hierarchies. Subclasses of any of the given classes
         * will not be registered. null by default */
        Class<?>[] excluded;
        
        Params reset() {
            name = null;
            capacity = 16;
            dupePolicy = DuplicatePolicy.THROW_EXCEPTION;
            args = null;
            unsafe = false;
            excluded = null;
            return this;
        }
    }
    
}
