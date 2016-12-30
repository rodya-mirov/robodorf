package io.github.rodyamirov.symbols;

import io.github.rodyamirov.exceptions.TypeCheckException;
import io.github.rodyamirov.utils.Procedure;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by richard.rast on 12/30/16.
 */
public class SymbolValueTest {
    private void checkException(Procedure procedure, Class exceptionClass, String errorMessage) {
        try {
            procedure.invoke();
            assertThat("This shouldn't happen", true, is(false));
        } catch (Exception thrown) {
            assertThat("Class is correct", thrown, isA(exceptionClass));
            assertThat("Message is correct", thrown.getMessage(), is(errorMessage));
        }
    }

    @Test
    public void symbolValueTest1() {
        TypeSpec typeSpec;
        SymbolValue symbolValue, symbolValue2;

        typeSpec = TypeSpec.BOOLEAN;

        symbolValue = SymbolValue.make(typeSpec, true);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(true));

        symbolValue2 = SymbolValue.make(typeSpec, true);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, false);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, false);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(false));

        symbolValue2 = SymbolValue.make(typeSpec, false);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, true);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type BOOLEAN"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.INTEGER, true),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Boolean, but required a value of type class java.lang.Integer"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.BOOLEAN, 1317),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Integer, but required a value of type class java.lang.Boolean"
        );
    }

    @Test
    public void symbolValueTest2() {
        TypeSpec typeSpec;
        SymbolValue symbolValue, symbolValue2;

        typeSpec = TypeSpec.INTEGER;

        symbolValue = SymbolValue.make(typeSpec, 12);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(12));

        symbolValue2 = SymbolValue.make(typeSpec, 12);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 15);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, -611);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(-611));

        symbolValue2 = SymbolValue.make(typeSpec, -611);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 13480);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type INTEGER"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.INTEGER, true),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Boolean, but required a value of type class java.lang.Integer"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.BOOLEAN, 1317),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Integer, but required a value of type class java.lang.Boolean"
        );
    }

    @Test
    public void symbolValueTest3() {
        TypeSpec typeSpec;
        SymbolValue symbolValue, symbolValue2;

        typeSpec = TypeSpec.REAL;

        symbolValue = SymbolValue.make(typeSpec, 12.0f);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(12.0f));

        symbolValue2 = SymbolValue.make(typeSpec, 12.0f);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 15.0f);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        symbolValue = SymbolValue.make(typeSpec, -611.0f);
        assertThat(symbolValue.typeSpec, is(typeSpec));
        assertThat(symbolValue.value, is(-611.0f));

        symbolValue2 = SymbolValue.make(typeSpec, -611.0f);
        assertThat("Equals works", symbolValue, is(symbolValue2));
        symbolValue2 = SymbolValue.make(typeSpec, 13480.0f);
        assertThat("Equals works", symbolValue, is(not(symbolValue2)));

        checkException(
                () -> SymbolValue.make(typeSpec, null),
                TypeCheckException.class,
                "Cannot assign a value of null for type REAL"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.REAL, 12),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Integer, but required a value of type class java.lang.Float"
        );

        checkException(
                () -> SymbolValue.make(TypeSpec.INTEGER, 1317.0f),
                TypeCheckException.class,
                "Attempted to assign a value of type class java.lang.Float, but required a value of type class java.lang.Integer"
        );
    }
}
