/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.groovy.dsl.actions.model;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

/**
 * @author Christoph Deppisch
 */
public class BodySpec extends GroovyObjectSupport {

    private BodySpec delegate;

    public Object methodMissing(String name, Object argLine) {
        Object[] args = new Object[]{};

        if (argLine != null) {
            args = (Object[]) argLine;
        }

        if (args.length == 0) {
            if (name.equals("json")) {
                delegate = new JsonBodySpec();
                return delegate;
            }

            if (name.equals("xml")) {
                delegate = new XmlBodySpec();
                return delegate;
            }
        }

        throw new MissingMethodException(name, this.getClass(), args);
    }

    public String get(Object result) {
        if (delegate != null) {
            return delegate.get(result);
        }

        return result.toString();
    }
}
