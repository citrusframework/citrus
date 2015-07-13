/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.actions;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionTest extends AbstractTestNGUnitTest {
	
    private ExecutePLSQLAction executePLSQLAction;
    
    private JdbcTemplate jdbcTemplate = EasyMock.createMock(JdbcTemplate.class);
    
    @BeforeMethod
    public void setUp() {
        executePLSQLAction  = new ExecutePLSQLAction();
        executePLSQLAction.setJdbcTemplate(jdbcTemplate);
    }
    
	@Test
	public void testPLSQLExecutionWithInlineScript() {
	    String stmt = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;/";
	    
	    executePLSQLAction.setScript(stmt);
	    
	    String controlStatement = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;";
	    
	    reset(jdbcTemplate);
	    
	    jdbcTemplate.execute(controlStatement);
	    expectLastCall().once();
	    
	    replay(jdbcTemplate);
	    
	    executePLSQLAction.execute(context);
	    
	    verify(jdbcTemplate);
	}
	
	@Test
    public void testPLSQLExecutionWithInlineScriptNoEndingCharacter() {
        String stmt = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;";
        
        executePLSQLAction.setScript(stmt);
        
        String controlStatement = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testPLSQLExecutionWithFileResource() {
        executePLSQLAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-plsql.sql");
        
        String controlStatement = "DECLARE\n" + 
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
             "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                    "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
             "END;";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testPLSQLExecutionWithInlineScriptVariableSupport() {
	    context.setVariable("myText", "Hello World!");
	    context.setVariable("tableName", "Greetings");
	    
        String stmt = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := '${myText}';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from ${tableName} where text='${myText}';\"" +
             "END;/";
        
        executePLSQLAction.setScript(stmt);
        
        String controlStatement = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testPLSQLExecutionWithFileResourceVariableSupport() {
	    context.setVariable("myText", "Hello World!");
        context.setVariable("tableName", "Greetings");
        
        executePLSQLAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-plsql-with-variables.sql");
        
        String controlStatement = "DECLARE\n" + 
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
             "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                    "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
             "END;";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().once();
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testPLSQLExecutionWithMultipleInlineStatements() {
        String stmt = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;" +
             "/" +
             "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;" +
             "/";
        
        executePLSQLAction.setScript(stmt);
        
        String controlStatement = "DECLARE " + 
                "Zahl1 number(2);" +
                "Text varchar(20) := 'Hello World!';" +
             "BEGIN" +
                "EXECUTE IMMEDIATE \"" +
                    "select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"" +
             "END;";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().times(2);
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
	
	@Test
    public void testPLSQLExecutionWithFileResourceMultipleStmts() {
        executePLSQLAction.setSqlResourcePath("classpath:com/consol/citrus/actions/test-plsql-multiple-stmts.sql");
        
        String controlStatement = "DECLARE\n" + 
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
             "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                    "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
             "END;";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().times(2);
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
}
