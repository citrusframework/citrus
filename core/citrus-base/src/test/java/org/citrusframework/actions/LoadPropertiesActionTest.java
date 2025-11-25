/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LoadPropertiesActionTest extends UnitTestSupport {

    @Test
    public void testLoadProperties() {
        LoadPropertiesAction loadProperties = new LoadPropertiesAction.Builder()
                .filePath("classpath:org/citrusframework/actions/load.properties")
                .build();

        loadProperties.execute(context);

        Assert.assertNotNull(context.getVariable("${myVariable}"));
        Assert.assertEquals(context.getVariable("${myVariable}"), "test");
        Assert.assertNotNull(context.getVariable("${user}"));
        Assert.assertEquals(context.getVariable("${user}"), "Citrus");
        Assert.assertNotNull(context.getVariable("${welcomeText}"));
        Assert.assertEquals(context.getVariable("${welcomeText}"), "Hello Citrus!");
        Assert.assertNotNull(context.getVariable("${todayDate}"));
        Assert.assertEquals(context.getVariable("${todayDate}"),
                "Today is " + new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())) + "!");
    }

    @Test
    public void testUnknownVariableInLoadProperties() {
        LoadPropertiesAction loadProperties = new LoadPropertiesAction.Builder()
                .filePath("classpath:org/citrusframework/actions/load-error.properties")
                .build();

        assertThatThrownBy(() -> loadProperties.execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage(
                        format(
                                "Unable to extract value using expression 'unknownVar'!%nReason: Unknown key 'unknownVar' in Map.%nFrom object (java.util.concurrent.ConcurrentHashMap):%n{}"
                        )
                );
    }
}
