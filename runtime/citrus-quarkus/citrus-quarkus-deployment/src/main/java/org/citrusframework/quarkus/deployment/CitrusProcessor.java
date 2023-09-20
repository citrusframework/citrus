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

package org.citrusframework.quarkus.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;

/**
 * Processor adding build steps to fully index Citrus API and base modules.
 * @author Christoph Deppisch
 */
public class CitrusProcessor {

    private static final String FEATURE = "citrus";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    /**
     * Citrus API has a lot of annotations
     * <p>
     * rather than hard coding them all we index them and discover them
     */
    @BuildStep
    IndexDependencyBuildItem indexCitrusApi() {
        return new IndexDependencyBuildItem("org.citrusframework", "citrus-api");
    }

    /**
     * Citrus base module gets indexed in order to discover all beans.
     */
    @BuildStep
    IndexDependencyBuildItem indexCitrusBase() {
        return new IndexDependencyBuildItem("org.citrusframework", "citrus-base");
    }
}
