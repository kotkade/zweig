package kotka.groovy.zweig

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import spock.lang.Specification

import java.lang.reflect.Modifier

class TestZweigBuilder extends Specification {
    def "Strings and Numbers are constants"() {
        when:
        def z = new ZweigBuilder().fromSpec(value)

        then:
        AstAssert.assertSyntaxTree(new ConstantExpression(value), z)

        where:
        value << [ 1, 1.5, "foo" ]
    }

    def "Classes are constants"() {
        when:
        def z = new ZweigBuilder().fromSpec(String)

        then:
        AstAssert.assertSyntaxTree(
                new ClassExpression(ClassHelper.make(String, false)),
                z)
    }

    def "List are constants"() {
        given:
        def target = new ListExpression([
            new ConstantExpression(1),
            new ConstantExpression(2),
            new ConstantExpression(3)
        ])

        when:
        def z = new ZweigBuilder().fromSpec([1, 2, 3])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Variables are a map with a variable key"() {
        when:
        def z = new ZweigBuilder().fromSpec([variable: "x"])

        then:
        AstAssert.assertSyntaxTree(new VariableExpression("x"), z)
    }

    def "Methods are specified as maps"() {
        given:
        def target = new MethodNode(
                "someMethod",
                Modifier.PUBLIC,
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
        def z = new ZweigBuilder().fromSpec([
                method:     "someMethod",
                arguments:  [[foo: String], [bar: Integer]],
                returnType: Integer,
                exceptions: [IOException],
                body:       [5]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "Method calls are specified as maps"() {
        given:
        def target = new MethodCallExpression(
                new VariableExpression("foo"),
                new ConstantExpression("bar"),
                new ArgumentListExpression([
                        new ConstantExpression(5)
                ])
        )

        when:
        def z = new ZweigBuilder().fromSpec([
                call: "bar",
                on:   [variable: "foo"],
                with: [5]
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    /* toParameter */
    def "toParameter takes a map from name to class"() {
        given:
        def parameter = new Parameter(ClassHelper.make(String, false), "foo")

        when:
        def param = withCategory { [foo: String].toParameter() }

        then:
        AstAssert.assertSyntaxTree(parameter, param)
    }

    def "toParameter is idempotent"() {
        given:
        def parameter = new Parameter(ClassHelper.make(String, false), "foo")

        expect:
        withCategory {
            parameter.toParameter() instanceof Parameter
        }
    }

    /* toClassNode */
    def "toClassNode works on Classes"() {
        expect:
        withCategory {
            String.toClassNode() instanceof ClassNode
        }
    }

    def "toClassNode is idempotent"() {
        expect:
        withCategory {
            ClassHelper.make(String, false).toClassNode() instanceof ClassNode
        }
    }

    /* toStatement */
    def "toStatement turns Expressions into Statements"() {
        expect:
        withCategory {
            new ConstantExpression("foo").toStatement() instanceof Statement
        }
    }

    def "toStatement is idempotent"() {
        expect:
        withCategory {
            new ExpressionStatement(
                    new ConstantExpression("foo")).
                    toStatement() instanceof Statement
        }
    }

    def "toStatement turns things in the domain of toZweig into Statements"() {
        expect:
        withCategory {
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
        def z = withCategory { [5, [variable: "foo"]].toArgumentList() }

        then:
        AstAssert.assertSyntaxTree(target, z)
    }

    def "toArgumentList is idempotent"() {
        given:
        def target = new ArgumentListExpression([])

        expect:
        withCategory {
            target.toArgumentList() instanceof ArgumentListExpression
        }
    }

    /* Helper */
    static withCategory(Closure body) {
        use(ZweigBuilderCategory) { body() }
    }
}
