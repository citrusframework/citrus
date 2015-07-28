/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.container.Template;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Builder defines call template action with global context and parameters.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class TemplateBuilder extends AbstractTestActionBuilder<Template> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public TemplateBuilder(Template action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public TemplateBuilder() {
		super(new Template());
	}

	/**
	 * Sets the template name.
	 * @param name
	 * @return
	 */
	public TemplateBuilder name(String name) {
		action.setName(name);
		return this;
	}

    /**
     * Loads template bean from Spring bean application context and sets attributes.
     * @param applicationContext
     * @return
     */
    public TemplateBuilder load(ApplicationContext applicationContext) {
        Template rootTemplate = applicationContext.getBean(action.getName(), Template.class);

        action.setGlobalContext(rootTemplate.isGlobalContext());
		action.setActor(rootTemplate.getActor());
		action.setActions(rootTemplate.getActions());
		action.setParameter(rootTemplate.getParameter());

        return this;
    }
	
	/**
     * Boolean flag marking the template variables should also affect
     * variables in test case.
     * @param globalContext the globalContext to set
     */
	public TemplateBuilder globalContext(boolean globalContext) {
		action.setGlobalContext(globalContext);
		return this;
	}

	/**
     * Set parameter before execution.
     * @param parameters the parameter to set
     */
	public TemplateBuilder parameters(Map<String, String> parameters) {
		action.getParameter().putAll(parameters);
		return this;
	}
	
	/**
     * Set parameter before execution.
     * @param name
     * @param value
     */
	public TemplateBuilder parameter(String name, String value) {
	    action.getParameter().put(name, value);
		return this;
	}
}
