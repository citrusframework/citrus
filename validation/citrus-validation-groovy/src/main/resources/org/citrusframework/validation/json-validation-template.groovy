import org.citrusframework.*
import org.citrusframework.variable.*
import org.citrusframework.context.TestContext
import org.citrusframework.validation.script.GroovyScriptExecutor
import groovy.json.JsonSlurper
import org.citrusframework.message.Message

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        String payload = receivedMessage.getPayload(String.class)

        def json;
        if (payload.length()) {
            json = new JsonSlurper().parseText(payload)
        } else {
            json = "";
        }

        @SCRIPTBODY@
    }
}
