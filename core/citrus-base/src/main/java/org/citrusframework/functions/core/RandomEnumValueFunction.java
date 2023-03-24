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

package org.citrusframework.functions.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

import java.util.List;
import java.util.Random;

/**
 * Function to choose one random value from a list of strings. The enumeration values to choose from
 * can either be specified as parameters or in the {@link RandomEnumValueFunction#values} property of 
 * an instance of this class. These two possibilities can only be used exclusively - either empty values
 * property and non-empty parameters or empty parameters and non-empty values property.
 * <p>Example custom function definition and the corresponding usage in a test:</p>
 * 
 * <code>
 * <pre>
 * &lt;bean id="myCustomFunctionLibrary" class="org.citrusframework.functions.FunctionLibrary"&gt;
 *  &lt;property name="name" value="myCustomFunctionLibrary" /&gt;
 *  &lt;property name="prefix" value="custom:" /&gt;
 *  &lt;property name="members"&gt;
 *    &lt;map&gt;
 *      &lt;entry key="randomHttpStatusCode"&gt;
 *        &lt;bean class="org.citrusframework.functions.core.RandomEnumValueFunction"&gt;
 *          &lt;property name="values"&gt;
 *            &lt;list&gt;
 *              &lt;value&gt;200&lt;/value&gt;
 *              &lt;value&gt;500&lt;/value&gt;
 *              &lt;value&gt;401&lt;/value&gt;
 *            &lt;/list&gt;
 *          &lt;/property&gt;
 *        &lt;/bean&gt;
 *      &lt;/entry&gt;
 *    &lt;/map&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * </code>
 * and the corresponding usage which sets the httpStatusCode to one of the configured values - 200, 500 or 401:
 * <code>
 * <pre>
 * &lt;variable name="httpStatusCode" value="custom:randomHttpStatusCode()" /&gt;
 * </pre>
 * </code>
 * <p>The other usage possibility is to choose a random value from a list of values given as argument 
 * like this which achieves the same result as the previously shown custom function:</p>
 * <code>
 * <pre>
 * &lt;variable name="httpStatusCode" value="citrus:randomEnumValue('200', '401', '500')" /&gt;
 * </pre>
 * </code>
 * You should choose which one of the two flavours to use based on the number of times you use this function - if you need it in
 * only one special case you may go with specifying the list as arguments otherwise you should define a custom function and reuse it. 
 * 
 * @author Dimo Velev (dimo.velev@gmail.com)
 */
public class RandomEnumValueFunction implements Function {
	private Random random = new Random(System.currentTimeMillis());
	private List<String> values = null;
	
	/**
	 * @see Function#execute(java.util.List, org.citrusframework.context.TestContext)
	 */
	public String execute(List<String> params, TestContext context) {
		if (values == null) {
			return randomValue(params);
		} else {
			if (!params.isEmpty()) {
				throw new InvalidFunctionUsageException("The enumeration values have already been set");
			}
			return randomValue(values);
		}
	}

	/**
	 * Pseudo-randomly choose one of the supplied values and return it.
	 * 
	 * @param values
	 * @return
	 * @throws IllegalArgumentException if the values supplied are <code>null</code> or an empty list.
	 */
	protected String randomValue(List<String> values) {
		if (values == null || values.isEmpty()) {
			throw new InvalidFunctionUsageException("No values to choose from");
		}
		
		final int idx = random.nextInt(values.size());
		
		return values.get(idx);
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
}
