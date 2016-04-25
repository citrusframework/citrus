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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.camel.actions.*;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Action executes Camel route action commands.
 * 
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelRouteActionBuilder extends AbstractTestActionBuilder<DelegatingTestAction<AbstractCamelRouteAction>> {

	/** Camel context */
	private ModelCamelContext camelContext;

	/** Spring application context */
	private ApplicationContext applicationContext;

	/**
	 * Constructor using action field.
	 */
	public CamelRouteActionBuilder() {
	    super(new DelegatingTestAction<AbstractCamelRouteAction>());
    }

	/**
	 * Sets the Camel context to use.
	 * @param camelContext
	 * @return
	 */
	public CamelRouteActionBuilder context(String camelContext) {
        Assert.notNull(applicationContext, "Citrus application context is not initialized!");
        this.camelContext = applicationContext.getBean(camelContext, ModelCamelContext.class);
		return this;
	}

	/**
	 * Sets the Camel context to use.
	 * @param camelContext
	 * @return
	 */
	public CamelRouteActionBuilder context(ModelCamelContext camelContext) {
		this.camelContext = camelContext;
		return this;
	}

	/**
	 * Execute control bus Camel operations.
	 * @return
	 */
	public CamelControlBusActionBuilder controlBus() {
		CamelControlBusAction camelControlBusAction = new CamelControlBusAction();
		camelControlBusAction.setCamelContext(getCamelContext());
		action.setDelegate(camelControlBusAction);

		return new CamelControlBusActionBuilder(camelControlBusAction);
	}

	/**
	 * Creates new Camel routes in route builder.
	 * @param routeBuilder
	 * @return
	 */
	public CamelRouteActionBuilder create(RouteBuilder routeBuilder) {
		CreateCamelRouteAction camelRouteAction = new CreateCamelRouteAction();

		try {
			if (!routeBuilder.getContext().equals(getCamelContext())) {
				routeBuilder.configureRoutes(getCamelContext());
			} else {
				routeBuilder.configure();
			}

			camelRouteAction.setRoutes(routeBuilder.getRouteCollection().getRoutes());
		} catch (Exception e) {
			throw new CitrusRuntimeException("Failed to configure route definitions with camel context", e);
		}

		camelRouteAction.setCamelContext(getCamelContext());
		action.setDelegate(camelRouteAction);
		return this;
	}

	/**
	 * Start these Camel routes.
	 */
	public void start(String ... routes) {
		StartCamelRouteAction camelRouteAction = new StartCamelRouteAction();
		camelRouteAction.setRouteIds(Arrays.asList(routes));

		camelRouteAction.setCamelContext(getCamelContext());
		action.setDelegate(camelRouteAction);
	}

	/**
	 * Stop these Camel routes.
	 */
	public void stop(String ... routes) {
		StopCamelRouteAction camelRouteAction = new StopCamelRouteAction();
		camelRouteAction.setRouteIds(Arrays.asList(routes));

		camelRouteAction.setCamelContext(getCamelContext());
		action.setDelegate(camelRouteAction);
	}

	/**
	 * Remove these Camel routes.
	 */
	public void remove(String ... routes) {
		RemoveCamelRouteAction camelRouteAction = new RemoveCamelRouteAction();
		camelRouteAction.setRouteIds(Arrays.asList(routes));

		camelRouteAction.setCamelContext(getCamelContext());
		action.setDelegate(camelRouteAction);
	}

	/**
	 * Sets the Spring bean application context.
	 * @param applicationContext
	 */
	public CamelRouteActionBuilder withApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		return this;
	}

	/**
	 * Gets the camel context either explicitly set before or default
	 * context from Spring application context.
	 * @return
	 */
	private ModelCamelContext getCamelContext() {
		if (camelContext == null) {
			Assert.notNull(applicationContext, "Citrus application context is not initialized!");

            if (applicationContext.containsBean("citrusCamelContext")) {
                camelContext = applicationContext.getBean("citrusCamelContext", ModelCamelContext.class);
            } else {
                camelContext = applicationContext.getBean(ModelCamelContext.class);
            }
		}

		return camelContext;
	}
}
