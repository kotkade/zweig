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

/**
 * <code>ZweigBuilder</code> translates a high-level AST description
 * into the actual groovy AST representation. The sole entry point is
 * the <code>fromSpec</code> method. All of the below described structures
 * nest arbitrarily.
 *
 * <p>
 * Also <code>fromSpec</code> is idempotent. So you
 * may provide your custom created <code>ASTNodes</code> directly should
 * the need arise. In this case <code>fromSpec</code> will leave them
 * alone.
 *
 * <h2>Simple constants</h2>
 * <p>
 * The following constants are immediately translated to constant
 * expressions:
 *
 * <ul>
 *     <li>Strings
 *     <li>Numbers
 *     <li>Booleans
 *     <li><code>null</code>
 *     <li>Classes (actually represented as a <code>ClassExpressions</code>)
 * </ul>
 *
 * <h2>Complex and compound AST nodes</h2>
 * <p>
 * More complicated specifications are described by maps. The following
 * describes the available structures. Each key in the map describes part
 * of the information required to build the structure.
 *
 * <p>
 * <em>Note:</em> The first key described is always the one used to
 * decide which type of structure is actually meant.
 *
 * <p>
 * <em>Note:</em> In case there is the default value given for a
 * keyword, this keyword is optional. If it is not contained in the
 * specification map, then the default value will apply.
 *
 * <h3>List expressions</h3>
 * <dl>
 *     <dt><code>list</code>
 *     <dd>a list of AST specifications
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [list: ["foo", "bar"]]
 *
 *   // corresponding code
 *   ["foo", "bar"]
 * </code></pre>
 *
 * <h3>Map expressions</h3>
 * <dl>
 *     <dt><code>map</code>
 *     <dd>a map of AST specifications
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [map: [foo: "bar", baz: 5]]
 *
 *   // corresponding code
 *   [foo: "bar", baz: 5]
 * </code></pre>
 *
 * <h3>Variable expressions</h3>
 * <dl>
 *     <dt><code>variable</code>
 *     <dd>the name of the variable
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [variable: "foo"]
 *
 *   // corresponding code
 *   foo
 * </code></pre>
 *
 * <h3>Return statement</h3>
 * <dl>
 *     <dt><code>return</code>
 *     <dd>the value to be returned
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   //spec
 *   [return: [variable "foo"]]
 *
 *   // corresponding code
 *   return foo
 * </code></pre>
 *
 * <h3>Assignment</h3>
 * <dl>
 *     <dt><code>set</code>
 *     <dd>the left hand side of the assignment
 *
 *     <dt><code>to</code>
 *     <dd>the value
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [set: [variable: foo]
 *    to:  5]
 *
 *   // corresponding code
 *   foo = 5
 * </code></pre>
 *
 * <h3>Property access (via getter/setter)</h3>
 * <dl>
 *     <dt><code>property</code>
 *     <dd>the name of the property
 *
 *     <dt><code>of</code>
 *     <dd>the receiver of the accessor call<br>
 *         <em>Default:</em> <code>this</code>
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [property: "bar"
 *    of:       [variable: "foo"]]
 *
 *   // corresponding code
 *   foo.bar
 * </code></pre>
 *
 * <h3>Field access on <code>this</code> (<em>not</em> via getter/setter)</h3>
 * <dl>
 *     <dt><code>field</code>
 *     <dd>the name of the field
 *
 *     <dt><code>of</code>
 *     <dd>the class of <code>this</code>
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [field: "size"
 *    of:    String]
 *
 *   // corresponding code
 *   this.size
 * </code></pre>
 *
 * <h3>Method call</h3>
 * <dl>
 *     <dt><code>call</code>
 *     <dd>the name of method to call
 *
 *     <dt><code>on</code>
 *     <dd>the receiver of the method call
 *
 *     <dt><code>with</code>
 *     <dd>a list of arguments<br>
 *         <em>Default:</em> []
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [call: "bar",
 *    on:   [variable: "foo"],
 *    with: ["baz", 5]]
 *
 *   // corresponding code
 *   foo.bar("baz", 5)
 * </code></pre>
 *
 * <h3>Static method call</h3>
 * <dl>
 *     <dt><code>staticCall</code>
 *     <dd>the name of method to call
 *
 *     <dt><code>on</code>
 *     <dd>the class to call the static method on
 *
 *     <dt><code>with</code>
 *     <dd>a list of arguments<br>
 *         <em>Default:</em> []
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [staticCall: "format",
 *    on:         String,
 *    with:       ["%d", 5]]
 *
 *   // corresponding code
 *   String.format("%d", 5)
 * </code></pre>
 *
 * <h3>Constructor call</h3>
 * <dl>
 *     <dt><code>construct</code>
 *     <dd>the class
 *
 *     <dt><code>with</code>
 *     <dd>a list of arguments<br>
 *         <em>Default:</em> []
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [construct: String,
 *    with:      [[variable: "inputBytes"]]]
 *
 *   // corresponding code
 *   new String(inputBytes)
 * </code></pre>
 *
 * <h3>Method definition</h3>
 * <dl>
 *     <dt><code>method</code>
 *     <dd>the name of the method to define
 *
 *     <dt><code>returnType</code>
 *     <dd>the class of the return value.<br>
 *         <em>Default:</em> <code>Object</code>
 *
 *     <dt><code>modifier</code>
 *     <dd>modifiers like <code>public</code> or <code>static</code>. This
 *         can be a string for a single modifier, an integer (as per
 *         <code>java.lang.reflect.Modifier</code>) or a list of thereof.<br>
 *         <em>Default:</em> "public"
 *
 *     <dt><code>arguments</code>
 *     <dd>a list of maps with one key/value pair. The key being the name
 *         of the argument, the value the argument type.<br>
 *         <em>Default:</em> []
 *
 *     <dt><code>exceptions</code>
 *     <dd>a list of exceptions this method may throw<br>
 *         <em>Default:</em> []
 *
 *     <dt><code>body</code>
 *     <dd>a list of expressions comprising the body of the method<br>
 *         <em>Default:</em> []
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [method:     "frobnicateFoo"
 *    modifier:   "static"
 *    returnType: Boolean,
 *    arguments:  [[foo: AFoo], [bar: Integer], [baz: Integer]],
 *    body: [
 *       [call: "frobnicate",
 *        on:   [variable: "foo"],
 *        with  [[variable: "bar"]]],
 *       [call: "frobnicate",
 *        on:   [variable: "foo"],
 *        with: [[variable: "baz"]]],
 *       true
 *    ]]
 *
 *   // corresponding code
 *   def static Boolean frobnicateFoo(Integer bar, Integer baz) {
 *       foo.frobnicate bar
 *       foo.frobnicate baz
 *       true
 *   }
 * </code></pre>
 *
 * <h3>Constructor definition</h3>
 * <dl>
 *     <dt><code>constructor</code>
 *     <dd>the class
 *
 *     <dt><code>modifier</code>
 *     <dd>modifiers like <code>public</code> or <code>static</code>. This
 *         can be a string for a single modifier, an integer (as per
 *         <code>java.lang.reflect.Modifier</code>) or a list of thereof.<br>
 *         <em>Default:</em> "public"
 *
 *     <dt><code>arguments</code>
 *     <dd>a list of maps with one key/value pair. The key being the name
 *         of the argument, the value the argument type.<br>
 *         <em>Default:</em> []
 *
 *     <dt><code>exceptions</code>
 *     <dd>a list of exceptions this constructor may throw<br>
 *         <em>Default:</em> []
 *
 *     <dt><code>body</code>
 *     <dd>a list of expressions comprising the body of the constructor<br>
 *         <em>Default:</em> []
 * </dl>
 *
 * <p>
 * Example:
 *
 * <pre><code>   // spec
 *   [construct:  "Foo"
 *    arguments:  [[bar: Integer]],
 *    body: [
 *       [construct: ClassNode.SUPER,
 *        with       ["foo", [variable: "bar"]]]
 *    ]]
 *
 *   // corresponding code
 *   Foo(Integer bar) {
 *       super("foo", bar)
 *   }
 * </code></pre>
 *
 * @author Meikel Brandmeyer
 */
class ZweigBuilder {
    /**
     * Converts a high-level description of the desired outcome into
     * the actual groovy AST structure. <code>fromSpec</code> is
     * idempotent.
     *
     * @param   spec  a high-level AST specification
     * @return        the corresponding AST data structure
     */
    def static fromSpec(spec) {
        use(ZweigBuilderCategory) {
            spec.toZweig()
        }
    }
}
