/*
 * Copyright the original author or authors.
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

package org.citrusframework.message.processor.camel;

import java.util.Map;

import org.citrusframework.message.MessageProcessor;

public interface CamelExpressionClause<T extends MessageProcessor.Builder<?, ?>, F, E> {

    F getExpressionType();

    E getExpressionValue();

    T expression(E expression);

    T language(F factory);

    /**
     * Specify the constant expression value. <b>Important:</b> this is a fixed constant value that is only set once
     * during starting up the route, do not use this if you want dynamic values during routing.
     */
    T constant(Object value);

    /**
     * Specify the constant expression value. <b>Important:</b> this is a fixed constant value that is only set once
     * during starting up the route, do not use this if you want dynamic values during routing.
     */
    T constant(String value, Class<?> resultType);

    /**
     * Specify the constant expression value. <b>Important:</b> this is a fixed constant value that is only set once
     * during starting up the route, do not use this if you want dynamic values during routing.
     */
    T constant(Object value, boolean trim);

    /**
     * An expression of the exchange
     */
    T exchange();

    /**
     * An expression of an inbound message
     */
    T inMessage();

    /**
     * An expression of an inbound message body
     */
    T body();

    /**
     * An expression of an inbound message body converted to the expected type
     */
    T body(Class<?> expectedType);

    /**
     * An expression of an inbound message header of the given name
     */
    T header(String name);

    /**
     * An expression of the inbound headers
     */
    T headers();

    /**
     * An expression of the exchange pattern
     */
    T exchangePattern();

    /**
     * An expression of an exchange property of the given name
     */
    T exchangeProperty(String name);

    /**
     * An expression of the exchange properties
     */
    T exchangeProperties();

    /**
     * An expression of a variable with the given name
     */
    T variable(String name);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     *
     * @param  ref the name (bean id) of the bean to lookup from the registry
     * @return     the builder to continue processing the DSL
     */
    T method(String ref);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     *
     * @param  instance the existing instance of the bean
     * @return          the builder to continue processing the DSL
     */
    T method(Object instance);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     * <p>
     * Will lookup in registry and if there is a single instance of the same type, then the existing bean is used,
     * otherwise a new bean is created (requires a default no-arg constructor).
     *
     * @param  beanType the Class of the bean which we want to invoke
     * @return          the builder to continue processing the DSL
     */
    T method(Class<?> beanType);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     *
     * @param  ref    the name (bean id) of the bean to lookup from the registry
     * @param  method the name of the method to invoke on the bean
     * @return        the builder to continue processing the DSL
     */
    T method(String ref, String method);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     *
     * @param  ref   the name (bean id) of the bean to lookup from the registry
     * @param  scope the scope of the bean
     * @return       the builder to continue processing the DSL
     */
    T method(String ref, Object scope);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     *
     * @param  ref    the name (bean id) of the bean to lookup from the registry
     * @param  method the name of the method to invoke on the bean
     * @param  scope  the scope of the bean
     * @return        the builder to continue processing the DSL
     */
    T method(String ref, String method, Object scope);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     *
     * @param  instance the existing instance of the bean
     * @param  method   the name of the method to invoke on the bean
     * @return          the builder to continue processing the DSL
     */
    T method(Object instance, String method);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     * <p>
     * Will lookup in registry and if there is a single instance of the same type, then the existing bean is used,
     * otherwise a new bean is created (requires a default no-arg constructor).
     *
     * @param  beanType the Class of the bean which we want to invoke
     * @param  method   the name of the method to invoke on the bean
     * @return          the builder to continue processing the DSL
     */
    T method(Class<?> beanType, String method);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     * <p>
     * Will lookup in registry and if there is a single instance of the same type, then the existing bean is used,
     * otherwise a new bean is created (requires a default no-arg constructor).
     *
     * @param  beanType the Class of the bean which we want to invoke
     * @param  scope    the scope of the bean
     * @return          the builder to continue processing the DSL
     */
    T method(Class<?> beanType, Object scope);

    /**
     * Evaluates an expression using the <a href="http://camel.apache.org/bean-language.html>bean language</a> which
     * basically means the bean is invoked to determine the expression value.
     * <p>
     * Will lookup in registry and if there is a single instance of the same type, then the existing bean is used,
     * otherwise a new bean is created (requires a default no-arg constructor).
     *
     * @param  beanType the Class of the bean which we want to invoke
     * @param  method   the name of the method to invoke on the bean
     * @param  scope    the scope of the bean
     * @return          the builder to continue processing the DSL
     */
    T method(Class<?> beanType, String method, Object scope);

    /**
     * Evaluates a <a href="http://camel.apache.org/groovy.html">Groovy expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T groovy(String text);

    /**
     * Evaluates a JavaScript expression.
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T js(String text);

    /**
     * Evaluates an JavaScript expression
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T js(String text, Class<?> resultType);

    /**
     * Evaluates an JOOR expression
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    @Deprecated(since = "4.3.0")
    T joor(String text);

    /**
     * Evaluates an JOOR expression
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    @Deprecated(since = "4.3.0")
    T joor(String text, Class<?> resultType);

    /**
     * Evaluates an Java expression
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T java(String text);

    /**
     * Evaluates an Java expression
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T java(String text, Class<?> resultType);

    /**
     * Evaluates <a href="http://camel.apache.org/jq.html">JQ expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T jq(String text);

    /**
     * Evaluates <a href="http://camel.apache.org/jq.html">JQ expression</a>
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T jq(String text, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/datasonnet.html">Datasonnet expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T datasonnet(String text);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T jsonpath(String text);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a>
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @return                    the builder to continue processing the DSL
     */
    T jsonpath(String text, boolean suppressExceptions);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a>
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @param  allowSimple        whether to allow in inlined simple exceptions in the json path expression
     * @return                    the builder to continue processing the DSL
     */
    T jsonpath(String text, boolean suppressExceptions, boolean allowSimple);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a>
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T jsonpath(String text, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a>
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @param  resultType         the return type expected by the expression
     * @return                    the builder to continue processing the DSL
     */
    T jsonpath(String text, boolean suppressExceptions, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a>
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @param  allowSimple        whether to allow in inlined simple exceptions in the json path expression
     * @param  resultType         the return type expected by the expression
     * @return                    the builder to continue processing the DSL
     */
    T jsonpath(String text, boolean suppressExceptions, boolean allowSimple, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a> with writeAsString enabled.
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T jsonpathWriteAsString(String text);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a> with writeAsString enabled.
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T jsonpathWriteAsString(String text, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a> with writeAsString enabled.
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @return                    the builder to continue processing the DSL
     */
    T jsonpathWriteAsString(String text, boolean suppressExceptions);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a> with writeAsString enabled.
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @param  resultType         the return type expected by the expression
     * @return                    the builder to continue processing the DSL
     */
    T jsonpathWriteAsString(String text, boolean suppressExceptions, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a> with writeAsString enabled.
     *
     * @param  text               the expression to be evaluated
     * @param  suppressExceptions whether to suppress exceptions such as PathNotFoundException
     * @param  allowSimple        whether to allow in inlined simple exceptions in the json path expression
     * @return                    the builder to continue processing the DSL
     */
    T jsonpathWriteAsString(String text, boolean suppressExceptions, boolean allowSimple);

    /**
     * Evaluates a <a href="http://camel.apache.org/jsonpath.html">Json Path expression</a> with unpacking a
     * single-element array into an object enabled.
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T jsonpathUnpack(String text, Class<?> resultType);

    /**
     * Evaluates an <a href="http://camel.apache.org/ognl.html">OGNL expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T ognl(String text);

    /**
     * Evaluates a Python expression.
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T python(String text);

    /**
     * Evaluates Python expression
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T python(String text, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/mvel.html">MVEL expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T mvel(String text);

    /**
     * Evaluates an expression by looking up existing expressions from the
     * registry
     *
     * @param  ref refers to the expression to be evaluated
     * @return     the builder to continue processing the DSL
     */
    T ref(String ref);

    /**
     * Evaluates an <a href="http://camel.apache.org/spel.html">SpEL expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T spel(String text);

    /**
     * Evaluates a compiled simple expression
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T csimple(String text);

    /**
     * Evaluates a compiled simple expression
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T csimple(String text, Class<?> resultType);

    /**
     * Evaluates a <a href="http://camel.apache.org/simple.html">Simple expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T simple(String text);

    /**
     * Evaluates a <a href="http://camel.apache.org/simple.html">Simple expression</a>
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the result type
     * @return            the builder to continue processing the DSL
     */
    T simple(String text, Class<?> resultType);

    /**
     * Evaluates an <a href="http://camel.apache.org/hl7.html">HL7 Terser expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T hl7terser(String text);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token the token
     * @return       the builder to continue processing the DSL
     */
    T tokenize(String token);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token the token
     * @param  group to group by the given number
     * @return       the builder to continue processing the DSL
     */
    T tokenize(String token, int group);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token     the token
     * @param  group     to group by the given number
     * @param  skipFirst whether to skip the very first element
     * @return           the builder to continue processing the DSL
     */
    T tokenize(String token, int group, boolean skipFirst);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token the token
     * @param  regex whether the token is a regular expression or not
     * @return       the builder to continue processing the DSL
     */
    T tokenize(String token, boolean regex);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token the token
     * @param  regex whether the token is a regular expression or not
     * @param  group to group by the given number
     * @return       the builder to continue processing the DSL
     */
    T tokenize(String token, boolean regex, int group);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token     the token
     * @param  regex     whether the token is a regular expression or not
     * @param  group     to group by the given number
     * @param  skipFirst whether to skip the very first element
     * @return           the builder to continue processing the DSL
     */
    T tokenize(String token, boolean regex, int group, boolean skipFirst);

    /**
     * Evaluates a token expression on the message body
     *
     * @param  token     the token
     * @param  regex     whether the token is a regular expression or not
     * @param  group     to group by the given number
     * @param  skipFirst whether to skip the very first element
     * @return           the builder to continue processing the DSL
     */
    T tokenize(String token, boolean regex, String group, boolean skipFirst);

    /**
     * Evaluates a token pair expression on the message body
     *
     * @param  startToken    the start token
     * @param  endToken      the end token
     * @param  includeTokens whether to include tokens
     * @return               the builder to continue processing the DSL
     */
    T tokenizePair(String startToken, String endToken, boolean includeTokens);

    /**
     * Evaluates a token pair expression on the message body with XML content
     *
     * @param  tagName                 the tag name of the child nodes to tokenize
     * @param  inheritNamespaceTagName optional parent or root tag name that contains namespace(s) to inherit
     * @param  group                   to group by the given number
     * @return                         the builder to continue processing the DSL
     */
    T tokenizeXMLPair(String tagName, String inheritNamespaceTagName, int group);

    /**
     * Evaluates a token pair expression on the message body with XML content
     *
     * @param  tagName                 the tag name of the child nodes to tokenize
     * @param  inheritNamespaceTagName optional parent or root tag name that contains namespace(s) to inherit
     * @param  group                   to group by the given number
     * @return                         the builder to continue processing the DSL
     */
    T tokenizeXMLPair(String tagName, String inheritNamespaceTagName, String group);

    /**
     * Evaluates an XML token expression on the message body with XML content
     *
     * @param  path       the xpath like path notation specifying the child nodes to tokenize
     * @param  mode       one of 'i', 'w', or 'u' to inject the namespaces to the token, to wrap the token with its
     *                    ancestor contet, or to unwrap to its element child
     * @param  namespaces the namespace map to the namespace bindings
     * @param  group      to group by the given number
     * @return            the builder to continue processing the DSL
     */
    T xtokenize(String path, char mode, Object namespaces, int group);

    /**
     * Evaluates an <a href="http://camel.apache.org/xpath.html">XPath expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T xpath(String text);

    /**
     * Evaluates an <a href="http://camel.apache.org/xpath.html">XPath expression</a> with the specified result type
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T xpath(String text, Class<?> resultType);

    /**
     * Evaluates an <a href="http://camel.apache.org/xpath.html">XPath expression</a> with the specified result type and
     * set of namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xpath(String text, Class<?> resultType, Object namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xpath.html">XPath expression</a> with the specified result type and
     * set of namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xpath(String text, Class<?> resultType, Map<String, String> namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xpath.html">XPath expression</a> with the specified set of
     * namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xpath(String text, Object namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xpath.html">XPath expression</a> with the specified set of
     * namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xpath(String text, Map<String, String> namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xquery.html">XQuery expression</a>
     *
     * @param  text the expression to be evaluated
     * @return      the builder to continue processing the DSL
     */
    T xquery(String text);

    /**
     * Evaluates an <a href="http://camel.apache.org/xquery.html">XQuery expression</a> with the specified result type
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @return            the builder to continue processing the DSL
     */
    T xquery(String text, Class<?> resultType);

    /**
     * Evaluates an <a href="http://camel.apache.org/xquery.html">XQuery expression</a> with the specified result type
     * and set of namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xquery(String text, Class<?> resultType, Object namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xquery.html">XQuery expression</a> with the specified result type
     * and set of namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  resultType the return type expected by the expression
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xquery(String text, Class<?> resultType, Map<String, String> namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xquery.html">XQuery expression</a> with the specified set of
     * namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xquery(String text, Object namespaces);

    /**
     * Evaluates an <a href="http://camel.apache.org/xquery.html">XQuery expression</a> with the specified set of
     * namespace prefixes and URIs
     *
     * @param  text       the expression to be evaluated
     * @param  namespaces the namespace prefix and URIs to use
     * @return            the builder to continue processing the DSL
     */
    T xquery(String text, Map<String, String> namespaces);

    /**
     * Evaluates <a href="http://camel.apache.org/wasm.html">Wasm expression</a>
     *
     * @param  functionName the name of the Wasm function to be evaluated
     * @param  module       the Wasm module providing the expression function
     * @return              the builder to continue processing the DSL
     */
    T wasm(String functionName, String module);

    /**
     * Evaluates <a href="http://camel.apache.org/wasm.html">Wasm expression</a>
     *
     * @param  functionName the name of the Wasm function to be evaluated
     * @param  module       the Wasm module providing the expression function
     * @param  resultType   the return type expected by the expression
     * @return              the builder to continue processing the DSL
     */
    T wasm(String functionName, String module, Class<?> resultType);

    /**
     * Evaluates a given language name with the expression text
     *
     * @param  language   the name of the language
     * @param  expression the expression in the given language
     * @return            the builder to continue processing the DSL
     */
    T language(String language, String expression);

}
