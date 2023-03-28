/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.main.spring;

import org.citrusframework.main.CitrusApp;

/**
 * Main command line application callable via static run methods and command line main method invocation.
 * Command line options are passed to the application for optional arguments. Application will run until the
 * duration time option has passed by or until the JVM terminates.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusSpringApp extends CitrusApp {

    /**
     * Default constructor using default configuration.
     */
    public CitrusSpringApp() {
        super(new CitrusSpringAppConfiguration());
    }

    /**
     * Construct with given configuration.
     * @param configuration
     */
    public CitrusSpringApp(CitrusSpringAppConfiguration configuration) {
        super(configuration);
    }

    /**
     * Construct from command line arguments.
     * @param args
     */
    public CitrusSpringApp(String[] args) {
        this(new CitrusSpringAppOptions().apply(args));
    }
}
