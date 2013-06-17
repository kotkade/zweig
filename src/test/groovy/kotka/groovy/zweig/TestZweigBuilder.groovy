package kotka.groovy.zweig

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import spock.lang.Specification

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

    def "List are compatible with fromSpec"() {
        when:
        def z = new ZweigBuilder().fromSpec([1, 2, 3])

        then:
        z instanceof List
        AstAssert.assertSyntaxTree(new ConstantExpression(1), z[0])
        AstAssert.assertSyntaxTree(new ConstantExpression(2), z[1])
        AstAssert.assertSyntaxTree(new ConstantExpression(3), z[2])
    }

    def "Variables are a map with a variable key"() {
        when:
        def z = new ZweigBuilder().fromSpec([variable: "x"])

        then:
        AstAssert.assertSyntaxTree(new VariableExpression("x"), z)
    }


    /* toClassNode */
    def "toClassNode works on Classes and is idempotent"() {
        given:
        def klass = String
        def klassNode = ClassHelper.make(klass, false)

        when:
        def z     = withCategory { klass.toClassNode() }
        def zNode = withCategory { klassNode.toClassNode() }

        then:
        AstAssert.assertSyntaxTree(klassNode, z)
        AstAssert.assertSyntaxTree(klassNode, zNode)
    }

    static withCategory(Closure body) {
        use(ZweigBuilderCategory) { body() }
    }
}
