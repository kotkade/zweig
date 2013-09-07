package kotka.groovy.zweig

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import spock.lang.Specification

import java.lang.reflect.Modifier

class TestNodeCategory extends Specification {
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

    class Dummy {}

    def "Field nodes dispatch on the 'field' key"() {
        given:
        def target = new FieldNode(
                "foo",
                Modifier.STATIC | Modifier.PRIVATE,
                ClassHelper.STRING_TYPE,
                ClassHelper.make(Dummy),
                new ConstantExpression("bar")
        )

        when:
        def z = ZweigBuilder.toNode([
                field:    "foo",
                of:       Dummy,
                type:     String,
                modifier: ["static", "private"],
                init:     "bar"
        ])

        then:
        AstAssert.assertSyntaxTree(target, z)
    }
}
