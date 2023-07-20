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

package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.CreateCamelRouteAction;

public class CreateRoutes implements CamelRouteActionBuilderWrapper<CreateCamelRouteAction.Builder> {
    private final CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder();

    public void setRouteContext(String routeContext) {
        builder.routeContext(routeContext);
    }

    @Override
    public CreateCamelRouteAction.Builder getBuilder() {
        return builder;
    }

}
