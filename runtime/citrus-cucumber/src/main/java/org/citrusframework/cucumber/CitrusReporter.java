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

package org.citrusframework.cucumber;

import org.citrusframework.CitrusInstanceManager;
import io.cucumber.core.plugin.DefaultSummaryPrinter;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.SummaryPrinter;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestRunFinished;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusReporter implements SummaryPrinter, ColorAware, ConcurrentEventListener {

    public static final String SUITE_NAME = "cucumber-suite";

    private final DefaultSummaryPrinter delegate = new DefaultSummaryPrinter();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestRunFinished.class, event -> CitrusInstanceManager.getOrDefault().afterSuite(SUITE_NAME));
        delegate.setEventPublisher(publisher);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        delegate.setMonochrome(monochrome);
    }
}
