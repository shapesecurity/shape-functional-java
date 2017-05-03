package com.shapesecurity.functional.data;

import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;

public final class FreePairingMonoid<A, B> implements Monoid<Pair<A, B>> {
    @Nonnull
    private final Monoid<A> monoidA;
    @Nonnull
    private final Monoid<B> monoidB;

    public FreePairingMonoid(@Nonnull Monoid<A> monoidA, @Nonnull Monoid<B> monoidB) {
        this.monoidA = monoidA;
        this.monoidB = monoidB;
    }

    @Nonnull
    @Override
    public Pair<A, B> identity() {
        return new Pair<>(this.monoidA.identity(), this.monoidB.identity());
    }

    @Nonnull
    @Override
    public Pair<A, B> append(Pair<A, B> a, Pair<A, B> b) {
        return new Pair<>(this.monoidA.append(a.left, b.left), this.monoidB.append(a.right, b.right));
    }
}
