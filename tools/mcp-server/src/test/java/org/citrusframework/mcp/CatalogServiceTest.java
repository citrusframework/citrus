/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.mcp;

import org.citrusframework.CitrusVersion;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CatalogServiceTest {

    @Test
    public void shouldUseCitrusVersion() {
        Assertions.assertEquals(CitrusVersion.version(), new CatalogService().getVersion());
    }

    @Test
    public void shouldKeepGivenVersion() {
        Assertions.assertEquals("5.3.0", CatalogService.requireVersion("5.3.0"));
    }

    @Test
    public void shouldRejectBlankVersion() {
        Assertions.assertThrows(CitrusRuntimeException.class, () -> CatalogService.requireVersion(""));
    }

    @Test
    public void shouldRejectNullVersion() {
        Assertions.assertThrows(CitrusRuntimeException.class, () -> CatalogService.requireVersion(null));
    }
}
