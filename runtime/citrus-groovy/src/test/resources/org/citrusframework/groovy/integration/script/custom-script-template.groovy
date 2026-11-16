package org.citrusframework.groovy.integration.script

import org.citrusframework.*
import org.citrusframework.variable.*
import org.citrusframework.context.TestContext
import org.citrusframework.groovy.actions.GroovyAction.ScriptExecutor
import org.junit.jupiter.api.Assertions

public class GScript implements ScriptExecutor {
    public void execute(TestContext context) {
        context.setVariable("scriptTemplateVar", "It works!")
        @SCRIPTBODY@
    }
}
