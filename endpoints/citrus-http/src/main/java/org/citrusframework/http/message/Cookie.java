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

package org.citrusframework.http.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;

/**
 * Http cookie model used in YAML/XML DSLs. Converts to {@link jakarta.servlet.http.Cookie}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class Cookie {

    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected String value;
    @XmlAttribute(name = "max-age")
    protected Integer maxAge;
    @XmlAttribute
    protected String path;
    @XmlAttribute
    protected String domain;
    @XmlAttribute
    protected Boolean secure;
    @XmlAttribute(name = "http-only")
    protected Boolean httpOnly;

    public jakarta.servlet.http.Cookie toCookie() {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
        if (path != null) {
            cookie.setPath(path);
        }
        if (domain != null) {
            cookie.setDomain(domain);
        }
        if (maxAge != null) {
            cookie.setMaxAge(maxAge);
        }
        if (secure != null) {
            cookie.setSecure(secure);
        }
        if (httpOnly != null) {
            cookie.setHttpOnly(httpOnly);
        }
        return cookie;
    }

    public String getName() {
        return name;
    }

    @SchemaProperty(required = true, description = "Http cookie name.")
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @SchemaProperty(required = true, description = "Http cookie value.")
    public void setValue(String value) {
        this.value = value;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    @SchemaProperty(advanced = true, description = "Http cookie max age.")
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getPath() {
        return path;
    }

    @SchemaProperty(advanced = true, description = "Http cookie path.")
    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    @SchemaProperty(advanced = true, description = "Http cookie domain.")
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Boolean getSecure() {
        return secure;
    }

    @SchemaProperty(advanced = true, description = "Http cookie secure flag.")
    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    @SchemaProperty(advanced = true, description = "Http cookie httpOnly flag.")
    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
}
