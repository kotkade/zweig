package kotka.groovy.zweig

import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
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

    def "Variables are a map with a variable key"() {
        when:
        def z = new ZweigBuilder().fromSpec([variable: "x"])

        then:
        astOfTypeWithName z, VariableExpression, "x"
    }

    private boolean astOfTypeWithValue(underTest, klass, value) {
        assert underTest.class == klass
        assert underTest.value == value
        true
    }

    private boolean astOfTypeWithName(underTest, klass, name) {
        assert underTest.class == klass
        assert underTest.name  == name
        true
    }
}
