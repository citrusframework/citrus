/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.selenium.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.JavaScriptAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

@XmlRootElement(name = "javascript")
public class JavaScript extends AbstractSeleniumAction.Builder<JavaScriptAction, JavaScript> {

    private final JavaScriptAction.Builder delegate = new JavaScriptAction.Builder();

    /**
     * Add script.
     * @param script
     */
    @XmlElement
    public void setScript(String script) {
        this.delegate.script(script);
    }

    /**
     * Add script arguments.
     * @param args
     */
    @XmlElement
    public void setArguments(Arguments args) {
        delegate.arguments(args.getArguments());
    }

    /**
     * Add script argument.
     * @param arg
     */
    @XmlAttribute
    public void setArgument(String arg) {
        delegate.argument(arg);
    }

    /**
     * Add expected error.
     * @param error
     */
    @XmlAttribute
    public void setError(String error) {
        this.delegate.error(error);
    }

    /**
     * Add expected error.
     * @param errors
     */
    @XmlElement
    public void setErrors(Errors errors) {
        this.delegate.errors(errors.getErrors());
    }

    @Override
    public JavaScript description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public JavaScript actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public JavaScript browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public JavaScriptAction build() {
        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "arguments"
    })
    public static class Arguments {
        @XmlElement(name = "argument")
        private List<String> arguments;

        public void setArguments(List<String> arguments) {
            this.arguments = arguments;
        }

        public List<String> getArguments() {
            if (arguments == null) {
                arguments = new ArrayList<>();
            }

            return arguments;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "errors"
    })
    public static class Errors {
        @XmlElement(name = "error")
        private List<String> errors;

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getErrors() {
            if (errors == null) {
                errors = new ArrayList<>();
            }

            return errors;
        }
    }
}
