package org.citrusframework.openapi.generator.util;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.SendMessageAction.SendMessageActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.testapi.ApiActionBuilderCustomizerService;
import org.citrusframework.testapi.GeneratedApi;

public class TestApiActionBuilderCustomizer implements ApiActionBuilderCustomizerService {

    @Override
    public <T extends SendMessageActionBuilder<?,?,?>> T build(GeneratedApi generatedApi, TestAction action,
        TestContext context, T builder) {

        generatedApi.getApiInfoExtensions().forEach((key, value) -> {
            builder.getMessageBuilderSupport().header(key, value);
        });

        return builder;
    }
}
