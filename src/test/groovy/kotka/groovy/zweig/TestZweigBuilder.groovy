package kotka.groovy.zweig

import org.codehaus.groovy.ast.expr.ConstantExpression

import spock.lang.Specification

class TestZweigBuilder extends Specification {
    def "Strings and Numbers are constants"() {
        when:
        def z = new ZweigBuilder().fromSpec(value)

        then:
        astOfTypeWithValue z, ConstantExpression, value

        where:
        value << [ 1, 1.5, "foo" ]
    }

    def "List are compatible with fromSpec"() {
        when:
        def z = new ZweigBuilder().fromSpec([1, 2, 3])

        then:
        z instanceof List
        astOfTypeWithValue z[0], ConstantExpression, 1
        astOfTypeWithValue z[1], ConstantExpression, 2
        astOfTypeWithValue z[2], ConstantExpression, 3
    }

    private boolean astOfTypeWithValue(underTest, klass, value) {
        assert underTest.class == klass
        assert underTest.value == value
        true
    }
}
