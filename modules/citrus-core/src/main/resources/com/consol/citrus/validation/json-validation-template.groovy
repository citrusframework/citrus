import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.GroovyScriptExecutor
import groovy.json.JsonSlurper
import com.consol.citrus.message.Message

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