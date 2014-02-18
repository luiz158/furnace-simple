/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.spi.ExportedInstance;
import org.jboss.forge.furnace.spi.ServiceRegistry;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.forge.furnace.util.ClassLoaders;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class SimpleServiceRegistry implements ServiceRegistry
{
   private final Furnace furnace;
   private final Addon addon;
   private final Set<Class<?>> serviceTypes;

   private final Map<String, ExportedInstance<?>> instanceCache = new WeakHashMap<>();
   private final Map<String, Set<ExportedInstance<?>>> instancesCache = new WeakHashMap<>();

   public SimpleServiceRegistry(Furnace furnace, Addon addon, Set<Class<?>> serviceTypes)
   {
      this.furnace = furnace;
      this.addon = addon;
      this.serviceTypes = serviceTypes;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> Set<ExportedInstance<T>> getExportedInstances(String clazz)
   {
      try
      {
         return getExportedInstances((Class<T>) Class.forName(clazz, false, addon.getClassLoader()));
      }
      catch (ClassNotFoundException e)
      {
         return Collections.emptySet();
      }
   }

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public <T> Set<ExportedInstance<T>> getExportedInstances(Class<T> clazz)
   {
      Addons.waitUntilStarted(addon);

      if(clazz.getClassLoader() != addon.getClassLoader())
         return Collections.emptySet();
      
      Set<ExportedInstance<T>> result = (Set) instancesCache.get(clazz.getName());

      if (result == null)
      {
         result = new HashSet<>();
         for (Class<?> type : serviceTypes)
         {
            if (clazz.isAssignableFrom(type))
            {
               result.add(new SimpleExportedInstanceImpl<>(furnace, addon, (Class<T>) type));
            }
         }

         instancesCache.put(clazz.getName(), (Set) result);
      }
      return result;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> ExportedInstance<T> getExportedInstance(String clazz)
   {
      try
      {
         return getExportedInstance((Class<T>) Class.forName(clazz, false, addon.getClassLoader()));
      }
      catch (ClassNotFoundException e)
      {
         return null;
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> ExportedInstance<T> getExportedInstance(final Class<T> clazz)
   {
      Assert.notNull(clazz, "Requested Class type may not be null");
      Addons.waitUntilStarted(addon);

      if(clazz.getClassLoader() != addon.getClassLoader())
         return null;
         
      ExportedInstance<T> result = (ExportedInstance<T>) instanceCache.get(clazz.getName());
      if (result == null)
      {
         try
         {
            result = ClassLoaders.executeIn(addon.getClassLoader(), new Callable<ExportedInstance<T>>()
            {
               @Override
               public ExportedInstance<T> call() throws Exception
               {
                  for (Class<?> type : serviceTypes)
                  {
                     if (clazz.isAssignableFrom(type))
                     {
                        return new SimpleExportedInstanceImpl<>(furnace, addon, (Class<T>) type);
                     }
                  }

                  if (ClassLoaders.ownsClass(addon.getClassLoader(), clazz))
                     return new SimpleExportedInstanceImpl<>(furnace, addon, clazz);

                  return null;
               }
            });

            if (result != null)
               instanceCache.put(clazz.getName(), result);
         }
         catch (Exception e)
         {
            throw new ContainerException("Could not get service of type [" + clazz + "] from addon ["
                     + addon
                     + "]", e);
         }

      }
      return result;
   }

   @Override
   public Set<Class<?>> getExportedTypes()
   {
      return Collections.unmodifiableSet(serviceTypes);
   }

   @Override
   public <T> Set<Class<T>> getExportedTypes(Class<T> type)
   {
      Set<Class<T>> result = new HashSet<>();
      for (Class<?> serviceType : serviceTypes)
      {
         if (type.isAssignableFrom(serviceType))
         {
            result.add(type);
         }
      }
      return result;
   }

   @Override
   public boolean hasService(Class<?> clazz)
   {
      Addons.waitUntilStarted(addon);
      for (Class<?> service : serviceTypes)
      {
         if (clazz.isAssignableFrom(service))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean hasService(String clazz)
   {
      try
      {
         return hasService(Class.forName(clazz, false, addon.getClassLoader()));
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }
   }

   @Override
   public String toString()
   {
      return serviceTypes.toString();
   }
}