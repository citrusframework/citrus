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
