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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoadPropertiesActionTest extends UnitTestSupport {

	@Test
	public void testLoadProperties() {
		LoadPropertiesAction loadProperties = new LoadPropertiesAction.Builder()
				.filePath("classpath:org/citrusframework/actions/load.properties")
				.build();

		loadProperties.execute(context);

		Assertions.assertNotNull(context.getVariable("${myVariable}"));
		Assertions.assertEquals("test", context.getVariable("${myVariable}"));
		Assertions.assertNotNull(context.getVariable("${user}"));
        Assertions.assertEquals("Citrus", context.getVariable("${user}"));
		Assertions.assertNotNull(context.getVariable("${welcomeText}"));
		Assertions.assertEquals("Hello Citrus!", context.getVariable("${welcomeText}"));
		Assertions.assertNotNull(context.getVariable("${todayDate}"));
        Assertions.assertEquals("Today is " + new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())) + "!",
                context.getVariable("${todayDate}"));
	}

	@Test
    public void testUnknownVariableInLoadProperties() {
		LoadPropertiesAction loadProperties = new LoadPropertiesAction.Builder()
				.filePath("classpath:org/citrusframework/actions/load-error.properties")
				.build();

        try {
            loadProperties.execute(context);
        } catch(CitrusRuntimeException e) {
            Assertions.assertEquals("Unknown variable 'unknownVar'", e.getMessage());
            return;
        }

        Assertions.fail("Missing exception for unkown variable in property file");
	}
}
