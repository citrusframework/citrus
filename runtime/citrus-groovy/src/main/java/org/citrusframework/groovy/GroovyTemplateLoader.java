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

package org.citrusframework.groovy;

import java.io.IOException;

import org.citrusframework.container.Template;
import org.citrusframework.container.TemplateLoader;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.citrusframework.groovy.dsl.actions.ActionsScript;
import org.citrusframework.groovy.dsl.actions.TemplateConfiguration;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class GroovyTemplateLoader implements TemplateLoader, ReferenceResolverAware {

    private ReferenceResolver referenceResolver;

    @Override
    public Template load(String filePath) {
        try {
            Resource script = FileUtils.getFileResource(filePath);
            ImportCustomizer ic = new ImportCustomizer();
            Template.Builder builder = new Template.Builder();
            builder.setReferenceResolver(referenceResolver);
            GroovyShellUtils.run(ic, new TemplateConfiguration(builder),
                    ActionsScript.normalize(FileUtils.readToString(script)), null, null);

            return new Template(builder);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load XML template for source '" + filePath + "'", e);
        }
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Adds reference resolver in builder pattern style.
     * @param referenceResolver
     * @return
     */
    public GroovyTemplateLoader withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }
}
