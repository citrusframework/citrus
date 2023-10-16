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

package org.citrusframework.actions;

import java.io.IOException;
import java.nio.charset.Charset;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private final String xmlData;

    /** External XML document resource path */
    private final String xmlResourcePath;

    /** Charset applied to xml resource */
    private final String xmlResourceCharset;

    /** Inline XSLT document */
    private final String xsltData;

    /** External XSLT document resource path */
    private final String xsltResourcePath;

    /** Charset applied to xslt resource */
    private final String xsltResourceCharset;

    /** Target variable for the result */
    private final String targetVariable;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TransformAction.class);

    /**
     * Default constructor.
     */
    public TransformAction(Builder builder) {
        super("transform", builder);

        this.xmlData = builder.xmlData;
        this.xmlResourcePath = builder.xmlResourcePath;
        this.xmlResourceCharset = builder.xmlResourceCharset;
        this.xsltData = builder.xsltData;
        this.xsltResourcePath = builder.xsltResourcePath;
        this.xsltResourceCharset = builder.xsltResourceCharset;
        this.targetVariable = builder.targetVariable;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting XSLT transformation");
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
            logger.info("Finished XSLT transformation");
        } catch (IOException | TransformerException e) {
            throw new CitrusRuntimeException(e);
        }
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

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<TransformAction, Builder> {

        private String xmlData;
        private String xmlResourcePath;
        private String xmlResourceCharset = CitrusSettings.CITRUS_FILE_ENCODING;
        private String xsltData;
        private String xsltResourcePath;
        private String xsltResourceCharset = CitrusSettings.CITRUS_FILE_ENCODING;
        private String targetVariable = "transform-result";

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder transform() {
            return new Builder();
        }

        /**
         * Set the target variable for the result
         * @param variable
         */
        public Builder result(String variable) {
            this.targetVariable = variable;
            return this;
        }

        /**
         * Set the target variable for the result
         * @param variable
         */
        public Builder variable(String variable) {
            this.targetVariable = variable;
            return this;
        }

        /**
         * Set the XML document
         * @param xmlData the xmlData to set
         */
        public Builder source(String xmlData) {
            this.xmlData = xmlData;
            return this;
        }

        /**
         * Set the XML document as resource
         * @param xmlResource the xmlResource to set
         */
        public Builder source(Resource xmlResource) {
            return source(xmlResource, FileUtils.getDefaultCharset());
        }

        /**
         * Set the XML document as resource file path
         * @param xmlResourcePath the xmlResource to set
         */
        public Builder sourceFile(String xmlResourcePath) {
            this.xmlResourcePath = xmlResourcePath;
            return this;
        }

        /**
         * Set the XML document as resource file path
         * @param xmlResourcePath the xmlResource to set
         * @param charset
         */
        public Builder sourceFile(String xmlResourcePath, String charset) {
            this.xmlResourcePath = xmlResourcePath;
            this.xmlResourceCharset = charset;
            return this;
        }

        /**
         * Set the XML document as resource
         * @param xmlResource the xmlResource to set
         * @param charset
         */
        public Builder source(Resource xmlResource, Charset charset) {
            try {
                this.xmlData = FileUtils.readToString(xmlResource, charset);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read xml resource", e);
            }
            return this;
        }

        /**
         * Set the XSLT document
         * @param xsltData the xsltData to set
         */
        public Builder xslt(String xsltData) {
            this.xsltData = xsltData;
            return this;
        }

        /**
         * Set the XSLT document as resource
         * @param xsltResource the xsltResource to set
         */
        public Builder xslt(Resource xsltResource) {
            return xslt(xsltResource, FileUtils.getDefaultCharset());
        }

        /**
         * Set the XML document as resource file path
         * @param xsltResourcePath the xmlResource to set
         */
        public Builder xsltFile(String xsltResourcePath) {
            this.xsltResourcePath = xsltResourcePath;
            return this;
        }

        /**
         * Set the XML document as resource file path
         * @param xsltResourcePath the xmlResource to set
         * @param charset
         */
        public Builder xsltFile(String xsltResourcePath, String charset) {
            this.xsltResourcePath = xsltResourcePath;
            this.xsltResourceCharset = charset;
            return this;
        }

        /**
         * Set the XSLT document as resource
         * @param xsltResource the xsltResource to set
         * @param charset
         */
        public Builder xslt(Resource xsltResource, Charset charset) {
            try {
                this.xsltData = FileUtils.readToString(xsltResource, charset);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read xstl resource", e);
            }

            return this;
        }

        @Override
        public TransformAction build() {
            return new TransformAction(this);
        }
    }
}
