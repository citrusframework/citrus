package org.citrusframework.testapi;

import org.citrusframework.TestAction;
import org.citrusframework.actions.SendMessageAction.SendMessageActionBuilder;
import org.citrusframework.context.TestContext;

/**
 * Implementors of this interface are used to customize the SendMessageActionBuilder with application specific information. E.g. cookies
 * or transactionIds.
 */
public interface ApiActionBuilderCustomizerService {
    <T extends SendMessageActionBuilder<?,?,?>> T build(GeneratedApi generatedApi, TestAction action, TestContext context, T builder);
}
