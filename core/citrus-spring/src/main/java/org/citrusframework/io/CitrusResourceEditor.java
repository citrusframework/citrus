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
package org.citrusframework.io;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;


/**
 * Editor for {@link Resource} descriptors, to automatically convert String locations e.g. file:C:/myfile.txt
 * or classpath:myfile.txt to {@link Resource} properties instead of using a String location property.
 * The path may contain ${...} placeholders, to be resolved as org.springframework.core.env.Environment
 * properties: e.g. ${user.dir}. Unresolvable placeholders are ignored by default.
 * Delegates to a {@link Resources} to do the {@link Resource} creation. The implementation follows the
 * implementation of {@link org.springframework.core.io.ResourceEditor}.
 *
 * @author Thorsten Schlathoelter
 * @since 4.0
 */
public class CitrusResourceEditor extends PropertyEditorSupport {

	@Nullable
	private PropertyResolver propertyResolver;

	private final boolean ignoreUnresolvablePlaceholders;

	public CitrusResourceEditor() {
		this(null);
	}

	public CitrusResourceEditor(@Nullable PropertyResolver propertyResolver) {
		this(propertyResolver, true);
	}

	public CitrusResourceEditor(@Nullable PropertyResolver propertyResolver,
			boolean ignoreUnresolvablePlaceholders) {
		this.propertyResolver = propertyResolver;
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}

	@Override
	public void setAsText(String text) {
		if (StringUtils.hasText(text)) {
			String locationToUse = resolvePath(text).trim();
			setValue(Resources.create(locationToUse));
		}
		else {
			setValue(null);
		}
	}

	protected String resolvePath(String path) {
		if (this.propertyResolver == null) {
			this.propertyResolver = new StandardEnvironment();
		}
		return (this.ignoreUnresolvablePlaceholders ? this.propertyResolver.resolvePlaceholders(path) :
				this.propertyResolver.resolveRequiredPlaceholders(path));
	}

	@Override
	@Nullable
	public String getAsText() {
		Resource value = (Resource) getValue();
		try {
			// Try to determine URL for resource.
			return (value != null ? value.getURL().toExternalForm() : "");
		}
		catch (IOException ex) {
			// Couldn't determine resource URL - return null to indicate
			// that there is no appropriate text representation.
			return null;
		}
	}

}
