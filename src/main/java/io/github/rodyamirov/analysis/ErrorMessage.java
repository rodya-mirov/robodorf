package io.github.rodyamirov.analysis;

import io.github.rodyamirov.tree.SyntaxTree;

import java.util.Objects;

/**
 * Created by richard.rast on 1/2/17.
 */
public class ErrorMessage {
    public final String message;
    public final SyntaxTree problemNode;

    public ErrorMessage(String message, SyntaxTree problemNode) {
        this.message = message;
        this.problemNode = problemNode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ErrorMessage)) {
            return false;
        }

        ErrorMessage other = (ErrorMessage)o;

        return Objects.equals(this.message, other.message)
                && Objects.equals(this.problemNode, other.problemNode);
    }

    @Override
    public int hashCode() {
        return 43 * Objects.hashCode(message) + Objects.hashCode(problemNode);
    }
}
