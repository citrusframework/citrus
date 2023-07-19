package org.citrusframework.camel.yaml;

import java.util.List;

import org.citrusframework.camel.actions.RemoveCamelRouteAction;

public class RemoveRoutes implements CamelRouteActionBuilderWrapper<RemoveCamelRouteAction.Builder> {
    private final RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder();

    public void setRoutes(List<String> routeIds) {
        builder.routeIds(routeIds);
    }

    @Override
    public RemoveCamelRouteAction.Builder getBuilder() {
        return builder;
    }
}
