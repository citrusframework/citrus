package com.consol.citrus.admin.jaxb;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class CitrusNamespacePrefixMapper extends NamespacePrefixMapper {

    /** List of known namespaces with mapping to prefix */
    private Map<String, String> namespaceMappings = new HashMap<String, String>();

    public CitrusNamespacePrefixMapper() {
        namespaceMappings.put("http://www.citrusframework.org/schema/config", "citrus");
        namespaceMappings.put("http://www.citrusframework.org/schema/jms/config", "citrus-jms");
        namespaceMappings.put("http://www.citrusframework.org/schema/http/config", "citrus-http");
        namespaceMappings.put("http://www.citrusframework.org/schema/websocket/config", "citrus-websocket");
        namespaceMappings.put("http://www.citrusframework.org/schema/ws/config", "citrus-ws");
        namespaceMappings.put("http://www.citrusframework.org/schema/ssh/config", "citrus-ssh");
        namespaceMappings.put("http://www.citrusframework.org/schema/mail/config", "citrus-mail");
        namespaceMappings.put("http://www.citrusframework.org/schema/vertx/config", "citrus-vertx");
        namespaceMappings.put("http://www.citrusframework.org/schema/ftp/config", "citrus-ftp");
        namespaceMappings.put("http://www.citrusframework.org/schema/testcase", "citrus-test");
    }

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if (namespaceMappings.containsKey(namespaceUri)) {
            return namespaceMappings.get(namespaceUri);
        }

        return suggestion;
    }

    /**
     * Gets the namespace mappings.
     * @return
     */
    public Map<String, String> getNamespaceMappings() {
        return namespaceMappings;
    }

    /**
     * Sets the namespace mappings.
     * @param namespaceMappings
     */
    public void setNamespaceMappings(Map<String, String> namespaceMappings) {
        this.namespaceMappings = namespaceMappings;
    }
}
