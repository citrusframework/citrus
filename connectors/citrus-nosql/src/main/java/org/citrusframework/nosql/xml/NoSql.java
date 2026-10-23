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

package org.citrusframework.nosql.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.nosql.actions.NoSqlAction;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * XML test action representing a NoSQL operation.
 *
 * <pre>{@code
 * <nosql client="ordersDatabase" operation="query" target="orders" max-results="10">
 *   <filter>{ "orderId": "${orderId}" }</filter>
 *   <validate path="$.documents[0].status" value="COMPLETED"/>
 *   <extract path="$.documents[0].customerId" variable="customerId"/>
 * </nosql>
 * }</pre>
 */
@XmlRootElement(name = "nosql")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"description", "filter", "payload", "validations", "extractions"})
public class NoSql implements TestActionBuilder<NoSqlAction>, ReferenceResolverAware {

    @XmlAttribute(required = true)
    private String client;

    @XmlAttribute(required = true)
    private String operation;

    @XmlAttribute(required = true)
    private String target;

    @XmlAttribute
    private String command;

    @XmlAttribute(name = "max-results")
    private Integer maxResults;

    @XmlAttribute(name = "retry-attempts")
    private Integer retryAttempts;

    @XmlAttribute(name = "retry-interval")
    private String retryInterval;

    @XmlElement
    private String description;

    @XmlElement
    private String filter;

    @XmlElement
    private String payload;

    @XmlElement(name = "validate")
    private List<Validate> validations = new ArrayList<>();

    @XmlElement(name = "extract")
    private List<Extract> extractions = new ArrayList<>();

    @XmlTransient
    private ReferenceResolver referenceResolver;

    @Override
    public NoSqlAction build() {
        if (filter != null && payload != null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL action must use either a filter or a payload, not both");
        }

        NoSqlAction.Builder builder = new NoSqlAction.Builder()
                .client(client)
                .target(target)
                .operation(operation, command);

        if (description != null) {
            builder.description(description);
        }

        String content = payload != null ? payload : filter;
        if (content != null) {
            builder.payload(content);
        }

        validations.forEach(validation -> builder.validate(validation.path, validation.value));
        extractions.forEach(extraction -> builder.extract(extraction.path, extraction.variable));

        if (maxResults != null) {
            builder.maxResults(maxResults);
        }

        if (retryAttempts != null) {
            builder.retry(retryAttempts, retryInterval);
        } else if (retryInterval != null && !retryInterval.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL retry interval requires retry attempts");
        }

        if (referenceResolver != null) {
            builder.withReferenceResolver(referenceResolver);
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public String getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(String retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public List<Validate> getValidations() {
        return validations;
    }

    public void setValidations(List<Validate> validations) {
        this.validations = validations;
    }

    public List<Extract> getExtractions() {
        return extractions;
    }

    public void setExtractions(List<Extract> extractions) {
        this.extractions = extractions;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Validate {

        @XmlAttribute(required = true)
        private String path;

        @XmlAttribute(required = true)
        private String value;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Extract {

        @XmlAttribute(required = true)
        private String path;

        @XmlAttribute(required = true)
        private String variable;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getVariable() {
            return variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }
    }
}
