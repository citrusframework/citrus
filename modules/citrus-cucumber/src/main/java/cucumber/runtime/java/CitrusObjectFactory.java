/*
 * Copyright 2006-2016 the original author or authors.
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

package cucumber.runtime.java;

import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusObjectFactory extends DefaultJavaObjectFactory {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusObjectFactory.class);

    /** Test designer */
    private TestDesigner designer;

    /** Test context */
    private TestContext context;

    /** Static self reference */
    private static CitrusObjectFactory selfReference;

    /**
     * Default constructor with static self reference initialization.
     */
    public CitrusObjectFactory() {
        selfReference = this;
    }

    @Override
    public void start() {
        super.start();
        context = CitrusBackend.getCitrus().createTestContext();
        designer = new DefaultTestDesigner(CitrusBackend.getCitrus().getApplicationContext(), context);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (CitrusObjectFactory.class.isAssignableFrom(type)) {
            return (T) this;
        }

        T instance = super.getInstance(type);
        CitrusAnnotations.injectAll(instance, CitrusBackend.getCitrus());

        injectTest(instance);

        return instance;
    }

    public void injectTest(final Object instance) {
        ReflectionUtils.doWithFields(instance.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                log.debug(String.format("Injecting Citrus framework instance on test class field '%s'", field.getName()));

                Class<?> type = field.getType();
                if (TestDesigner.class.isAssignableFrom(type)) {
                    log.debug("Injecting Citrus test designer on method parameter");
                    ReflectionUtils.setField(field, instance, designer);
                } else {
                    throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
                }
            }
        }, new ReflectionUtils.FieldFilter() {
            @Override
            public boolean matches(Field field) {
                if (field.isAnnotationPresent(CitrusResource.class)) {
                    if (!field.isAccessible()) {
                        ReflectionUtils.makeAccessible(field);
                    }

                    return true;
                }

                return false;
            }
        });
    }

    /**
     * Static access to self reference.
     * @return
     */
    public static CitrusObjectFactory instance() throws IllegalAccessException {
        if (selfReference == null) {
            throw new IllegalAccessException("Illegal access to self reference - not available yet");
        }

        return selfReference;
    }
}
