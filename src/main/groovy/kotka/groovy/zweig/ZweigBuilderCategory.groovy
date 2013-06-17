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
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression

class ZweigBuilderCategory {
    /* toZweig */
    static toZweig(Number n) {
        new ConstantExpression(n)
    }

    static toZweig(String s) {
        new ConstantExpression(s)
    }

    static toZweig(Class c) {
        new ClassExpression(c.toClassNode())
    }

    static toZweig(List l) {
        l.collect { it.toZweig() }
    }

    static final mapToZweig = [
            variable: { new VariableExpression(it["variable"]) }
    ]

    static toZweig(Map m) {
        def action = m.keySet().find {
            mapToZweig.containsKey(it)
        }

        if (action != null) {
            mapToZweig[action](m)
        }
    }

    /* toClassNode */
    static toClassNode(ClassNode n) {
        n
    }

    static toClassNode(Class c) {
        ClassHelper.make(c, false)
    }
}
