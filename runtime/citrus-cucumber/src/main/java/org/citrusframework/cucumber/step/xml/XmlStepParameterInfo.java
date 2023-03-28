package org.citrusframework.cucumber.step.xml;

import java.lang.reflect.Type;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.TypeResolver;

/**
 * @author Christoph Deppisch
 */
public class XmlStepParameterInfo implements ParameterInfo {

    private final Type type;

    public XmlStepParameterInfo(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isTransposed() {
        return false;
    }

    @Override
    public TypeResolver getTypeResolver() {
        return () -> type;
    }
}
