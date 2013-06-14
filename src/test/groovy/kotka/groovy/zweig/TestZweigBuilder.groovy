package kotka.groovy.zweig

import org.codehaus.groovy.ast.expr.ConstantExpression

import spock.lang.Specification

class TestZweigBuilder extends Specification {
    def "Numbers are constants"() {
        given:
        def z

        when:
        z = new ZweigBuilder().fromSpec(1)

        then:
        z instanceof ConstantExpression
        z.value == 1

        when:
        z = new ZweigBuilder().fromSpec(1.5)

        then:
        z instanceof ConstantExpression
        z.value == 1.5
    }

    def "Strings are constants"() {
        given:
        def z = new ZweigBuilder().fromSpec("foo")

        expect:
        z instanceof ConstantExpression
        z.value == "foo"
    }
}
