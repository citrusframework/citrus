package org.citrusframework.camel.yaml;

import java.util.List;

import org.citrusframework.camel.actions.StopCamelRouteAction;

public class StopRoutes implements CamelRouteActionBuilderWrapper<StopCamelRouteAction.Builder> {
    private final StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder();

    public void setRoutes(List<String> routeIds) {
        builder.routeIds(routeIds);
    }

    @Override
    public StopCamelRouteAction.Builder getBuilder() {
        return builder;
    }
}
