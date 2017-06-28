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

package com.consol.citrus.actions;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.*;
import java.io.IOException;
import java.nio.charset.Charset;


/**
 * Action transforms a XML document(specified inline or from external file resource)
 * with a XSLT document(specified inline or from external file resource)
 * and puts the result in the specified variable.
 *
 * @author Philipp Komninos
 * @since 2010
 */
public class TransformAction extends AbstractTestAction {

    /** Inline XML document */
    private String xmlData;

    /** External XML document resource path */
    private String xmlResourcePath;

    /** Charset applied to xml resource */
    private String xmlResourceCharset = Citrus.CITRUS_FILE_ENCODING;

    /** Inline XSLT document */
    private String xsltData;

    /** External XSLT document resource path */
    private String xsltResourcePath;

    /** Charset applied to xslt resource */
    private String xsltResourceCharset = Citrus.CITRUS_FILE_ENCODING;

    /** Target variable for the result */
    private String targetVariable = "transform-result";

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TransformAction.class);

    /**
     * Default constructor.
     */
    public TransformAction() {
        setName("transform");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting XSLT transformation");
            }

            //parse XML document and define XML source for transformation
            Source xmlSource = null;
            if (xmlResourcePath != null) {
                xmlSource = new StringSource(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(xmlResourcePath, context),
                        Charset.forName(context.replaceDynamicContentInString(xmlResourceCharset)))));
            } else if (xmlData != null) {
                xmlSource = new StringSource(context.replaceDynamicContentInString(xmlData));
            } else {
                throw new CitrusRuntimeException("Neither inline XML nor " +
                        "external file resource is defined for bean. " +
                        "Cannot transform XML document.");
            }

            //parse XSLT document and define  XSLT source for transformation
            Source xsltSource = null;
            if (xsltResourcePath != null) {
                xsltSource = new StringSource(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(xsltResourcePath, context),
                        Charset.forName(context.replaceDynamicContentInString(xsltResourceCharset)))));
            } else if (xsltData != null) {
                xsltSource = new StringSource(context.replaceDynamicContentInString(xsltData));
            } else {
                throw new CitrusRuntimeException("Neither inline XSLT nor " +
                        "external file resource is defined for bean. " +
                        "Cannot transform XSLT document.");
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(xsltSource);

            StringResult result = new StringResult();
            transformer.transform(xmlSource, result);

            context.setVariable(targetVariable, result.toString());
            log.info("Finished XSLT transformation");
        } catch (IOException | TransformerException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the XML document
     * @param xmlData the xmlData to set
     */
    public TransformAction setXmlData(String xmlData) {
        this.xmlData = xmlData;
        return this;
    }

    /**
     * Set the XML document as resource
     * @param xmlResource the xmlResource to set
     */
    public TransformAction setXmlResourcePath(String xmlResource) {
        this.xmlResourcePath = xmlResource;
        return this;
    }

    /**
     * Set the XSLT document
     * @param xsltData the xsltData to set
     */
    public TransformAction setXsltData(String xsltData) {
        this.xsltData = xsltData;
        return this;
    }

    /**
     * Set the XSLT document as resource
     * @param xsltResource the xsltResource to set
     */
    public TransformAction setXsltResourcePath(String xsltResource) {
        this.xsltResourcePath = xsltResource;
        return this;
    }

    /**
     * Set the target variable for the result
     * @param targetVariable the targetVariable to set
     */
    public TransformAction setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
        return this;
    }

    /**
     * Gets the xmlData.
     * @return the xmlData
     */
    public String getXmlData() {
        return xmlData;
    }

    /**
     * Gets the xmlResource.
     * @return the xmlResource
     */
    public String getXmlResourcePath() {
        return xmlResourcePath;
    }

    /**
     * Gets the xsltData.
     * @return the xsltData
     */
    public String getXsltData() {
        return xsltData;
    }

    /**
     * Gets the xsltResource.
     * @return the xsltResource
     */
    public String getXsltResourcePath() {
        return xsltResourcePath;
    }

    /**
     * Gets the targetVariable.
     * @return the targetVariable
     */
    public String getTargetVariable() {
        return targetVariable;
    }

}
