import org.citrusframework.*
import org.citrusframework.variable.*
import org.citrusframework.context.TestContext
import org.citrusframework.validation.script.GroovyScriptExecutor
import groovy.util.XmlSlurper
import org.citrusframework.message.Message

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        String payload = receivedMessage.getPayload(String.class)

        def root;
        if (payload.length()) {
            root = new XmlSlurper().parseText(payload)
        } else {
            root = "";
        }

        @SCRIPTBODY@
    }
}
