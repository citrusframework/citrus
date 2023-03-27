import org.citrusframework.citrus.*
import org.citrusframework.citrus.variable.*
import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.script.GroovyAction.ScriptExecutor

public class GScript implements ScriptExecutor {
    public void execute(TestContext context) {
        @SCRIPTBODY@
    }
}
