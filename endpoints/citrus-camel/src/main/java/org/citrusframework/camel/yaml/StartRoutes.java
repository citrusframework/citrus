package org.citrusframework.camel.yaml;

import java.util.List;

import org.citrusframework.camel.actions.StartCamelRouteAction;

public class StartRoutes implements CamelRouteActionBuilderWrapper<StartCamelRouteAction.Builder> {
    private final StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder();

    public void setRoutes(List<String> routeIds) {
        builder.routeIds(routeIds);
    }

    @Override
    public StartCamelRouteAction.Builder getBuilder() {
        return builder;
    }
}
