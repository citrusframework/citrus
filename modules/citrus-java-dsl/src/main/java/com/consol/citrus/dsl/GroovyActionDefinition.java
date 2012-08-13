package com.consol.citrus.dsl;

import org.springframework.core.io.Resource;

import com.consol.citrus.script.GroovyAction;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 */
public class GroovyActionDefinition extends AbstractActionDefinition<GroovyAction> {

	public GroovyActionDefinition(GroovyAction action) {
	    super(action);
    }
	
	/**
     * Set the groovy script code.
     * @param script the script to set
     */
	public GroovyActionDefinition script(String script) {
		action.setScript(script);
		return this;
	}

	/**
     * Set file resource.
     * @param fileResource the fileResource to set
     */
	public GroovyActionDefinition fileResource(Resource fileResource) {
		action.setFileResource(fileResource);
		return this;
	}
	
	/**
     * Set the script template resource.
     * @param scriptTemplate the scriptTemplate to set
     */
	public GroovyActionDefinition scriptTemplateResource(Resource scriptTemplate) {
		action.setScriptTemplateResource(scriptTemplate);
		return this;
	}
	
	/**
     * Prevent script template usage if false.
     * @param useScriptTemplate the useScriptTemplate to set
     */
	public GroovyActionDefinition useScriptTemplate(boolean useScriptTemplate) {
		action.setUseScriptTemplate(useScriptTemplate);
		return this;
	}
}
