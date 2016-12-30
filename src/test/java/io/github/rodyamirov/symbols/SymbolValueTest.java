package io.github.rodyamirov.symbols;

import io.github.rodyamirov.utils.Procedure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/30/16.
 */
public class SymbolValueTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private <T extends Throwable> void checkException(Procedure procedure, Class<T> exceptionClass, String errorMessage) {
        thrown.expect(exceptionClass);
        thrown.expectMessage(is(errorMessage));

        procedure.invoke(); // this is supposed to throw an exception

        thrown = ExpectedException.none();
    }

    @Test
    public void test1() {
        checkException(() -> {throw new IllegalStateException("ise");}, IllegalStateException.class, "ise");
    }
}
