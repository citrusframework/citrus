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

package org.citrusframework.log;

/**
 * Modifier masks output that gets printed to an output stream. Usually used
 * to mask sensitive data like passwords and secrets when printed to the logger output.
 *
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface LogModifier {

    /**
     * Mask given logger statement and apply custom modifications before
     * the logger is printed to the output stream.
     * @param statement
     * @return
     */
    String mask(String statement);
}
