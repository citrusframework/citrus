package com.consol.citrus.xml;

import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImpl implements NamespaceContext {

    private Map<String, String> namespaces;
    
    public NamespaceContextImpl() {
        namespaces = new HashMap<String, String>();
    }
    
    public NamespaceContextImpl(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public String getNamespaceURI(String prefix) {
        if(prefix != null && prefix.length() == 0 && namespaces.containsKey("xmlns")) {
            return namespaces.get("xmlns");
        }else if(namespaces.containsKey("xmlns:" + prefix)) {
            return namespaces.get("xmlns:" + prefix);
        } else {
            return namespaces.get(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        if(namespaces.containsValue(namespaceURI)) {
            for (Entry<String, String> entry : namespaces.entrySet()) {
                if(entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        List<String> prefixes = new ArrayList<String>();
        
        if(namespaces.containsValue(namespaceURI)) {
            for (Entry<String, String> entry : namespaces.entrySet()) {
                if(entry.getValue().equals(namespaceURI)) {
                    prefixes.add(entry.getKey());
                }
            }
        }
        
        return prefixes.iterator();
    }

    /**
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

}
