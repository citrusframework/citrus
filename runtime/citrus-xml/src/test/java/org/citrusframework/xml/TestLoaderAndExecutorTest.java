/*
 * Copyright 2024 the original author or authors.
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

package org.citrusframework.xml;

import org.citrusframework.common.TestLoaderAndExecutor;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.citrusframework.common.TestLoader.XML;
import static org.citrusframework.common.TestLoaderAndExecutor.lookup;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TestLoaderAndExecutorTest {

    @Test
    public void shouldLookupTestLoader() {
        assertTrue(lookup().containsKey(XML));

        Optional<TestLoaderAndExecutor> lookup = lookup(XML);
        assertTrue(lookup.isPresent());
        assertEquals(XmlTestLoader.class, lookup.get().getClass());
    }
}
