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

import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Action transforms a XML document(specified inline or from external file resource)
 * with a XSLT document(specified inline or from external file resource)
 * and puts the result in the specified variable.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class TransformActionBuilder extends AbstractTestActionBuilder<TransformAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public TransformActionBuilder(TransformAction action) {
		super(action);
	}

	/**
	 * Default constructor.
	 */
	public TransformActionBuilder() {
		super(new TransformAction());
	}

	/**
	 * Set the target variable for the result
	 * @param variable
	 */
	public TransformActionBuilder result(String variable) {
		action.setTargetVariable(variable);
		return this;
	}
	
	/**
	 * Set the XML document
	 * @param xmlData the xmlData to set
	 */
	public TransformActionBuilder source(String xmlData) {
		action.setXmlData(xmlData);
		return this;
	}
	
	/**
	 * Set the XML document as resource
	 * @param xmlResource the xmlResource to set
	 */
	public TransformActionBuilder source(Resource xmlResource) {
	    return source(xmlResource, FileUtils.getDefaultCharset());
	}

	/**
	 * Set the XML document as resource
	 * @param xmlResource the xmlResource to set
	 * @param charset
	 */
	public TransformActionBuilder source(Resource xmlResource, Charset charset) {
	    try {
	        action.setXmlData(FileUtils.readToString(xmlResource, charset));
	    } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read xml resource", e);
        }
		return this;
	}
	
	/**
	 * Set the XSLT document
	 * @param xsltData the xsltData to set
	 */
	public TransformActionBuilder xslt(String xsltData) {
		action.setXsltData(xsltData);
		return this;
	}
	
	/**
	 * Set the XSLT document as resource
	 * @param xsltResource the xsltResource to set
	 */
	public TransformActionBuilder xslt(Resource xsltResource) {
	    return xslt(xsltResource, FileUtils.getDefaultCharset());
	}

	/**
	 * Set the XSLT document as resource
	 * @param xsltResource the xsltResource to set
	 * @param charset
	 */
	public TransformActionBuilder xslt(Resource xsltResource, Charset charset) {
	    try {
	        action.setXsltData(FileUtils.readToString(xsltResource, charset));
	    } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read xstl resource", e);
        }

		return this;
	}
}
