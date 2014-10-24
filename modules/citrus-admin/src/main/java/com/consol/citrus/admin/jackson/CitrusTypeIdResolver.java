/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.jackson;

import com.consol.citrus.model.testcase.core.Action;
import com.consol.citrus.model.testcase.core.ObjectFactory;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Special type id resolver for Citrus test actions. Implementation ensures proper json type resolving for test action
 * classes.
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class CitrusTypeIdResolver implements TypeIdResolver {

    /** List of known id to type mappings */
    private Map<String, JavaType> typeMappings = new HashMap<String, JavaType>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusTypeIdResolver.class);

    @Override
    public void init(JavaType baseType) {
        try {
            List<Class<?>> types = findTestActionTypes(ObjectFactory.class.getPackage().getName());
            for (Class<?> type : types) {
                log.info(String.format("Adding type mapping %s:%s", getTestActionTypeId(type), type.getName()));
                typeMappings.put(getTestActionTypeId(type), TypeFactory.type(type.getClass()));
            }

        } catch (IOException e) {
            log.warn("Unable to dynamically construct known test action types for type name id resolving", e);
        } catch (ClassNotFoundException e) {
            log.warn("Unable to dynamically construct known test action types for type name id resolving", e);
        }
    }

    @Override
    public String idFromValue(Object value) {
        String typeId = getTestActionTypeId(value.getClass());
        log.info(String.format("Resolved '%s' typeId for class: %s", typeId, value.getClass()));
        return typeId;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public JavaType typeFromId(String id) {
        if (typeMappings.containsKey(id)) {
            JavaType type = typeMappings.get(id);
            log.info(String.format("Resolved '%s' class for typeId: %s", type , id));
            return type;
        }

        log.info(String.format("Resolved '%s' class for typeId: %s", Action.class , id));
        return TypeFactory.type(Action.class);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    /**
     * Finds all JaxB generated test action classes in object factory package.
     * @param basePackage
     * @return
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private List<Class<?>> findTestActionTypes(String basePackage) throws IOException, ClassNotFoundException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        List<Class<?>> candidates = new ArrayList<Class<?>>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + "/" + "**/*.class";
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (isCandidate(metadataReader)) {
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        }
        return candidates;
    }

    /**
     * Resolve package name to resource path with support
     * of property placeholders.
     * @param basePackage
     * @return
     */
    private String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * Check if class type is JaxB annotated XmlRootElement.
     * @param metadataReader
     * @return
     * @throws ClassNotFoundException
     */
    private boolean isCandidate(MetadataReader metadataReader) {
        try {
            Class type = Class.forName(metadataReader.getClassMetadata().getClassName());
            if (type.getAnnotation(XmlRootElement.class) != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            log.warn("Unable to get class metadata information", e);
        }

        return false;
    }

    /**
     * Reads XmlRootElement annotation name field from class - if annotation is
     * not present return class name.
     * @param type
     * @return
     */
    private String getTestActionTypeId(Class<?> type) {
        XmlRootElement beanTypeAnnotation = type.getAnnotation(XmlRootElement.class);
        if (beanTypeAnnotation != null) {
            return beanTypeAnnotation.name();
        }

        return "";
    }
}
