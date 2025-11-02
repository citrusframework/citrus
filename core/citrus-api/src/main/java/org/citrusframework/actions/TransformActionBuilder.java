/*
 * Copyright the original author or authors.
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

import java.nio.charset.Charset;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.Resource;

public interface TransformActionBuilder<T extends TestAction>
        extends ActionBuilder<T, TransformActionBuilder<T>>, TestActionBuilder<T> {

    /**
     * Set the target variable for the result
     */
    TransformActionBuilder<T> result(String variable);

    /**
     * Set the target variable for the result
     */
    TransformActionBuilder<T> variable(String variable);

    /**
     * Set the XML document
     * @param xmlData the xmlData to set
     */
    TransformActionBuilder<T> source(String xmlData);

    /**
     * Set the XML document as resource
     * @param xmlResource the xmlResource to set
     */
    TransformActionBuilder<T> source(Resource xmlResource);

    /**
     * Set the XML document as resource file path
     * @param xmlResourcePath the xmlResource to set
     */
    TransformActionBuilder<T> sourceFile(String xmlResourcePath);

    /**
     * Set the XML document as resource file path
     * @param xmlResourcePath the xmlResource to set
     * @param charset
     */
    TransformActionBuilder<T> sourceFile(String xmlResourcePath, String charset);

    /**
     * Set the XML document as resource
     * @param xmlResource the xmlResource to set
     * @param charset
     */
    TransformActionBuilder<T> source(Resource xmlResource, Charset charset);

    /**
     * Set the XSLT document
     * @param xsltData the xsltData to set
     */
    TransformActionBuilder<T> xslt(String xsltData);

    /**
     * Set the XSLT document as resource
     * @param xsltResource the xsltResource to set
     */
    TransformActionBuilder<T> xslt(Resource xsltResource);

    /**
     * Set the XML document as resource file path
     * @param xsltResourcePath the xmlResource to set
     */
    TransformActionBuilder<T> xsltFile(String xsltResourcePath);

    /**
     * Set the XML document as resource file path
     * @param xsltResourcePath the xmlResource to set
     * @param charset
     */
    TransformActionBuilder<T> xsltFile(String xsltResourcePath, String charset);

    /**
     * Set the XSLT document as resource
     * @param xsltResource the xsltResource to set
     * @param charset
     */
    TransformActionBuilder<T> xslt(Resource xsltResource, Charset charset);

    interface BuilderFactory {

        TransformActionBuilder<?> transform();

    }

}
