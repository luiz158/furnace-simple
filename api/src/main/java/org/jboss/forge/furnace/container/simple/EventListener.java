/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.container.simple;

import java.lang.annotation.Annotation;

/**
 * <p>
 * To register an {@link EventListener}, a file must be created with the name
 * <code>META-INF/services/org.jboss.forge.furnace.container.simple.EventListener</code>, and each {@link EventListener}
 * implementation type name must be added on a separate line:
 * <p>
 * 
 * <pre>
 * public class ExampleEventListener implements EventListener
 * {
 *    // ...
 * }
 * </pre>
 * <p>
 * Example registration file:
 * </p>
 * 
 * <pre>
 * META-INF/services/org.jboss.forge.furnace.container.simple.EventListener
 * -------
 * org.example.ExampleEventListener
 * org.example.ExampleEventListener2
 * org.my.custom.MyEventListener
 * -------
 * </pre>
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface EventListener
{
   public void handleEvent(Object event, Annotation... qualifiers);
}
