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
import org.codehaus.groovy.ast.stmt.BlockStatement

import java.lang.reflect.Modifier

class NodeCategory {
    /* toNode */
    static toNode(ASTNode n) {
        n
    }

    static final mapToNode = [
            constructor: {
                def modifier   = it["modifier"] ?: Modifier.PUBLIC
                def parameters = it["constructor"].collect { it.toParameter() }
                def exceptions = it["exceptions"].collect  { it.toClassNode() }
                def body       = it["body"].collect { it.toStatement() }

                new ConstructorNode(
                        modifier.toModifier(),
                        parameters as Parameter[],
                        exceptions as ClassNode[],
                        new BlockStatement(body, new VariableScope())
                )
            },

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
                        modifier.toModifier(),
                        returnType.toClassNode(),
                        parameters as Parameter[],
                        exceptions as ClassNode[],
                        new BlockStatement(body, new VariableScope())
                )
            },

            field: {
                def fieldName = it["field"]
                def modifier  = it["modifier"].toModifier()
                def className = it["of"].toClassNode()
                def type      = it["type"].toClassNode()
                def init      = it["init"].toExpression()

                new FieldNode(
                    fieldName,
                    modifier,
                    type,
                    className,
                    init
                )
            }
    ]

    static toNode(Map m) {
        def action = m.keySet().find {
            mapToNode.containsKey(it)
        }

        if (action != null) {
            mapToNode[action](m)
        } else {
            def keys = m.keySet().join ", "
            throw new IllegalArgumentException(
                    "Invalid node specification map with keys: $keys"
            )
        }
    }
}
