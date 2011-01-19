import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator.ValidationScriptExecutor

import java.util.List;
import java.util.Map;

public class ValidationScript implements ValidationScriptExecutor{
    public void validate(List<Map<String, Object>> rows, TestContext context){
        @SCRIPTBODY@
    }
}