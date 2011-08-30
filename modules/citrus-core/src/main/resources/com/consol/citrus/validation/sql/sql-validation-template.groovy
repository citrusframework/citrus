import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.sql.SqlResultSetScriptExecutor

import java.util.List;
import java.util.Map;

public class ValidationScript implements SqlResultSetScriptExecutor{
    public void validate(List<Map<String, Object>> rows, TestContext context){
        @SCRIPTBODY@
    }
}