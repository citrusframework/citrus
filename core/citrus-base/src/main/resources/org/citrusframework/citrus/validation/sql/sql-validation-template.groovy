import org.citrusframework.citrus.*
import org.citrusframework.citrus.variable.*
import org.citrusframework.citrus.context.TestContext
import org.citrusframework.citrus.validation.script.sql.SqlResultSetScriptExecutor

import java.util.List;
import java.util.Map;

public class ValidationScript implements SqlResultSetScriptExecutor{
    public void validate(List<Map<String, Object>> rows, TestContext context){
        @SCRIPTBODY@
    }
}
