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

import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression

class ZweigBuilder {
    def fromSpec(spec) {
        if (spec instanceof Number || spec instanceof String)
            buildConstantExpression spec
        else if (spec instanceof List)
            buildList spec
        else if (spec instanceof Map)
            buildMap spec
        else
            throw new Exception("Unknown specification type: ${spec}")
    }

    private def buildConstantExpression(spec) {
        new ConstantExpression(spec)
    }

    private def buildList(spec) {
        spec.collect { this.fromSpec(it) }
    }

    private def buildMap(spec) {
        if (spec.containsKey("variable"))
            buildVariableExpression(spec["variable"])
    }

    private def buildVariableExpression(v) {
        new VariableExpression(v)
    }
}