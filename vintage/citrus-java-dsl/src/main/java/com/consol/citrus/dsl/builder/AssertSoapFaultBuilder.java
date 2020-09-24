package com.consol.citrus.dsl.builder;

import java.nio.charset.Charset;

import com.consol.citrus.AbstractTestContainerBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultDetailValidationContext;
import com.consol.citrus.ws.validation.SoapFaultValidationContext;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public class AssertSoapFaultBuilder extends AbstractTestContainerBuilder<AssertSoapFault, AssertSoapFaultBuilder> {

    private final AssertSoapFault.Builder delegate = new AssertSoapFault.Builder();

    private SoapFaultDetailValidationContext.Builder detailValidationContext;
    private SoapFaultValidationContext.Builder validationContext = new SoapFaultValidationContext.Builder();

    public AssertSoapFaultBuilder when(TestAction action) {
        return when(() -> action);
    }

    public AssertSoapFaultBuilder when(TestActionBuilder<?> action) {
        return actions(action);
    }

    @Override
    public AssertSoapFaultBuilder actions(TestActionBuilder<?>... actions) {
        delegate.actions(actions);
        return this;
    }

    public AssertSoapFaultBuilder faultCode(String code) {
        delegate.faultCode(code);
        return this;
    }

    public AssertSoapFaultBuilder faultString(String faultString) {
        delegate.faultString(faultString);
        return this;
    }

    public AssertSoapFaultBuilder faultActor(String faultActor) {
        delegate.faultActor(faultActor);
        return this;
    }

    public AssertSoapFaultBuilder faultDetail(String faultDetail) {
        delegate.faultDetail(faultDetail);
        return this;
    }

    public AssertSoapFaultBuilder faultDetailResource(Resource resource) {
        return faultDetailResource(resource, FileUtils.getDefaultCharset());
    }

    public AssertSoapFaultBuilder faultDetailResource(Resource resource, Charset charset) {
        delegate.faultDetailResource(resource, charset);
        return this;
    }

    public AssertSoapFaultBuilder faultDetailResource(String filePath) {
        delegate.faultDetailResource(filePath);
        return this;
    }

    public AssertSoapFaultBuilder validator(SoapFaultValidator validator) {
        delegate.validator(validator);
        return this;
    }

    public AssertSoapFaultBuilder validator(String validatorName, ApplicationContext applicationContext) {
        delegate.withReferenceResolver(new SpringBeanReferenceResolver(applicationContext));
        delegate.validator(validatorName);
        return this;
    }

    public AssertSoapFaultBuilder schemaValidation(boolean enabled) {
        getDetailValidationContext().schemaValidation(enabled);
        return this;
    }

    public AssertSoapFaultBuilder xsd(String schemaName) {
        getDetailValidationContext().schema(schemaName);
        return this;
    }

    public AssertSoapFaultBuilder xsdSchemaRepository(String schemaRepository) {
        getDetailValidationContext().schemaRepository(schemaRepository);
        return this;
    }

    public AssertSoapFaultBuilder validationContext(SoapFaultValidationContext.Builder validationContext) {
        this.validationContext = validationContext;
        this.delegate.validate(this.validationContext);
        return this;
    }

    public AssertSoapFaultBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return this;
    }

    private SoapFaultDetailValidationContext.Builder getDetailValidationContext() {
        if (detailValidationContext == null) {
            detailValidationContext = new SoapFaultDetailValidationContext.Builder();
        }

        return detailValidationContext;
    }

    @Override
    public AssertSoapFault build() {
        if (detailValidationContext != null) {
            this.validationContext.detail(detailValidationContext.build());
            this.detailValidationContext = null;
        }

        return delegate.build();
    }
}
