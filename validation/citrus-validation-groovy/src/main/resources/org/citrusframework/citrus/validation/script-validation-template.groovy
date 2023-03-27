import org.citrusframework.citrus.*
import org.citrusframework.citrus.variable.*
import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.validation.script.GroovyScriptExecutor
import org.citrusframework.citrus.message.Message

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        Map<String, Object> headers = receivedMessage.getHeaders()
        String payload = receivedMessage.getPayload(String.class)

        @SCRIPTBODY@
    }
}
