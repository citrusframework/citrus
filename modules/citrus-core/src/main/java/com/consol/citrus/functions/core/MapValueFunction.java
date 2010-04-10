package com.consol.citrus.functions.core;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

/**
 * Function to map the function's argument to a corresponding value configured using a map. 
 * <p>Example of the function definition and its usage:</p>
 * 
 * <code>
 * <pre>
 * &lt;bean id="myCustomFunctionLibrary" class="com.consol.citrus.functions.FunctionLibrary"&gt;
 *  &lt;property name="name" value="myCustomFunctionLibrary" /&gt;
 *  &lt;property name="prefix" value="custom:" /&gt;
 *  &lt;property name="members"&gt;
 *    &lt;map&gt;
 *      &lt;entry key="mapHttpStatusCodeToMessage"&gt;
 *        &lt;bean class="com.consol.citrus.functions.core.MapValueFunction"&gt;
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
public class MapValueFunction implements Function, InitializingBean {
	private Map<String, String> map = null;
	
	/**
	 * @see Function#execute(List)
	 */
	public String execute(List<String> params) {
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

	public void afterPropertiesSet() {
		Assert.notEmpty(map);
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
