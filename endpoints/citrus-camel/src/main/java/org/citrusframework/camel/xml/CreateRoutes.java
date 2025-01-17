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

package org.citrusframework.camel.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "routeSpec",
        "route"
})
public class CreateRoutes {

    @XmlAnyElement(lax = true)
    protected org.w3c.dom.Element routeSpec;

    @XmlElement
    protected RouteSpec route;

    public org.w3c.dom.Element getRouteSpec() {
        return routeSpec;
    }

    public void setRouteSpec(org.w3c.dom.Element routeSpec) {
        this.routeSpec = routeSpec;
    }

    public void setRoute(RouteSpec route) {
        this.route = route;
    }

    public RouteSpec getRoute() {
        return route;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
    })
    public static class RouteSpec {
        @XmlAttribute
        protected String id;

        @XmlAttribute
        protected String file;

        @XmlValue
        protected String route;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getFile() {
            return file;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public String getRoute() {
            return route;
        }
    }
}
