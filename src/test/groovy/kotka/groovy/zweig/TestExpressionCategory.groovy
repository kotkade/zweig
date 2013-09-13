package kotka.groovy.zweig

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import spock.lang.Specification

class TestExpressionCategory extends Specification {
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

    def "Not expressions dispatch on the 'not' key"() {
        given:
        def target = new NotExpression(
            new BooleanExpression(new ConstantExpression(true))
        )

        when:
        def z = ZweigBuilder.toExpression([not: true])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Variables dispatch on the 'variable' key"() {
        when:
        def z = ZweigBuilder.toExpression([variable: "x"])

        then:
        AstAssert.assertSyntaxTree(new VariableExpression("x"), z)
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
}
