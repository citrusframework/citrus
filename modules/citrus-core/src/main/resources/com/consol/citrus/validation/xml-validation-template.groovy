import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.GroovyScriptExecutor
import groovy.util.XmlSlurper
import com.consol.citrus.message.Message

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message receivedMessage, TestContext context){
        def root = new XmlSlurper().parseText(receivedMessage.getPayload(String.class))
        @SCRIPTBODY@
    }
}