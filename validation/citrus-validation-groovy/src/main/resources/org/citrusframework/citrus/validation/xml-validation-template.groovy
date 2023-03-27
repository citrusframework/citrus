import org.citrusframework.citrus.*
import org.citrusframework.citrus.variable.*
import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.validation.script.GroovyScriptExecutor
import groovy.util.XmlSlurper
import org.citrusframework.citrus.message.Message

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
