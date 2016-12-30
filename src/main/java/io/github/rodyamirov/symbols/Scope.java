package io.github.rodyamirov.symbols;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by richard.rast on 12/30/16.
 */
public final class Scope {
    public final Optional<Scope> parentScope;
    public final String immediateScopeName;

    public Scope(String immediateScopeName) {
        this.parentScope = Optional.empty();
        this.immediateScopeName = immediateScopeName;
    }

    public Scope(String immediateScopeName, Scope parent) {
        this.parentScope = Optional.of(parent);
        this.immediateScopeName = immediateScopeName;
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

        sb.append(immediateScopeName);

        return sb.toString();
    }
}
