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

        when:
        z = new ZweigBuilder().fromSpec(1.5)

        then:
        z instanceof ConstantExpression
        z.value == 1.5
    }

    private boolean astOfTypeWithValue(underTest, klass, value) {
        assert underTest.class == klass
        assert underTest.value == value
        true
    }
}
