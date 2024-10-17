package org.citrusframework.ws.client;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.mockito.Mock;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WebServiceEndpointConfigurationTest {

    @Mock
    private WebServiceTemplate webServiceTemplateMock;

    private AutoCloseable mockitoContext;

    private WebServiceEndpointConfiguration fixture;

    @BeforeMethod
    public void setUp() {
        mockitoContext = openMocks(this);

        fixture = new WebServiceEndpointConfiguration();
        fixture.setWebServiceTemplate(webServiceTemplateMock);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mockitoContext.close();
    }

    @Test
    public void containsLoggingClientInterceptorByDefault() {
        assertThat(fixture.getInterceptors())
            .hasSize(1)
            .satisfiesOnlyOnce(i -> assertThat(i).isInstanceOf(LoggingClientInterceptor.class));

        verify(webServiceTemplateMock)
            .setInterceptors(fixture.getInterceptors().toArray(new ClientInterceptor[0]));
    }

    @Test
    public void setInterceptors_overridesDefaultInterceptor() {
        var clientInterceptor = mock(ClientInterceptor.class);

        fixture.setInterceptors(singletonList(clientInterceptor));

        verifyFixtureContainsOnlyClientInterceptor(clientInterceptor);

        verify(webServiceTemplateMock)
            .setInterceptors(new ClientInterceptor[]{clientInterceptor});
    }

    @Test
    public void setInterceptor_overridesDefaultInterceptor() {
        var clientInterceptor = mock(ClientInterceptor.class);

        fixture.setInterceptor(clientInterceptor);

        verifyFixtureContainsOnlyClientInterceptor(clientInterceptor);

        verify(webServiceTemplateMock)
            .setInterceptors(new ClientInterceptor[]{clientInterceptor});
    }

    @Test
    public void addInterceptorAppendsToDefaultInterceptors() {
        var clientInterceptor = mock(ClientInterceptor.class);

        fixture.addInterceptor(clientInterceptor);

        assertThat(fixture.getInterceptors())
            .hasSize(2)
            .satisfiesOnlyOnce(i -> assertThat(i).isInstanceOf(LoggingClientInterceptor.class))
            .satisfiesOnlyOnce(i -> assertThat(i).isEqualTo(clientInterceptor));

        verify(webServiceTemplateMock)
            .setInterceptors(fixture.getInterceptors().toArray(new ClientInterceptor[0]));
    }

    private void verifyFixtureContainsOnlyClientInterceptor(ClientInterceptor clientInterceptor) {
        assertThat(fixture.getInterceptors())
            .hasSize(1)
            .satisfiesOnlyOnce(i -> assertThat(i).isEqualTo(clientInterceptor));
    }
}
