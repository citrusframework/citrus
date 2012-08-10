package com.consol.citrus.dsl;

import java.util.Collections;
import java.util.Map;

import com.consol.citrus.container.Template;

public class TemplateDefinition extends AbstractActionDefinition<Template> {

	public TemplateDefinition(Template action) {
	    super(action);
    }
	
	public TemplateDefinition globalContext(boolean globalContext) {
		action.setGlobalContext(globalContext);
		return this;
	}

	public TemplateDefinition parameters(Map<String, String> parameters) {
		action.setParameter(parameters);
		return this;
	}
	
	public TemplateDefinition parameters(String key, String value) {
		return parameters(Collections.singletonMap(key, value));
	}
}
