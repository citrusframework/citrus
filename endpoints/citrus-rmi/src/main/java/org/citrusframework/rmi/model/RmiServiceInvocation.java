/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.rmi.model;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.TypeConverter;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "remote",
        "method",
        "args"
})
@XmlRootElement(name = "service-invocation")
public class RmiServiceInvocation {

    @XmlElement
    protected String remote;
    @XmlElement(required = true)
    protected String method;

    protected RmiServiceInvocation.Args args;

    /**
     * Static create method from target object and method definition.
     * @return
     */
    public static RmiServiceInvocation create(Object remoteTarget, Method method, Object[] args) {
        RmiServiceInvocation serviceInvocation = new RmiServiceInvocation();

        if (Proxy.isProxyClass(remoteTarget.getClass())) {
            serviceInvocation.setRemote(method.getDeclaringClass().getName());
        } else {
            serviceInvocation.setRemote(remoteTarget.getClass().getName());
        }

        serviceInvocation.setMethod(method.getName());

        if (args != null) {
            serviceInvocation.setArgs(new RmiServiceInvocation.Args());

            for (Object arg : args) {
                MethodArg methodArg = new MethodArg();

                methodArg.setValueObject(arg);
                if (Map.class.isAssignableFrom(arg.getClass())) {
                    methodArg.setType(Map.class.getName());
                } else if (List.class.isAssignableFrom(arg.getClass())) {
                    methodArg.setType(List.class.getName());
                } else {
                    methodArg.setType(arg.getClass().getName());
                }

                serviceInvocation.getArgs().getArgs().add(methodArg);
            }
        }

        return serviceInvocation;
    }

    /**
     * Gets the argument types from list of args.
     * @return
     */
    public Class[] getArgTypes() {
        List<Class> types = new ArrayList<>();

        if (args != null) {
            for (MethodArg arg : args.getArgs()) {
                try {
                    types.add(Class.forName(arg.getType()));
                } catch (ClassNotFoundException e) {
                    throw new CitrusRuntimeException("Failed to access method argument type", e);
                }
            }
        }

        return types.toArray(new Class[types.size()]);
    }

    /**
     * Gets method args as objects. Automatically converts simple types and ready referenced beans.
     * @return
     */
    public Object[] getArgValues(ReferenceResolver referenceResolver) {
        List<Object> argValues = new ArrayList<>();

        try {
            if (args != null) {
                for (MethodArg methodArg : args.getArgs()) {
                    Class argType = Class.forName(methodArg.getType());
                    Object value = null;

                    if (methodArg.getValueObject() != null) {
                        value = methodArg.getValueObject();
                    } else if (methodArg.getValue() != null) {
                        value = methodArg.getValue();
                    } else if (StringUtils.hasText(methodArg.getRef()) && referenceResolver != null) {
                        value = referenceResolver.resolve(methodArg.getRef());
                    }

                    if (value == null) {
                        argValues.add(null);
                    } else if (argType.isInstance(value) || argType.isAssignableFrom(value.getClass())) {
                        argValues.add(argType.cast(value));
                    } else if (Map.class.equals(argType)) {
                        String mapString = value.toString();

                        Properties props = new Properties();
                        try {
                            props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replace(", ", "\n")));
                        } catch (IOException e) {
                            throw new CitrusRuntimeException("Failed to reconstruct method argument of type map", e);
                        }
                        Map<String, String> map = new LinkedHashMap<>();
                        for (Map.Entry<Object, Object> entry : props.entrySet()) {
                            map.put(entry.getKey().toString(), entry.getValue().toString());
                        }

                        argValues.add(map);
                    } else {
                        argValues.add(TypeConverter.lookupDefault().convertIfNecessary(value, argType));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Failed to construct method arg objects", e);
        }

        return argValues.toArray(new Object[argValues.size()]);
    }

    /**
     * Gets the value of the remote property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Sets the value of the remote property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRemote(String value) {
        this.remote = value;
    }

    /**
     * Gets the value of the method property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the args property.
     *
     * @return
     *     possible object is
     *     {@link RmiServiceInvocation.Args }
     *
     */
    public RmiServiceInvocation.Args getArgs() {
        return args;
    }

    /**
     * Sets the value of the args property.
     *
     * @param value
     *     allowed object is
     *     {@link RmiServiceInvocation.Args }
     *
     */
    public void setArgs(RmiServiceInvocation.Args value) {
        this.args = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "args"
    })
    public static class Args {

        @XmlElement(name = "arg", required = true)
        protected List<MethodArg> args;

        public List<MethodArg> getArgs() {
            if (args == null) {
                args = new ArrayList<MethodArg>();
            }
            return this.args;
        }

    }
}
