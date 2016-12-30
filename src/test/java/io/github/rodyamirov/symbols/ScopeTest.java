package io.github.rodyamirov.symbols;

import io.github.rodyamirov.lex.Token;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by richard.rast on 12/30/16.
 */
public class ScopeTest {
    @Test
    public void rootTest() {
        Scope root = Scope.makeRootScope(Token.ID("idk"));

        assertThat(root.parentScope, is(Optional.empty()));
        assertThat(root.immediateScopeName, is(Token.ID("idk")));

        Scope root2 = Scope.makeRootScope(Token.ID("idk"));

        assertThat(root2.parentScope, is(Optional.empty()));
        assertThat(root2.immediateScopeName, is(Token.ID("idk")));

        assertThat(root, is(root2));

        Scope root3 = Scope.makeRootScope(Token.ID("idk1"));

        assertThat(root3.parentScope, is(Optional.empty()));
        assertThat(root3.immediateScopeName, is(Token.ID("idk1")));

        assertThat(root, is(not(root3)));
        assertThat(root2, is(not(root3)));
    }

    @Test
    public void childTest() {
        Scope root1, root2, child1, child2, child3;

        root1 = Scope.makeRootScope(Token.ID("root"));
        root2 = Scope.makeRootScope(Token.ID("root"));

        child1 = root1.makeChildScope(Token.ID("child"));
        child2 = root2.makeChildScope(Token.ID("child"));

        assertThat(child1, is(child2));
        assertThat(child1.immediateScopeName, is(Token.ID("child")));
        assertThat(child2.immediateScopeName, is(Token.ID("child")));

        assertThat(child1.parentScope.get(), is(root1));
        assertThat(child1.parentScope.get(), is(root2)); // swap intentional
        assertThat(child2.parentScope.get(), is(root1));
        assertThat(child2.parentScope.get(), is(root2));

        root2 = Scope.makeRootScope(Token.ID("otherRoot"));
        child3 = root2.makeChildScope(Token.ID("child"));
        assertThat(child3.parentScope.get(), is(root2));
        assertThat(child3.immediateScopeName, is(Token.ID("child")));
        assertThat(child3, is(not(child1)));

        child2 = root2.makeChildScope(Token.ID("otherChild"));
        assertThat(child3, is(not(child2)));
    }
}
