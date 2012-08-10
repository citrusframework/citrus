package com.consol.citrus.dsl;

import org.springframework.core.io.Resource;

import com.consol.citrus.script.GroovyAction;

public class GroovyActionDefinition extends AbstractActionDefinition<GroovyAction> {

	public GroovyActionDefinition(GroovyAction action) {
	    super(action);
    }
	
	public GroovyActionDefinition script(String script) {
		action.setScript(script);
		return this;
	}

	public GroovyActionDefinition fileResource(Resource fileResource) {
		action.setFileResource(fileResource);
		return this;
	}
	
	public GroovyActionDefinition scriptTemplateResource(Resource scriptTemplate) {
		action.setScriptTemplateResource(scriptTemplate);
		return this;
	}
	
	public GroovyActionDefinition useScriptTemplate(boolean useScriptTemplate) {
		action.setUseScriptTemplate(useScriptTemplate);
		return this;
	}
}
