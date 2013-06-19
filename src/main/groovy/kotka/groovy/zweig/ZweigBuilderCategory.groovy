/*-
 * Copyright 2013 © Meikel Brandmeyer.
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package kotka.groovy.zweig

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.runtime.NullObject

import java.lang.reflect.Modifier

class ZweigBuilderCategory {
    /* toZweig */
    static toZweig(Number n) {
        new ConstantExpression(n)
    }

    static toZweig(String s) {
        new ConstantExpression(s)
    }

    static toZweig(NullObject _null) {
        ConstantExpression.NULL
    }

    static toZweig(Boolean b) {
        b ? ConstantExpression.TRUE : ConstantExpression.FALSE
    }

    static toZweig(Class c) {
        new ClassExpression(c.toClassNode())
    }

    static toZweig(List l) {
        new ListExpression(l.collect { it.toZweig() })
    }

    static final mapToZweig = [
            variable: { new VariableExpression(it["variable"]) },

            method: {
                def methodName = it["method"]
                def modifier   = it["modifier"] ?: Modifier.PUBLIC

                def parameters = it["arguments"].collect  { it.toParameter() }
                def exceptions = it["exceptions"].collect { it.toClassNode() }

                def returnType = it["returnType"] ?: ClassHelper.OBJECT_TYPE
                def body       = it["body"].collect {
                    it.toStatement()
                }

                new MethodNode(
                    methodName,
                    modifier,
                    returnType.toClassNode(),
                    parameters as Parameter[],
                    exceptions as ClassNode[],
                    new BlockStatement(body, new VariableScope())
                )
            },

            call: {
                new MethodCallExpression(
                        it["on"].toZweig(),
                        it["call"].toZweig(),
                        it["with"].toArgumentList()
                )
            },

            callStatic: {
                new StaticMethodCallExpression(
                        it["on"].toClassNode(),
                        it["callStatic"],
                        it["with"].toArgumentList()
                )
            },

            constructor: {
                def modifiers  = it["modifiers"] ?: Modifier.PUBLIC
                def parameters = it["constructor"].collect { it.toParameter() }
                def exceptions = it["exceptions"].collect  { it.toClassNode() }
                def body       = it["body"].collect { it.toStatement() }

                new ConstructorNode(
                        modifiers,
                        parameters as Parameter[],
                        exceptions as ClassNode[],
                        new BlockStatement(body, new VariableScope())
                )
            },

            construct: {
                new ConstructorCallExpression(
                        it["construct"].toClassNode(),
                        it["with"].toArgumentList()
                )
            }
    ]

    static toZweig(Map m) {
        def action = m.keySet().find {
            mapToZweig.containsKey(it)
        }

        if (action != null) {
            mapToZweig[action](m)
        }
    }

    /* toParameter */
    static toParameter(Parameter p) {
        p
    }

    static toParameter(Map m) {
        m.inject(null) { _ignore, p, k ->
            new Parameter(k.toClassNode(), p)
        }
    }

    /* toClassNode */
    static toClassNode(ClassNode n) {
        n
    }

    static toClassNode(Class c) {
        ClassHelper.make(c, false)
    }

    static toClassNode(String s) {
        Class.forName(s).toClassNode()
    }

    /* toStatement */
    static toStatement(Statement s) {
        s
    }

    static toStatement(Expression e) {
        new ExpressionStatement(e)
    }

    static toStatement(Object o) {
        o.toZweig().toStatement()
    }

    /* toArgumentList */
    static toArgumentList(ArgumentListExpression l) {
        l
    }

    static toArgumentList(NullObject _null) {
        ArgumentListExpression.EMPTY_ARGUMENTS
    }

    static toArgumentList(List l) {
        new ArgumentListExpression(l.collect { it.toZweig() })
    }
}
