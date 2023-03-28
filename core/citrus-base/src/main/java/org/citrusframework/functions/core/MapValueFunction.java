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

import java.util.List;
import java.util.Map;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

/**
 * Function to map the function's argument to a corresponding value configured using a map.
 * <p>Example of the function definition and its usage:</p>
 *
 * <code>
 * <pre>
 * &lt;bean id="myCustomFunctionLibrary" class="org.citrusframework.functions.FunctionLibrary"&gt;
 *  &lt;property name="name" value="myCustomFunctionLibrary" /&gt;
 *  &lt;property name="prefix" value="custom:" /&gt;
 *  &lt;property name="members"&gt;
 *    &lt;map&gt;
 *      &lt;entry key="mapHttpStatusCodeToMessage"&gt;
 *        &lt;bean class="org.citrusframework.functions.core.MapValueFunction"&gt;
 *          &lt;property name="values"&gt;
 *            &lt;map&gt;
 *              &lt;entry key="200" value="OK" /&gt;
 *              &lt;entry key="401" value="Unauthorized" /&gt;
 *              &lt;entry key="500" value="Internal Server Error" /&gt;
 *            &lt;/map&gt;
 *          &lt;/property&gt;
 *        &lt;/bean&gt;
 *      &lt;/entry&gt;
 *    &lt;/map&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * </code>
 * and the corresponding usage in a test which maps the HTTP status code 500 to its message 'Internal Server Error':
 * <code>
 * <pre>
 * &lt;variable name="httpStatusCodeMessage" value="custom:mapHttpStatusCodeToMessage('500')" /&gt;
 * </pre>
 * </code>
 *
 * @author Dimo Velev (dimo.velev@gmail.com)
 *
 */
public class MapValueFunction implements Function, InitializingPhase {

    /** Mappings for key value logic in this function */
	private Map<String, String> map;

	@Override
	public String execute(List<String> params, TestContext context) {
		if (params.size() != 1) {
			throw new InvalidFunctionUsageException("Expected exactly one argument but got " + params.size());
		}

		final String key = params.get(0);
		final String result = map.get(key);

		if (result == null) {
			throw new InvalidFunctionUsageException("No mapping found for \"" + key + "\"");
		}

		return result;
	}

	@Override
	public void initialize() {
		if (map == null) {
			throw new CitrusRuntimeException("MapValueFunction must not use an empty value map");
		}
	}

	/**
	 * Gets the mappings for this function.
	 * @return
	 */
	public Map<String, String> getMap() {
		return map;
	}

	/**
	 * Sets the mappings for this function.
	 * @param map
	 */
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
