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

package org.citrusframework.zookeeper.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.zookeeper.actions.ZooExecuteAction;
import org.citrusframework.zookeeper.client.ZooClient;

@XmlRootElement(name = "zookeeper")
public class Zookeeper implements TestActionBuilder<ZooExecuteAction>, ReferenceResolverAware {

    private Info info;
    private Create create;
    private Delete delete;
    private Exists exists;
    private Get get;
    private Set set;
    private Children children;

    private String description;
    private String client;
    private String result;
    private List<Validation> validate = new ArrayList<>();
    private List<Extraction> extract = new ArrayList<>();
    private ReferenceResolver referenceResolver;

    @XmlElement
    public void setDescription(String description) {
        this.description = description;
    }

    @XmlAttribute
    public void setClient(String client) {
        this.client = client;
    }

    @XmlAttribute
    public void setResult(String result) {
        this.result = result;
    }

    @XmlElement(name = "validate")
    public void setValidate(List<Validation> validate) {
        this.validate = validate;
    }

    public List<Validation> getValidate() {
        return validate;
    }

    @XmlElement(name = "extract")
    public void setExtract(List<Extraction> extract) {
        this.extract = extract;
    }

    public List<Extraction> getExtract() {
        return extract;
    }

    @XmlElement
    public void setInfo(Info info) {
        this.info = info;
    }

    @XmlElement
    public void setCreate(Create create) {
        this.create = create;
    }

    @XmlElement
    public void setDelete(Delete delete) {
        this.delete = delete;
    }

    @XmlElement
    public void setExists(Exists exists) {
        this.exists = exists;
    }

    @XmlElement(name = "get")
    public void setGet(Get get) {
        this.get = get;
    }

    @XmlElement(name = "set")
    public void setSet(Set set) {
        this.set = set;
    }

    @XmlElement(name = "children")
    public void setChildren(Children children) {
        this.children = children;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public ZooExecuteAction build() {
        int commandCount = 0;
        if (info != null) {
            commandCount++;
        }
        if (create != null) {
            commandCount++;
        }
        if (delete != null) {
            commandCount++;
        }
        if (exists != null) {
            commandCount++;
        }
        if (get != null) {
            commandCount++;
        }
        if (set != null) {
            commandCount++;
        }
        if (children != null) {
            commandCount++;
        }

        if (commandCount == 0) {
            throw new CitrusRuntimeException("Missing Zookeeper command - please provide proper command details");
        }

        if (commandCount > 1) {
            throw new CitrusRuntimeException("Ambiguous Zookeeper command - please provide only one command");
        }

        ZooExecuteAction.Builder delegate = new ZooExecuteAction.Builder();

        if (info != null) {
            delegate.command(info.getCommand());
        }

        if (create != null) {
            delegate.command(create.getCommand());
        }

        if (delete != null) {
            delegate.command(delete.getCommand());
        }

        if (exists != null) {
            delegate.command(exists.getCommand());
        }

        if (get != null) {
            delegate.command(get.getCommand());
        }

        if (set != null) {
            delegate.command(set.getCommand());
        }

        if (children != null) {
            delegate.command(children.getCommand());
        }

        if (StringUtils.hasText(description)) {
            delegate.description(description);
        }

        if (StringUtils.hasText(result)) {
            delegate.result(result);
        }

        if (validate != null) {
            for (Validation validation : validate) {
                if (!StringUtils.hasText(validation.getPath())) {
                    throw new CitrusRuntimeException(
                            "Missing path on zookeeper validate - please provide proper validate details");
                }
                delegate.validate(validation.getPath(), validation.getValue());
            }
        }

        if (extract != null && !extract.isEmpty()) {
            Map<String, Object> expressions = new LinkedHashMap<>();
            for (Extraction extraction : extract) {
                if (!StringUtils.hasText(extraction.getVariable())) {
                    throw new CitrusRuntimeException(
                            "Missing variable name on zookeeper extract - please provide proper extract details");
                }
                expressions.put(extraction.getPath(), extraction.getVariable());
            }
            delegate.extract(DelegatingPayloadVariableExtractor.Builder.fromBody().expressions(expressions).build());
        }

        if (StringUtils.hasText(client) && referenceResolver != null) {
            delegate.client(referenceResolver.resolve(client, ZooClient.class));
        } else {
            delegate.client(new ZooClient());
        }

        return delegate.build();
    }

    public static class Validation {
        private String path;
        private String value;

        @XmlAttribute(required = true)
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @XmlAttribute
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Extraction {
        private String path;
        private String variable;

        @XmlAttribute(required = true)
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @XmlAttribute(required = true)
        public String getVariable() {
            return variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }
    }
}
