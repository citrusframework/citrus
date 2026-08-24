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

package org.citrusframework.playwright.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.PlaywrightAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * XML DSL entry point for Playwright actions.
 *
 * <p>Registered via {@code META-INF/citrus/xml/builder/playwright} so XML test
 * cases can use the {@code playwright} action element in the Citrus testcase
 * namespace.</p>
 */
@XmlRootElement(name = "playwright")
public class Playwright implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private AbstractPlaywrightAction.Builder<?, ?> builder;

    private String description;
    private String actor;
    private String browserName;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public void setDescription(String value) {
        this.description = value;
    }

    @XmlAttribute
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * Use a custom Playwright browser endpoint referenced by name.
     */
    @XmlAttribute
    public void setBrowser(String browser) {
        this.browserName = browser;
    }

    @XmlElement
    public void setStart(Start builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setStop(Stop builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setOpen(Open builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setNavigate(Navigate builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setClick(Click builder) {
        this.builder = builder;
    }

    @XmlElement(name = "double-click")
    public void setDoubleClick(DoubleClick builder) {
        this.builder = builder;
    }

    @XmlElement(name = "right-click")
    public void setRightClick(RightClick builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setHover(Hover builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setFocus(Focus builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setTap(Tap builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setFill(Fill builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setClear(Clear builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setPress(Press builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setCheck(Check builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setUncheck(Uncheck builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setSelect(Select builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setUpload(Upload builder) {
        this.builder = builder;
    }

    @XmlElement(name = "wait")
    public void setWait(Wait builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setScreenshot(Screenshot builder) {
        this.builder = builder;
    }

    @XmlElement(name = "javascript")
    public void setJavaScript(JavaScript builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setVerify(Verify builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setExtract(Extract builder) {
        this.builder = builder;
    }

    @Override
    public PlaywrightAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Playwright action - please provide proper action details");
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (browserName != null) {
                builder.browser(referenceResolver.resolve(browserName, PlaywrightBrowser.class));
            }

            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
