package com.consol.citrus.dsl;

import java.util.Collections;
import java.util.Map;

import com.consol.citrus.container.Template;

/**
 * Class represents a previously defined block of test actions. Test cases can call
 * templates and reuse its functionality.
 * 
 * Templates operate on test variables. While calling the template caller can set these
 * variables as parameters.
 * 
 * Nested test actions are executes in sequence.
 * 
 * The template execution may affect existing variable values in the calling test case. So
 * variables may have different values in the test case after template execution. Therefore
 * user can create a local test context by setting globalContext to false. Template then will 
 * have no affect on the variables used in the test case.
 */
public class TemplateDefinition extends AbstractActionDefinition<Template> {

	public TemplateDefinition(Template action) {
	    super(action);
    }
	
	/**
     * Boolean flag marking the template variables should also affect
     * variables in test case.
     * @param globalContext the globalContext to set
     */
	public TemplateDefinition globalContext(boolean globalContext) {
		action.setGlobalContext(globalContext);
		return this;
	}

	/**
     * Set parameter before execution.
     * @param parameter the parameter to set
     */
	public TemplateDefinition parameters(Map<String, String> parameters) {
		action.setParameter(parameters);
		return this;
	}
	
	/**
     * Set parameter before execution.
     * @param parameter the parameter to set with key and value
     */
	public TemplateDefinition parameters(String key, String value) {
		return parameters(Collections.singletonMap(key, value));
	}
}
