/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.functions.core;

import com.consol.citrus.context.TestContext;

import java.time.Instant;
import java.util.List;

/**
 * Function returning the actual timestamp.
 * 
 * @author Alexandr Kuznecov
 */
public class UnixTimestampFunction extends AbstractDateFunction {
    public String execute(List<String> parameterList, TestContext context) {
        return Long.toString(Instant.now().getEpochSecond());
    }
}
