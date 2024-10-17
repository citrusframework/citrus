package org.citrusframework.ws.client;

import static org.citrusframework.util.ReflectionHelper.getField;

import java.util.List;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

public final class WsTestUtils {

    @SuppressWarnings({"unchecked"})
    public static List<ClientInterceptor> getInterceptors(WebServiceClient webServiceClient) throws NoSuchFieldException {
        return (List<ClientInterceptor>) getField(
            WebServiceEndpointConfiguration.class.getDeclaredField("interceptors"),
            webServiceClient.getEndpointConfiguration());
    }
}
