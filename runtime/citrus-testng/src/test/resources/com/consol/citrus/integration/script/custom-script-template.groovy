import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.script.GroovyAction.ScriptExecutor
import org.testng.Assert

public class GScript implements ScriptExecutor {
    public void execute(TestContext context) {
        context.setVariable("scriptTemplateVar", "It works!")
        @SCRIPTBODY@
    }
}
