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

package org.citrusframework.camel.message;

import org.apache.camel.BeanScope;
import org.apache.camel.Expression;
import org.apache.camel.ExpressionFactory;
import org.apache.camel.builder.ExpressionClauseSupport;
import org.apache.camel.support.builder.Namespaces;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.processor.camel.CamelExpressionClause;

public class CamelExpressionClauseSupport<T extends MessageProcessor.Builder<?, ?>> extends ExpressionClauseSupport<T>
        implements CamelExpressionClause<T, ExpressionFactory, Expression> {

    public CamelExpressionClauseSupport(T result) {
        super(result);
    }

    @Override
    public T method(String ref, Object scope) {
        if (scope instanceof BeanScope beanScope) {
            return method(ref, beanScope);
        }

        throw new CitrusRuntimeException("Invalid scope type, is not a bean scope");
    }

    @Override
    public T method(String ref, String method, Object scope) {
        if (scope instanceof BeanScope beanScope) {
            return method(ref, method, beanScope);
        }

        throw new CitrusRuntimeException("Invalid scope type, is not a bean scope");
    }

    @Override
    public T method(Class<?> beanType, Object scope) {
        if (scope instanceof BeanScope beanScope) {
            return method(beanType, beanScope);
        }

        throw new CitrusRuntimeException("Invalid scope type, is not a bean scope");
    }

    @Override
    public T method(Class<?> beanType, String method, Object scope) {
        if (scope instanceof BeanScope beanScope) {
            return method(beanType, method, beanScope);
        }

        throw new CitrusRuntimeException("Invalid scope type, is not a bean scope");
    }

    @Override
    public T xtokenize(String path, char mode, Object namespaces, int group) {
        if (namespaces instanceof Namespaces ns) {
            return xtokenize(path, mode, ns, group);
        }

        throw new CitrusRuntimeException("Invalid namespaces type: " + namespaces.getClass());
    }

    @Override
    public T xpath(String text, Class<?> resultType, Object namespaces) {
        if (namespaces instanceof Namespaces ns) {
            return xpath(text, resultType, ns);
        }

        throw new CitrusRuntimeException("Invalid namespaces type: " + namespaces.getClass());
    }

    @Override
    public T xpath(String text, Object namespaces) {
        if (namespaces instanceof Namespaces ns) {
            return xpath(text, ns);
        }

        throw new CitrusRuntimeException("Invalid namespaces type: " + namespaces.getClass());
    }

    @Override
    public T xquery(String text, Class<?> resultType, Object namespaces) {
        if (namespaces instanceof Namespaces ns) {
            return xquery(text, resultType, ns);
        }

        throw new CitrusRuntimeException("Invalid namespaces type: " + namespaces.getClass());
    }

    @Override
    public T xquery(String text, Object namespaces) {
        if (namespaces instanceof Namespaces ns) {
            return xquery(text, ns);
        }

        throw new CitrusRuntimeException("Invalid namespaces type: " + namespaces.getClass());
    }
}
