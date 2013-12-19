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

class ZweigUtil {
    /* Taken from https://github.com/grails/grails-core/blob/770bb6ceb23bff3bf84236182e1aed897781bc03/grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsASTUtils.java#L439 */
    def static Parameter[] copyParameters(Parameter[] parameterTypes) {
        parameterTypes.collect {
            def type = it.type
            def name = it.name
            def init = it.initialExpression
            def newParam = new Parameter(nonGeneric(type), name, init)
            newParam.addAnotations(it.annotations)
            newParam
        } as Parameter[]
    }

    /* Taken from https://github.com/grails/grails-core/blob/770bb6ceb23bff3bf84236182e1aed897781bc03/grails-core/src/main/groovy/org/codehaus/groovy/grails/compiler/injection/GrailsASTUtils.java#L450 */
    def static ClassNode nonGeneric(ClassNode type) {
        if (type.isUsingGenerics()) {
            final ClassNode nonGen = ClassHelper.makeWithoutCaching(type.name)
            nonGen.redirect = type
            nonGen.genericsTypes = null
            nonGen.usingGenerics = false
            return nonGen
        }

        if (type.isArray()) {
            final ClassNode nonGen = ClassHelper.makeWithoutCaching(Object)
            nonGen.usingGenerics = false
            return nonGen.makeArray()
        }

        return type.plainNodeReference
    }
}
