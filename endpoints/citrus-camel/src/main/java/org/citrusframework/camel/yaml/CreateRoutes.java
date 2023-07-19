package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.CreateCamelRouteAction;

public class CreateRoutes implements CamelRouteActionBuilderWrapper<CreateCamelRouteAction.Builder> {
    private final CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder();

    public void setRouteContext(String routeContext) {
        builder.routeContext(routeContext);
    }

    @Override
    public CreateCamelRouteAction.Builder getBuilder() {
        return builder;
    }

}
