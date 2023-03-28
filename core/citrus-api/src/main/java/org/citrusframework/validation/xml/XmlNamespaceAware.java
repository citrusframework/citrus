package org.citrusframework.validation.xml;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public interface XmlNamespaceAware {

    /**
     * Sets the Xml namespaces as map where the key is the namespace prefix and value is the namespace URI.
     * @param namespaces
     */
    void setNamespaces(Map<String, String> namespaces);
}
