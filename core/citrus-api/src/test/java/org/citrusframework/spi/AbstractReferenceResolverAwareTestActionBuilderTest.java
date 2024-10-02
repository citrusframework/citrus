package org.citrusframework.spi;

import java.lang.reflect.Field;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.util.ReflectionHelper;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

public class AbstractReferenceResolverAwareTestActionBuilderTest {

    @Mock
    private ReferenceResolver referenceResolver;

    @Mock
    private TestReferenceResolver referenceResolverAware;

    private AbstractReferenceResolverAwareTestActionBuilder fixture;

    private static final Field delegate = ReflectionHelper.findField(
            AbstractReferenceResolverAwareTestActionBuilder.class, "delegate");

    private static final Field referenceResolverField = ReflectionHelper.findField(
            AbstractReferenceResolverAwareTestActionBuilder.class, "referenceResolver");

    private AutoCloseable openedMocks;

    @BeforeMethod
    public void beforeMethod() {
        openedMocks = openMocks(this);

        fixture = new AbstractReferenceResolverAwareTestActionBuilder() {

            @Override
            public TestAction build() {
                throw new IllegalArgumentException("Test implementation!");
            }
        };

        ReflectionHelper.setField(delegate, fixture, referenceResolverAware);
    }

    @Test
    public void getDelegate() {
        assertEquals(referenceResolverAware, fixture.getDelegate());
    }

    @Test
    public void setReferenceResolver() {
        fixture.setReferenceResolver(referenceResolver);

        assertNotNull(ReflectionHelper.getField(referenceResolverField, fixture), "ReferenceResolver should be set");
        verify(referenceResolverAware).setReferenceResolver(referenceResolver);
    }

    @Test
    public void setReferenceResolver_doesNotPropagateToNonReferenceResolverAware() {
        var testActionBuilder = mock(TestActionBuilder.class);
        ReflectionHelper.setField(delegate, fixture, testActionBuilder);

        fixture.setReferenceResolver(referenceResolver);

        assertNotNull(ReflectionHelper.getField(referenceResolverField, fixture), "ReferenceResolver should be set");
        verifyNoInteractions(referenceResolverAware);
        verifyNoInteractions(testActionBuilder);
    }

    @Test
    public void setReferenceResolver_ignoresNullReferenceResolver() {
        fixture.setReferenceResolver(null);

        assertNull(ReflectionHelper.getField(referenceResolverField, fixture), "ReferenceResolver should NOT be set");
        verifyNoInteractions(referenceResolverAware);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        openedMocks.close();
    }

    private abstract static class TestReferenceResolver implements TestActionBuilder, ReferenceResolverAware {
    }
}
