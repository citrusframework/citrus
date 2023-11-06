/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jbang;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class LoggingSupport {

    private static final AtomicBoolean LOG_INIT_DONE = new AtomicBoolean();

    private LoggingSupport() {
        // prevent instantiation of utility class
    }

    public static void configureLog(
            String level, boolean color, String engine) {
        if (LOG_INIT_DONE.compareAndSet(false, true)) {
            long pid = ProcessHandle.current().pid();
            System.setProperty("pid", Long.toString(pid));

            if ("cucumber".equals(engine)) {
                Configurator.initialize("CitrusJBang", "log4j2-cucumber.properties");
            } else if (!color) {
                Configurator.initialize("CitrusJBang", "log4j2-no-color.properties");
            } else {
                Configurator.initialize("CitrusJBang", "log4j2.properties");
            }
        }

        setRootLoggingLevel(level);
    }

    public static void setRootLoggingLevel(String level) {
        if (level != null) {
            level = level.toLowerCase();

            switch (level) {
                case "off" -> Configurator.setRootLevel(Level.OFF);
                case "trace" -> Configurator.setRootLevel(Level.TRACE);
                case "debug" -> Configurator.setRootLevel(Level.DEBUG);
                case "info" -> Configurator.setRootLevel(Level.INFO);
                case "warn" -> Configurator.setRootLevel(Level.WARN);
                case "error" -> Configurator.setRootLevel(Level.ERROR);
                case "fatal" -> Configurator.setRootLevel(Level.FATAL);
                default -> {
                    Configurator.setRootLevel(Level.INFO);
                }
            }
        }
    }

    public static class LoggingLevels implements Iterable<String> {

        @Override
        public Iterator<String> iterator() {
            return List.of("ERROR", "WARN", "INFO", "DEBUG", "TRACE").iterator();
        }
    }
}
