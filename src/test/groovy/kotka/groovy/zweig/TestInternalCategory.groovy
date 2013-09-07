package kotka.groovy.zweig

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import spock.lang.Specification

import java.lang.reflect.Modifier

class TestInternalCategory extends Specification {
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

    def "toModifier defaults to public"() {
        expect:
        ZweigBuilder.withCategories {
            null.toModifier() == Modifier.PUBLIC
        }
    }

    /* toBlockStatement */
    def "toBlockStatement is idempotent"() {
        given:
        def target = new BlockStatement(
            [new ExpressionStatement(new ConstantExpression(null))],
            new VariableScope()
        )
        def z

        when:
        ZweigBuilder.withCategories {
            z = target.toBlockStatement()
        }

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "toBlockStatement turns lists into block statements"() {
        given:
        def target = new BlockStatement(
            [new ExpressionStatement(new ConstantExpression(null)),
             new ExpressionStatement(new ConstantExpression(null))],
            new VariableScope()
        )
        def z

        when:
        ZweigBuilder.withCategories {
            z = [null, null].toBlockStatement()
        }

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "toBlockStatement turns everything else into a one statement block"() {
        given:
        def target = new BlockStatement(
            [new ExpressionStatement(new ConstantExpression(null))],
            new VariableScope()
        )
        def z

        when:
        ZweigBuilder.withCategories {
            z = null.toBlockStatement()
        }

        then:
        AstAssert.assertSyntaxTree(target, z)
    }
}
