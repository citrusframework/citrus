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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Action transforms a XML document(specified inline or from external file resource)
 * with a XSLT document(specified inline or from external file resource)
 * and puts the result in the specified variable.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.TransformActionBuilder}
 */
public class TransformActionDefinition extends AbstractActionDefinition<TransformAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public TransformActionDefinition(TransformAction action) {
		super(action);
	}

	/**
	 * Default constructor.
	 */
	public TransformActionDefinition() {
		super(new TransformAction());
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
	    try {
	        action.setXmlData(FileUtils.readToString(xmlResource));
	    } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read xml resource", e);
        }
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
	    try {
	        action.setXsltData(FileUtils.readToString(xsltResource));
	    } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read xstl resource", e);
        }

		return this;
	}
}
