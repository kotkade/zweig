package kotka.groovy.zweig

import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import spock.lang.Specification

class TestStatementCategory extends Specification {
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
}
