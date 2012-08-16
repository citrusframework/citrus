/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl;

import org.springframework.core.io.Resource;

import com.consol.citrus.actions.TransformAction;

/**
 * Action transforms a XML document(specified inline or from external file resource)
 * with a XSLT document(specified inline or from external file resource)
 * and puts the result in the specified variable.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 */
public class TransformActionDefinition extends AbstractActionDefinition<TransformAction> {
	
	public TransformActionDefinition(TransformAction action) {
		super(action);
	}

	/**
	 * Set the target variable for the result
	 * @param variable
	 */
	public TransformActionDefinition result(String variable) {
		action.setTargetVariable(variable);
		return this;
	}
	
	/**
	 * Set the XML document
	 * @param xmlData the xmlData to set
	 */
	public TransformActionDefinition source(String xmlData) {
		action.setXmlData(xmlData);
		return this;
	}
	
	/**
	 * Set the XML document as resource
	 * @param xmlResource the xmlResource to set
	 */
	public TransformActionDefinition source(Resource xmlResource) {
		action.setXmlResource(xmlResource);
		return this;
	}
	
	/**
	 * Set the XSLT document
	 * @param xsltData the xsltData to set
	 */
	public TransformActionDefinition xslt(String xsltData) {
		action.setXsltData(xsltData);
		return this;
	}
	
	/**
	 * Set the XSLT document as resource
	 * @param xsltResource the xsltResource to set
	 */
	public TransformActionDefinition xslt(Resource xsltResource) {
		action.setXsltResource(xsltResource);
		return this;
	}
}
