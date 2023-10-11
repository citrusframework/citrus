/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jmx.config.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.jmx.model.ManagedBeanDefinition;
import org.citrusframework.jmx.model.ManagedBeanInvocation;
import org.citrusframework.jmx.model.OperationParam;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.jmx.server.JmxServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServerConfigParser implements AnnotationConfigParser<JmxServerConfig, JmxServer> {

    @Override
    public JmxServer parse(JmxServerConfig annotation, ReferenceResolver referenceResolver) {
        JmxServerBuilder builder = new JmxServerBuilder();

        builder.autoStart(annotation.autoStart());

        if (StringUtils.hasText(annotation.serverUrl())) {
            builder.serverUrl(annotation.serverUrl());
        }

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.protocol())) {
            builder.protocol(annotation.protocol());
        }

        if (StringUtils.hasText(annotation.binding())) {
            builder.binding(annotation.binding());
        }

        builder.createRegistry(annotation.createRegistry());

        if (StringUtils.hasText(annotation.environmentProperties())) {
            builder.environmentProperties(referenceResolver.resolve(annotation.environmentProperties(), Properties.class));
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), JmxMessageConverter.class));
        }

        builder.timeout(annotation.timeout());

        List<ManagedBeanDefinition> managedBeans = new ArrayList<>();
        MbeanConfig[] mbeanConfigs = annotation.mbeans();
        for (MbeanConfig mbeanConfig : mbeanConfigs) {
            ManagedBeanDefinition mbeanDefinition = new ManagedBeanDefinition();
            mbeanDefinition.setType(mbeanConfig.type());
            mbeanDefinition.setName(mbeanConfig.name());
            mbeanDefinition.setObjectDomain(mbeanConfig.objectDomain());
            mbeanDefinition.setObjectName(mbeanConfig.objectName());

            List<ManagedBeanInvocation.Operation> mbeanOperations = new ArrayList<>();
            MbeanOperation[] mbeanOperationConfigs = mbeanConfig.operations();
            for (MbeanOperation mbeanOperationConfig : mbeanOperationConfigs) {
                ManagedBeanInvocation.Operation op = new ManagedBeanInvocation.Operation();
                op.setName(mbeanOperationConfig.name());

                Class<?>[] parameter = mbeanOperationConfig.parameter();
                ManagedBeanInvocation.Parameter params = new ManagedBeanInvocation.Parameter();
                for (Class<?> paramType : parameter) {
                    OperationParam p = new OperationParam();
                    p.setType(paramType.getName());
                    params.getParameter().add(p);
                }

                if (params.getParameter() != null && !params.getParameter().isEmpty()) {
                    op.setParameter(params);
                }

                mbeanOperations.add(op);
            }
            mbeanDefinition.setOperations(mbeanOperations);

            List<ManagedBeanInvocation.Attribute> mbeanAttributes = new ArrayList<>();
            MbeanAttribute[] mbeanAttributeConfigs = mbeanConfig.attributes();
            for (MbeanAttribute mbeanAttributeConfig : mbeanAttributeConfigs) {
                ManagedBeanInvocation.Attribute att = new ManagedBeanInvocation.Attribute();
                att.setType(mbeanAttributeConfig.type().getName());
                att.setName(mbeanAttributeConfig.name());

                mbeanAttributes.add(att);
            }
            mbeanDefinition.setAttributes(mbeanAttributes);

            managedBeans.add(mbeanDefinition);
        }

        builder.mbeans(managedBeans);

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
