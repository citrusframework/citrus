/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;

import org.easymock.classextension.EasyMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionTest extends AbstractBaseTest {
	
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
        executePLSQLAction.setSqlResource(new ClassPathResource("test-plsql.sql", ExecutePLSQLActionTest.class));
        
        String controlStatement = "DECLARE\n" + 
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
             "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                    "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
             "END;\n";
        
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
        
        executePLSQLAction.setSqlResource(new ClassPathResource("test-plsql-with-variables.sql", ExecutePLSQLActionTest.class));
        
        String controlStatement = "DECLARE\n" + 
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
             "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                    "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
             "END;\n";
        
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
        executePLSQLAction.setSqlResource(new ClassPathResource("test-plsql-multiple-stmts.sql", ExecutePLSQLActionTest.class));
        
        String controlStatement = "DECLARE\n" + 
                "    Zahl1 number(2);\n" +
                "    Text varchar(20) := 'Hello World!';\n" +
             "BEGIN\n" +
                "    EXECUTE IMMEDIATE \"\n" +
                    "        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';\"\n" +
             "END;\n";
        
        reset(jdbcTemplate);
        
        jdbcTemplate.execute(controlStatement);
        expectLastCall().times(2);
        
        replay(jdbcTemplate);
        
        executePLSQLAction.execute(context);
        
        verify(jdbcTemplate);
    }
}
