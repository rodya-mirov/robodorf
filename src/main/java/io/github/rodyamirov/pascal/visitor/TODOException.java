package io.github.rodyamirov.pascal.visitor;

/**
 * Placeholder exception to allow compilation and testing even when functionality is not yet
 * complete. Also, "Find Usages" is a pretty great way to find what I haven't gotten around
 * to doing yet.
 *
 * Created by richard.rast on 12/28/16.
 */
public class TODOException extends RuntimeException {
    public TODOException(String message) {
        super(message);
    }

    public static TODOException make() {
        return new TODOException("This functionality not yet implemented.");
    }
}
