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

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.runtime.NullObject
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import java.lang.reflect.Modifier

class ExpressionCategory {
    /* toExpression */
    static toExpression(Number n) {
        new ConstantExpression(n)
    }

    static toExpression(String s) {
        new ConstantExpression(s)
    }

    static toExpression(NullObject _null) {
        ConstantExpression.NULL
    }

    static toExpression(Boolean b) {
        b ? ConstantExpression.TRUE : ConstantExpression.FALSE
    }

    static toExpression(Class c) {
        c.toClassNode().toExpression()
    }

    static toExpression(ClassNode c) {
        new ClassExpression(c)
    }

    static toExpression(FieldNode f) {
        new FieldExpression(f)
    }

    static toExpression(Expression x) {
        x
    }

    static final mapToExpression = [
            variable: {
                new VariableExpression(it["variable"])
            },

            list: { m ->
                new ListExpression(m["list"].collect { it.toExpression() })
            },

            map: { m ->
                new MapExpression(m["map"].collect { k, v ->
                    new MapEntryExpression(k.toExpression(), v.toExpression())
                })
            },

            call: {
                new MethodCallExpression(
                        it["on"].toExpression(),
                        it["call"].toExpression(),
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

            construct: {
                new ConstructorCallExpression(
                        it["construct"].toClassNode(),
                        it["with"].toArgumentList()
                )
            },

            set: {
                new BinaryExpression(
                        it["set"].toExpression(),
                        new Token(Types.EQUALS, "=", -1, -1),
                        it["to"].toExpression()
                )
            },

            property: {
                new PropertyExpression(
                        (it["of"] ?: [variable: this]).toExpression(),
                        it["property"]
                )
            },

            field: {
                new FieldExpression(
                        it["of"].toClassNode().getField(it["field"])
                )
            }
    ]

    static toExpression(Map m) {
        def action = m.keySet().find {
            mapToExpression.containsKey(it)
        }

        if (action != null) {
            mapToExpression[action](m)
        } else {
            def keys = m.keySet().join ", "
            throw new IllegalArgumentException(
                    "Invalid specification map with keys: $keys"
            )
        }
    }
}
