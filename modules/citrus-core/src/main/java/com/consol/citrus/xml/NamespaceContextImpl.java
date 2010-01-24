/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.xml;

import java.util.*;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;

/**
 * Namespace context implementation.
 * 
 * @author Christoph Deppisch
 */
public class NamespaceContextImpl implements NamespaceContext {

    /** Map holding namespace elements */
    private Map<String, String> namespaces;
    
    /**
     * Default constructor
     */
    public NamespaceContextImpl() {
        namespaces = new HashMap<String, String>();
    }
    
    /**
     * Default constructor using fields.
     * @param namespaces
     */
    public NamespaceContextImpl(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        if(prefix != null && prefix.length() == 0 && namespaces.containsKey("xmlns")) {
            return namespaces.get("xmlns");
        }else if(namespaces.containsKey("xmlns:" + prefix)) {
            return namespaces.get("xmlns:" + prefix);
        } else {
            return namespaces.get(prefix);
        }
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
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

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
	public Iterator<String> getPrefixes(String namespaceURI) {
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
     * Set the namespaces.
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the namespaces.
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

}
