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

package com.consol.citrus.mvn.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Display help information on citrus-maven-plugin.<br/> Call <pre>  mvn citrus:help -Ddetail=true -Dgoal=&lt;goal-name&gt;</pre> to display parameter details.
 *
 * @version generated on Fri Mar 12 10:22:23 CET 2010
 * @author org.apache.maven.tools.plugin.generator.PluginHelpGenerator (version 2.4.3)
 * @goal help
 * @requiresProject false
 */
@SuppressWarnings("unchecked")
public class HelpMojo
    extends AbstractMojo
{
    /**
     * If <code>true</code>, display all settable properties for each goal.
     * 
     * @parameter expression="${detail}" default-value="false"
     */
    private boolean detail;

    /**
     * The name of the goal for which to show help. If unspecified, all goals will be displayed.
     * 
     * @parameter expression="${goal}"
     */
    private java.lang.String goal;

    /**
     * The maximum length of a display line, should be positive.
     * 
     * @parameter expression="${lineLength}" default-value="80"
     */
    private int lineLength;

    /**
     * The number of spaces per indentation level, should be positive.
     * 
     * @parameter expression="${indentSize}" default-value="2"
     */
    private int indentSize;


    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        if ( lineLength <= 0 )
        {
            getLog().warn( "The parameter 'lineLength' should be positive, using '80' as default." );
            lineLength = 80;
        }
        if ( indentSize <= 0 )
        {
            getLog().warn( "The parameter 'indentSize' should be positive, using '2' as default." );
            indentSize = 2;
        }

        StringBuffer sb = new StringBuffer();

        append( sb, "com.consol.citrus.mvn:citrus-maven-plugin:1.2-SNAPSHOT", 0 );
        append( sb, "", 0 );

        append( sb, "citrus-maven-plugin 1.2-SNAPSHOT", 0 );
        append( sb, "Citrus Maven Plugin", 1 );
        append( sb, "", 0 );

        if ( goal == null || goal.length() <= 0 )
        {
            append( sb, "This plugin has 4 goals:", 0 );
            append( sb, "", 0 );
        }

        if ( goal == null || goal.length() <= 0 || "create-excel-doc".equals( goal ) )
        {
            append( sb, "citrus:create-excel-doc", 0 );
            append( sb, "Goal which creates a test documentation in excel.", 1 );
            append( sb, "", 0 );
            if ( detail )
            {
                append( sb, "Available parameters:", 1 );
                append( sb, "", 0 );

                append( sb, "author (Default: Citrus Testframework)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "company (Default: Unknown)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "customHeaders", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "interactiveMode (Default: true)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "outputFile (Default: CitrusTests)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "pageTitle (Default: Citrus Test Documentation)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "testDirectory (Default: src/citrus/tests)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );
            }
        }

        if ( goal == null || goal.length() <= 0 || "create-html-doc".equals( goal ) )
        {
            append( sb, "citrus:create-html-doc", 0 );
            append( sb, "Goal which creates a test documentation in html.", 1 );
            append( sb, "", 0 );
            if ( detail )
            {
                append( sb, "Available parameters:", 1 );
                append( sb, "", 0 );

                append( sb, "columns (Default: 1)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "interactiveMode (Default: true)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "logo (Default: logo.png)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "outputFile (Default: CitrusTests)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "overviewTitle (Default: Overview)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "pageTitle (Default: Citrus Test Documentation)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "testDirectory (Default: src/citrus/tests)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );
            }
        }

        if ( goal == null || goal.length() <= 0 || "create-test".equals( goal ) )
        {
            append( sb, "citrus:create-test", 0 );
            append( sb, "Goal which creates a new test case using test case creator.", 1 );
            append( sb, "", 0 );
            if ( detail )
            {
                append( sb, "Available parameters:", 1 );
                append( sb, "", 0 );

                append( sb, "author (Default: Unknown)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "description (Default: TODO: Description)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "framework (Default: testng)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "interactiveMode (Default: true)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "name", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );

                append( sb, "targetPackage (Default: com.consol.citrus)", 2 );
                append( sb, "(no description available)", 3 );
                append( sb, "", 0 );
            }
        }

        if ( goal == null || goal.length() <= 0 || "help".equals( goal ) )
        {
            append( sb, "citrus:help", 0 );
            append( sb, "Display help information on citrus-maven-plugin.\nCall\n\u00a0\u00a0mvn\u00a0citrus:help\u00a0-Ddetail=true\u00a0-Dgoal=<goal-name>\nto display parameter details.", 1 );
            append( sb, "", 0 );
            if ( detail )
            {
                append( sb, "Available parameters:", 1 );
                append( sb, "", 0 );

                append( sb, "detail (Default: false)", 2 );
                append( sb, "If true, display all settable properties for each goal.", 3 );
                append( sb, "", 0 );

                append( sb, "goal", 2 );
                append( sb, "The name of the goal for which to show help. If unspecified, all goals will be displayed.", 3 );
                append( sb, "", 0 );

                append( sb, "lineLength (Default: 80)", 2 );
                append( sb, "The maximum length of a display line, should be positive.", 3 );
                append( sb, "", 0 );

                append( sb, "indentSize (Default: 2)", 2 );
                append( sb, "The number of spaces per indentation level, should be positive.", 3 );
                append( sb, "", 0 );
            }
        }

        if ( getLog().isInfoEnabled() )
        {
            getLog().info( sb.toString() );
        }
    }

    /**
     * <p>Repeat a String <code>n</code> times to form a new string.</p>
     *
     * @param str String to repeat
     * @param repeat number of times to repeat str
     * @return String with repeated String
     * @throws NegativeArraySizeException if <code>repeat < 0</code>
     * @throws NullPointerException if str is <code>null</code>
     */
    private static String repeat( String str, int repeat )
    {
        StringBuffer buffer = new StringBuffer( repeat * str.length() );

        for ( int i = 0; i < repeat; i++ )
        {
            buffer.append( str );
        }

        return buffer.toString();
    }

    /** 
     * Append a description to the buffer by respecting the indentSize and lineLength parameters.
     * <b>Note</b>: The last character is always a new line.
     * 
     * @param sb The buffer to append the description, not <code>null</code>.
     * @param description The description, not <code>null</code>.
     * @param indent The base indentation level of each line, must not be negative.
     */
    private void append( StringBuffer sb, String description, int indent )
    {
        for ( Iterator it = toLines( description, indent, indentSize, lineLength ).iterator(); it.hasNext(); )
        {
            sb.append( it.next().toString() ).append( '\n' );
        }
    }

    /** 
     * Splits the specified text into lines of convenient display length.
     * 
     * @param text The text to split into lines, must not be <code>null</code>.
     * @param indent The base indentation level of each line, must not be negative.
     * @param indentSize The size of each indentation, must not be negative.
     * @param lineLength The length of the line, must not be negative.
     * @return The sequence of display lines, never <code>null</code>.
     * @throws NegativeArraySizeException if <code>indent < 0</code>
     */
    private static List toLines( String text, int indent, int indentSize, int lineLength )
    {
        List lines = new ArrayList();

        String ind = repeat( "\t", indent );
        String[] plainLines = text.split( "(\r\n)|(\r)|(\n)" );
        for ( int i = 0; i < plainLines.length; i++ )
        {
            toLines( lines, ind + plainLines[i], indentSize, lineLength );
        }

        return lines;
    }

    /** 
     * Adds the specified line to the output sequence, performing line wrapping if necessary.
     * 
     * @param lines The sequence of display lines, must not be <code>null</code>.
     * @param line The line to add, must not be <code>null</code>.
     * @param indentSize The size of each indentation, must not be negative.
     * @param lineLength The length of the line, must not be negative.
     */
    private static void toLines( List lines, String line, int indentSize, int lineLength )
    {
        int lineIndent = getIndentLevel( line );
        StringBuffer buf = new StringBuffer( 256 );
        String[] tokens = line.split( " +" );
        for ( int i = 0; i < tokens.length; i++ )
        {
            String token = tokens[i];
            if ( i > 0 )
            {
                if ( buf.length() + token.length() >= lineLength )
                {
                    lines.add( buf.toString() );
                    buf.setLength( 0 );
                    buf.append( repeat( " ", lineIndent * indentSize ) );
                }
                else
                {
                    buf.append( ' ' );
                }
            }
            for ( int j = 0; j < token.length(); j++ )
            {
                char c = token.charAt( j );
                if ( c == '\t' )
                {
                    buf.append( repeat( " ", indentSize - buf.length() % indentSize ) );
                }
                else if ( c == '\u00A0' )
                {
                    buf.append( ' ' );
                }
                else
                {
                    buf.append( c );
                }
            }
        }
        lines.add( buf.toString() );
    }

    /** 
     * Gets the indentation level of the specified line.
     * 
     * @param line The line whose indentation level should be retrieved, must not be <code>null</code>.
     * @return The indentation level of the line.
     */
    private static int getIndentLevel( String line )
    {
        int level = 0;
        for ( int i = 0; i < line.length() && line.charAt( i ) == '\t'; i++ )
        {
            level++;
        }
        for ( int i = level + 1; i <= level + 4 && i < line.length(); i++ )
        {
            if ( line.charAt( i ) == '\t' )
            {
                level++;
                break;
            }
        }
        return level;
    }
}
