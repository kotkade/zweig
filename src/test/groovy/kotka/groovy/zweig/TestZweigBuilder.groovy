package kotka.groovy.zweig

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import spock.lang.Specification

import java.lang.reflect.Modifier

class TestZweigBuilder extends Specification {
    def "Numbers are constants"() {
        expect:
        AstAssert.assertSyntaxTree(
                new ConstantExpression(value),
                ZweigBuilder.toExpression(value)
        )

        where:
        value << [ 1, 1.5 ]
    }

    def "Strings are constants"() {
        expect:
        AstAssert.assertSyntaxTree(
                new ConstantExpression("foo"),
                ZweigBuilder.toExpression("foo")
        )
    }

    def "null is constant"() {
        expect:
        ZweigBuilder.toExpression(null) == ConstantExpression.NULL
    }

    def "Booleans are constants"() {
        expect:
        ZweigBuilder.toExpression(b) == bExpr

        where:
        b     | bExpr
        true  | ConstantExpression.TRUE
        false | ConstantExpression.FALSE
    }

    def "Classes are constants"() {
        when:
        def z = ZweigBuilder.toExpression(String)

        then:
        AstAssert.assertSyntaxTree(
                new ClassExpression(ClassHelper.make(String, false)),
                z)
    }

    def "ClassNodes are constants"() {
        given:
        def klass = ClassHelper.make(String, false)

        expect:
        AstAssert.assertSyntaxTree(new ClassExpression(klass),
                ZweigBuilder.withCategories { klass.toExpression() })
    }

    def "Lists are constants"() {
        given:
        def target = new ListExpression([
            new ConstantExpression(1),
            new ConstantExpression(2),
            new ConstantExpression(3)
        ])

        when:
        def z = ZweigBuilder.toExpression([list: [1, 2, 3]])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Maps are constants"() {
        given:
        def target = new MapExpression([
                new MapEntryExpression(
                        new ConstantExpression("foo"),
                        new ConstantExpression(1)
                ),
                new MapEntryExpression(
                        new ConstantExpression("bar"),
                        new ConstantExpression(2)
                )
        ])

        when:
        def z = ZweigBuilder.toExpression([map: [foo: 1, bar: 2]])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "toExpression is idempotent"() {
        expect:
        AstAssert.assertSyntaxTree(x, ZweigBuilder.withCategories { x.toExpression() })

        where:
        x << [
                new ConstantExpression("foo"),
                new VariableExpression("foo"),
                new ListExpression([
                        new ConstantExpression("foo"),
                        new VariableExpression("foo"),
                ])
        ]
    }

    def "Variables dispatch on the 'variable' key"() {
        when:
        def z = ZweigBuilder.toExpression([variable: "x"])

        then:
        AstAssert.assertSyntaxTree(new VariableExpression("x"), z)
    }

    def "Return statements dispatch on the 'return' key"() {
        given:
        def target = new ReturnStatement(
                new ConstantExpression("foo")
        )

        when:
        def z = ZweigBuilder.toStatement([return: "foo"])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "If statements dispatch on the 'if' key"() {
        given:
        def target = new IfStatement(
                new BooleanExpression(ConstantExpression.TRUE),
                new ExpressionStatement(new ConstantExpression("foo")),
                new ExpressionStatement(new ConstantExpression("bar"))
        )

        when:
        def z = ZweigBuilder.toStatement([
                if:   true,
                then: "foo",
                else: "bar"
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "If statements' 'else' branch is optional"() {
        given:
        def target = new IfStatement(
                new BooleanExpression(ConstantExpression.TRUE),
                new ExpressionStatement(new ConstantExpression("foo")),
                EmptyStatement.INSTANCE
        )

        when:
        def z = ZweigBuilder.toStatement([
                if:   true,
                then: "foo"
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Methods definitions dispatch on the 'method' key"() {
        given:
        def target = new MethodNode(
                "someMethod",
                Modifier.PUBLIC | Modifier.STATIC,
                ClassHelper.make(Integer, false),
                [
                    new Parameter(ClassHelper.make(String, false), "foo"),
                    new Parameter(ClassHelper.make(Integer, false), "bar"),
                ] as Parameter[],
                [
                    ClassHelper.make(IOException, false)
                ] as ClassNode[],
                new BlockStatement([
                        new ExpressionStatement(
                                new ConstantExpression(5)
                        )
                ], new VariableScope())
        )

        when:
        def z = ZweigBuilder.toNode([
                method:     "someMethod",
                modifier:   ["public", "static"],
                arguments:  [[foo: String], [bar: Integer]],
                returnType: Integer,
                exceptions: [IOException],
                body:       [5]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Method calls dispatch on the 'call' key"() {
        given:
        def target = new MethodCallExpression(
                new VariableExpression("foo"),
                new ConstantExpression("bar"),
                new ArgumentListExpression([
                        new ConstantExpression(5)
                ])
        )

        when:
        def z = ZweigBuilder.toExpression([
                call: "bar",
                on:   [variable: "foo"],
                with: [5]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Static method calls dispatch on the 'callStatic' key"() {
        given:
        def target = new StaticMethodCallExpression(
                ClassHelper.make(String, false),
                "format",
                new ArgumentListExpression([
                        new ConstantExpression("foo")
                ])
        )

        when:
        def z = ZweigBuilder.toExpression([
                callStatic: "format",
                on:         String,
                with:       ["foo"]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Assignment dispatches on the 'set' key"() {
        given:
        def target = new BinaryExpression(
                new VariableExpression("foo"),
                new Token(Types.EQUALS, "=", -1, -1),
                new ConstantExpression(5)
        )

        when:
        def z = ZweigBuilder.toExpression([set: [variable: "foo"], to: 5])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Property access dispatches on the 'property' key"() {
        given:
        def target = new PropertyExpression(new VariableExpression("foo"), "bar")

        when:
        def z = ZweigBuilder.toExpression([property: "bar", of: [variable: "foo"]])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    class Dummy {
        def String foo
    }

    def "Field access dispatches on the 'field' key"() {
        given:
        def klass  = ClassHelper.make(Dummy, false)
        def target = new FieldExpression(
                klass.getDeclaredField("foo")
        )

        when:
        def z = ZweigBuilder.toExpression([field: "foo", of: Dummy])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    class Dummy2 extends Dummy {
        def String bar
    }

    def "Field access works with the inheritance chain"() {
        given:
        def klass  = ClassHelper.make(Dummy2, false)
        def target = new FieldExpression(
                klass.getField("foo")
        )

        when:
        def z = ZweigBuilder.toExpression([field: "foo", of: Dummy2])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Field nodes are constants"() {
        given:
        def klass  = ClassHelper.make(Dummy, false)
        def field  = klass.getField("foo")
        def target = new FieldExpression(field)

        expect:
        AstAssert.assertSyntaxTree(target, ZweigBuilder.toExpression(field))
    }

    def "Constructors dispatch on the 'constructor' key"() {
        given:
        def target = new ConstructorNode(
                Modifier.PUBLIC,
                [
                        new Parameter(ClassHelper.make(String, false), "foo"),
                        new Parameter(ClassHelper.make(Integer, false), "bar")
                ] as Parameter[],
                [ ClassHelper.make(IOException, false) ] as ClassNode[],
                new BlockStatement([
                        new ExpressionStatement(
                                new ConstructorCallExpression(
                                        ClassNode.SUPER,
                                        new ArgumentListExpression([
                                                new VariableExpression("bar"),
                                                new ConstantExpression(5)
                                        ])
                                )
                        )
                ], new VariableScope())
        )

        when:
        def z = ZweigBuilder.toNode([
                constructor: [[foo: String], [bar: Integer]],
                exceptions:  [IOException],
                body: [
                        [construct: ClassNode.SUPER,
                         with:      [[variable: "bar"], 5]]
                ]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Constructor calls dispatch on the 'construct' key"() {
        given:
        def target = new ConstructorCallExpression(
                ClassHelper.make(Integer, false),
                new ArgumentListExpression([
                        new ConstantExpression(5)
                ])
        )

        when:
        def z = ZweigBuilder.toExpression([
                construct: Integer,
                with:      [5]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)

    }

    /* toParameter */
    def "toParameter takes a map from name to class"() {
        given:
        def parameter = new Parameter(ClassHelper.make(String, false), "foo")

        when:
        def param = ZweigBuilder.withCategories { [foo: String].toParameter() }

        then:
        AstAssert.assertSyntaxTree(parameter, param)
    }

    def "toParameter is idempotent"() {
        given:
        def parameter = new Parameter(ClassHelper.make(String, false), "foo")

        expect:
        ZweigBuilder.withCategories {
            parameter.toParameter() instanceof Parameter
        }
    }

    /* toClassNode */
    def "toClassNode works on Classes"() {
        expect:
        AstAssert.assertSyntaxTree(
                ClassHelper.make(String, false),
                ZweigBuilder.withCategories { String.toClassNode() }
        )
    }

    def "toClassNode works on Strings"() {
        expect:
        AstAssert.assertSyntaxTree(
                ClassHelper.make(String, false),
                ZweigBuilder.withCategories { "java.lang.String".toClassNode() }
        )
    }

    def "toClassNode is idempotent"() {
        expect:
        ZweigBuilder.withCategories {
            ClassHelper.make(String, false).toClassNode() instanceof ClassNode
        }
    }

    /* toStatement */
    def "toStatement turns Expressions into Statements"() {
        expect:
        ZweigBuilder.withCategories {
            new ConstantExpression("foo").toStatement() instanceof Statement
        }
    }

    def "toStatement is idempotent"() {
        expect:
        ZweigBuilder.withCategories {
            new ExpressionStatement(
                    new ConstantExpression("foo")).
                    toStatement() instanceof Statement
        }
    }

    def "toStatement turns things in the domain of toExpression into Statements"() {
        expect:
        ZweigBuilder.withCategories {
            [variable: "foo"].toStatement() instanceof Statement
        }
    }

    /* toArgumentList */
    def "toArgumentList turns a list into an argument list"() {
        given:
        def target = new ArgumentListExpression([
                new ConstantExpression(5),
                new VariableExpression("foo")
        ])

        when:
        def z = ZweigBuilder.withCategories { [5, [variable: "foo"]].toArgumentList() }

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "toArgumentList is idempotent"() {
        given:
        def target = new ArgumentListExpression([])

        expect:
        ZweigBuilder.withCategories {
            target.toArgumentList() instanceof ArgumentListExpression
        }
    }

    def "toArgumentList on null is the empty argument list"() {
        expect:
        ZweigBuilder.withCategories {
            null.toArgumentList() == ArgumentListExpression.EMPTY_ARGUMENTS
        }
    }

    /* toModifier */
    def "toModifier takes Integers verbatim"() {
        given:
        def m = Modifier.PUBLIC | Modifier.STATIC

        expect:
        ZweigBuilder.withCategories {
            m.toModifier() == m
        }
    }

    def "toModifier translates Strings"() {
        expect:
        ZweigBuilder.withCategories {
            "public".toModifier() == Modifier.PUBLIC
        }
    }

    def "toModifier combines lists of modifiers"() {
        given:
        def m = Modifier.PUBLIC | Modifier.STATIC

        expect:
        ZweigBuilder.withCategories {
            ["public", Modifier.STATIC].toModifier() == m
        }
    }
}
