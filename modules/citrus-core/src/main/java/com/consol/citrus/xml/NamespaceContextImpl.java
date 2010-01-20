/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

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
