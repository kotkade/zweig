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
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.runtime.NullObject

import java.lang.reflect.Modifier

class InternalCategory {
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

    /* toArgumentList */
    static toArgumentList(ArgumentListExpression l) {
        l
    }

    static toArgumentList(NullObject _null) {
        ArgumentListExpression.EMPTY_ARGUMENTS
    }

    static toArgumentList(List l) {
        new ArgumentListExpression(l.collect { it.toExpression() })
    }

    /* toModifier */
    static toModifier(Integer i) {
        i
    }

    static toModifier(Long l) {
        l
    }

    def private static Map modifierTranslation = [
            public:       Modifier.PUBLIC,
            private:      Modifier.PRIVATE,
            protected:    Modifier.PROTECTED,

            final:        Modifier.FINAL,
            abstract:     Modifier.ABSTRACT,
            static:       Modifier.STATIC,
            synchronized: Modifier.SYNCHRONIZED
    ]

    static toModifier(String s) {
        modifierTranslation[s.toLowerCase()]
    }

    static toModifier(List l) {
        l.inject(0) { mod, m -> mod | m.toModifier() }
    }

    static toModifier(NullObject _) {
        Modifier.PUBLIC
    }

    /* toBooleanExpression */
    static toBooleanExpression(BooleanExpression b) {
        b
    }

    static toBooleanExpression(Expression e) {
        new BooleanExpression(e)
    }

    static toBooleanExpression(Object o) {
        o.toExpression().toBooleanExpression()
    }

    /* toBlockStatement */
    static toBlockStatement(BlockStatement b) {
        b
    }

    static toBlockStatement(List l) {
        new BlockStatement(l.collect { it.toStatement() }, new VariableScope())
    }

    static toBlockStatement(Object o) {
        [o].toBlockStatement()
    }
}
