/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
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

        try {
            loadProperties.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'unknownVar'");
            return;
        }

        Assert.fail("Missing exception for unkown variable in property file");
	}
}
