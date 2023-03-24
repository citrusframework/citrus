/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.ssh.server;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Roland Huss
 * @since 05.09.12
 */
public class SimplePasswordAuthenticatorTest {

    @Test
    public void simple() {
        SimplePasswordAuthenticator auth = new SimplePasswordAuthenticator("roland","secret");
        assertTrue(auth.authenticate("roland","secret",null));
        assertFalse(auth.authenticate("guenther","uebel",null));
    }
}
