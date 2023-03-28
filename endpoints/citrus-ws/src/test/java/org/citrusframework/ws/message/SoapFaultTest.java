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

package org.citrusframework.ws.message;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SoapFaultTest {

    @Test
    public void testToString() {
        SoapFault fault = new SoapFault();
        fault.faultCode("TEC-1000");
        Assert.assertTrue(fault.toString().endsWith("[fault: {TEC-1000}]"));

        fault.faultString("Internal server error");
        Assert.assertTrue(fault.toString().endsWith("[fault: {TEC-1000}{Internal server error}{en}]"));

        fault.faultCode("{http://citrusframework.org}TEC-1000");
        Assert.assertTrue(fault.toString().endsWith("[fault: {{http://citrusframework.org}TEC-1000}{Internal server error}{en}]"));

        fault.locale("DE");
        Assert.assertTrue(fault.toString().endsWith("[fault: {{http://citrusframework.org}TEC-1000}{Internal server error}{de}]"));

        fault.faultActor("Actor");
        Assert.assertTrue(fault.toString().endsWith("[fault: {{http://citrusframework.org}TEC-1000}{Internal server error}{de}{Actor}]"));
    }
}
