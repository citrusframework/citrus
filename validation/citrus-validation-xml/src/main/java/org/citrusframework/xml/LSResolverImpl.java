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

package org.citrusframework.xml;

import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Very basic LSResolver implementation for resolving dtd resources by their systemId.
 *
 * @author Christoph Deppisch
 */
public class LSResolverImpl implements LSResourceResolver {
    /** DOM implementation */
    private DOMImplementationLS domImpl;

    /**
     * Constructor
     * @param domImpl
     */
    public LSResolverImpl(DOMImplementationLS domImpl) {
        this.domImpl = domImpl;
    }

    /**
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        LSInput input = domImpl.createLSInput();
        Resource resource = Resources.fromClasspath(systemId);
        if (resource.getInputStream() == null || !resource.exists()) {
            return null;
        }

        input.setByteStream(resource.getInputStream());
        return input;
    }
}
