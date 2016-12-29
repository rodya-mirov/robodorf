package io.github.rodyamirov.pascal.visitor;

import io.github.rodyamirov.pascal.TypeSpec;

/**
 * Created by richard.rast on 12/27/16.
 */
public class TypeCheckException extends IllegalStateException {
    public TypeCheckException(String errorMessage) {
        super(errorMessage);
    }

    public static TypeCheckException wrongValueClass(Class actual, Class intended) {
        String message = String.format(
                "Attempted to assign a value of type %s, but required a value of type %s",
                actual.toString(), intended.toString()
        );
        return new TypeCheckException(message);
    }

    public static TypeCheckException wrongValueClass(TypeSpec actual, TypeSpec intended) {
        String message = String.format(
                "Attempted to assign a value of type %s, but required a value of type %s",
                actual.toString(), intended.toString()
        );
        return new TypeCheckException(message);
    }

    public static TypeCheckException conversionImpossible(TypeSpec actual, TypeSpec intended) {
        String message = String.format(
                "Cannot convert a value of type %s to type %s",
                actual.name(), intended.name()
        );
        return new TypeCheckException(message);
    }
}
