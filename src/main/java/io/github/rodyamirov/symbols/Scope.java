package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by richard.rast on 12/30/16.
 */
public final class Scope {
    public final Optional<Scope> parentScope;
    public final Token<String> immediateScopeName;

    private Scope(Token<String> immediateScopeName, Optional<Scope> parentScope) {
        this.immediateScopeName = immediateScopeName;
        this.parentScope = parentScope;
    }

    public static Scope makeRootScope(Token<String> immediateScopeName) {
        return new Scope(immediateScopeName, Optional.empty());
    }

    public Scope makeChildScope(Token<String> childScopeName) {
        return new Scope(childScopeName, Optional.of(this));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Scope)) {
            return false;
        }

        Scope other = (Scope)o;

        return Objects.equals(this.immediateScopeName, other.immediateScopeName)
                && Objects.equals(this.parentScope, other.parentScope);
    }

    @Override
    public int hashCode() {
        return immediateScopeName.hashCode() * 43 + parentScope.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        parentScope.ifPresent(ps -> {
            sb.append(ps.toString());
            sb.append(".");
        });

        sb.append(immediateScopeName.value);

        return sb.toString();
    }
}
