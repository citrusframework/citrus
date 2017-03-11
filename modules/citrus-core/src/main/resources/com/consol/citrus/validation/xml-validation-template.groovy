import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.GroovyScriptExecutor
import groovy.util.XmlSlurper
import com.consol.citrus.message.Message

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