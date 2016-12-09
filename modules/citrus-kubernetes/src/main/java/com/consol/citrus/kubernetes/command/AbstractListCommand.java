/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.kubernetes.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public abstract class AbstractListCommand<R> extends AbstractKubernetesCommand<R> {

    protected static final String LABEL = "label";

    /** Logger */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractListCommand(String name) {
        super(String.format("kubernetes:%s:list", name));
    }

    @Override
    public void execute(KubernetesClient kubernetesClient, TestContext context) {
        ClientMixedOperation operation = listOperation(kubernetesClient, context);

        if (hasParameter(LABEL)) {
            operation.withLabels(getLabels(getParameters().get(LABEL).toString(), context));
            operation.withoutLabels(getWithoutLabels(getParameters().get(LABEL).toString(), context));
        }

        setCommandResult((R) operation.list());

        if (getCommandResult() != null) {
            log.debug(getCommandResult().toString());
        }
    }

    /**
     * Subclasses provide operation to call.
     * @param kubernetesClient
     * @param context
     * @return
     */
    protected abstract ClientMixedOperation listOperation(KubernetesClient kubernetesClient, TestContext context);

    /**
     * Reads labels from expression string.
     * @param labelExpression
     * @param context
     * @return
     */
    protected Map<String, String> getLabels(String labelExpression, TestContext context) {
        Map<String, String> labels = new HashMap<>();

        Set<String> values = StringUtils.commaDelimitedListToSet(labelExpression);
        for (String item : values) {
            if (item.contains("!=")) {
                continue;
            } else if (item.contains("=")) {
                labels.put(context.replaceDynamicContentInString(item.substring(0, item.indexOf("="))), context.replaceDynamicContentInString(item.substring(item.indexOf("=") + 1)));
            } else if (!item.startsWith("!")) {
                labels.put(context.replaceDynamicContentInString(item), null);
            }
        }

        return labels;
    }

    /**
     * Reads without labels from expression string.
     * @param labelExpression
     * @param context
     * @return
     */
    protected Map<String, String> getWithoutLabels(String labelExpression, TestContext context) {
        Map<String, String> labels = new HashMap<>();

        Set<String> values = StringUtils.commaDelimitedListToSet(labelExpression);
        for (String item : values) {
            if (item.contains("!=")) {
                labels.put(context.replaceDynamicContentInString(item.substring(0, item.indexOf("!="))), context.replaceDynamicContentInString(item.substring(item.indexOf("!=") + 2)));
            } else if (item.startsWith("!")) {
                labels.put(context.replaceDynamicContentInString(item.substring(1)), null);
            }
        }

        return labels;
    }

    /**
     * Sets the pod label parameter.
     * @param key
     * @param value
     * @return
     */
    public AbstractListCommand label(String key, String value) {
        if (!hasParameter(LABEL)) {
            getParameters().put(LABEL, key + "=" + value);
        } else {
            getParameters().put(LABEL, getParameters().get(LABEL) + "," + key + "=" + value);
        }
        return this;
    }

    /**
     * Sets the pod label parameter.
     * @param key
     * @return
     */
    public AbstractListCommand label(String key) {
        if (!hasParameter(LABEL)) {
            getParameters().put(LABEL, key);
        } else {
            getParameters().put(LABEL, getParameters().get(LABEL) + "," + key);
        }
        return this;
    }

    /**
     * Sets the without pod label parameter.
     * @param key
     * @param value
     * @return
     */
    public AbstractListCommand withoutLabel(String key, String value) {
        if (!hasParameter(LABEL)) {
            getParameters().put(LABEL, key + "!=" + value);
        } else {
            getParameters().put(LABEL, getParameters().get(LABEL) + "," + key + "!=" + value);
        }
        return this;
    }

    /**
     * Sets the without pod label parameter.
     * @param key
     * @return
     */
    public AbstractListCommand withoutLabel(String key) {
        if (!hasParameter(LABEL)) {
            getParameters().put(LABEL, "!" + key);
        } else {
            getParameters().put(LABEL, getParameters().get(LABEL) + ",!" + key);
        }
        return this;
    }

}
