package com.consol.citrus.dsl;

import java.util.Collections;

import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.script.sql.SqlResultSetScriptValidator;

public class ExecuteSQLQueryBuilderTest {	
	DataSource dataSource = EasyMock.createMock(DataSource.class);
	Resource resource = EasyMock.createMock(Resource.class);
	SqlResultSetScriptValidator validator = EasyMock.createMock(SqlResultSetScriptValidator.class);
	
	@Test
	public void testExecuteSQLQueryWithResource(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
		@Override
		protected void configure(){
			executeSQLQuery(dataSource)
			.sqlResource(resource)
			.controlResultSet(Collections.singletonMap("Test", Collections.singletonList("Value")))
			.scriptValidationContext(new ScriptValidationContext())
			.extractVariables(Collections.singletonMap("Test", "Value"))
			.validator(validator);
		}
	};
	
	builder.configure();
	
	Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
	
	ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
	
	Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
	Assert.assertEquals(action.getControlResultSet().size(), 1);
	Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "Test=[Value]");
	Assert.assertEquals(action.getExtractVariables().size(), 1);
	Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "Test=Value");
	Assert.assertEquals(action.getDataSource(), dataSource);
	Assert.assertEquals(action.getSqlResource(), resource);
	Assert.assertEquals(action.getValidator(), validator);
	}
	
	@Test
	public void testExecuteSQLQueryWithStatements(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
		@Override
		protected void configure(){
			executeSQLQuery(dataSource)
			.statements("stmnt1", "stmnt2", "stmnt3")
			.controlResultSet(Collections.singletonMap("Test", Collections.singletonList("Value")))
			.scriptValidationContext(new ScriptValidationContext())
			.extractVariables(Collections.singletonMap("Test", "Value"))
			.validator(validator);
		}
	};
	
	builder.configure();
	
	Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ExecuteSQLQueryAction.class);
	
	ExecuteSQLQueryAction action = (ExecuteSQLQueryAction)builder.getTestCase().getActions().get(0);
	
	Assert.assertEquals(action.getName(), ExecuteSQLQueryAction.class.getSimpleName());
	Assert.assertEquals(action.getControlResultSet().size(), 1);
	Assert.assertEquals(action.getControlResultSet().entrySet().iterator().next().toString(), "Test=[Value]");
	Assert.assertEquals(action.getExtractVariables().size(), 1);
	Assert.assertEquals(action.getExtractVariables().entrySet().iterator().next().toString(), "Test=Value");
	Assert.assertEquals(action.getStatements().size(), 3);
	Assert.assertEquals(action.getStatements().toString(), "[stmnt1, stmnt2, stmnt3]");
	Assert.assertEquals(action.getDataSource(), dataSource);
	Assert.assertEquals(action.getValidator(), validator);
	}
}
