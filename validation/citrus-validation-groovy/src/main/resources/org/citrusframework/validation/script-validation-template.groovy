import org.citrusframework.*
import org.citrusframework.variable.*
import org.citrusframework.context.TestContext
import org.citrusframework.validation.script.GroovyScriptExecutor
import org.citrusframework.message.Message

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        Map<String, Object> headers = receivedMessage.getHeaders()
        String payload = receivedMessage.getPayload(String.class)

        @SCRIPTBODY@
    }
}
