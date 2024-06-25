package org.citrusframework.spi;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
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
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AbstractReferenceResolverAwareTestActionBuilderTest {

    @Mock
    private ReferenceResolver referenceResolver;

    @Mock
    private TestReferenceResolver referenceResolverAware;

    private AbstractReferenceResolverAwareTestActionBuilder fixture;

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

        setField(fixture, "delegate", referenceResolverAware);
    }

    @Test
    public void getDelegate() {
        assertEquals(referenceResolverAware, fixture.getDelegate());
    }

    @Test
    public void setReferenceResolver() {
        fixture.setReferenceResolver(referenceResolver);

        assertNotNull(getField(fixture, "referenceResolver"), "ReferenceResolver should be set");
        verify(referenceResolverAware).setReferenceResolver(referenceResolver);
    }

    @Test
    public void setReferenceResolver_doesNotPropagateToNonReferenceResolverAware() {
        var testActionBuilder = mock(TestActionBuilder.class);
        setField(fixture, "delegate", testActionBuilder);

        fixture.setReferenceResolver(referenceResolver);

        assertNotNull(getField(fixture, "referenceResolver"), "ReferenceResolver should be set");
        verifyNoInteractions(referenceResolverAware);
        verifyNoInteractions(testActionBuilder);
    }

    @Test
    public void setReferenceResolver_ignoresNullReferenceResolver() {
        fixture.setReferenceResolver(null);

        assertNull(getField(fixture, "referenceResolver"), "ReferenceResolver should NOT be set");
        verifyNoInteractions(referenceResolverAware);
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        openedMocks.close();
    }

    private abstract static class TestReferenceResolver implements TestActionBuilder, ReferenceResolverAware {
    }
}
