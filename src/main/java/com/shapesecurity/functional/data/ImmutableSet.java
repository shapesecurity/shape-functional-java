package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Unit;

import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
public class ImmutableSet<T> {
    @NotNull
    private final HashTable<T, Unit> data;

    public int length() {
        return this.data.length;
    }

    ImmutableSet(@NotNull HashTable<T, Unit> data) {
        this.data = data;
    }

    public static <T> ImmutableSet<T> emptyUsingEquality() {
        return new ImmutableSet<>(HashTable.emptyUsingEquality());
    }

    public static <T> ImmutableSet<T> emptyUsingIdentity() {
        return new ImmutableSet<>(HashTable.emptyUsingIdentity());
    }

    @Deprecated
    public static <T> ImmutableSet<T> empty() {
        return ImmutableSet.emptyUsingEquality();
    }

    @Deprecated
    public static <T> ImmutableSet<T> emptyP() {
        return ImmutableSet.emptyUsingIdentity();
    }

    public <B extends T> ImmutableSet<T> put(@NotNull B datum) {
        return new ImmutableSet<>(this.data.put(datum, Unit.unit));
    }

    @NotNull
    public <B extends T> ImmutableSet<T> putAll(@NotNull ImmutableList<B> list) {
        return list.foldLeft(ImmutableSet::put, this);
    }

    public boolean contains(@NotNull T datum) {
        return this.data.containsKey(datum);
    }

    public ImmutableSet<T> remove(@NotNull T datum) {
        return new ImmutableSet<>(this.data.remove(datum));
    }

    public <A> A foldAbelian(@NotNull F2<T, A, A> f, @NotNull A init) {
        return this.data.foldRight((p, acc) -> f.apply(p.left, acc), init);
    }

    public ImmutableSet<T> union(@NotNull ImmutableSet<T> other) {
        return new ImmutableSet<>(this.data.merge(other.data));
    }

    // Does not guarantee ordering of elements in resulting list.
    public ImmutableList<T> toList() {
        return this.foldAbelian((v, acc) -> acc.cons(v), ImmutableList.empty());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        return other instanceof ImmutableSet && this.data.length == ((ImmutableSet) other).data.length && this.data.foldLeft((memo, pair) -> memo && ((ImmutableSet) other).data.containsKey(pair.left), true);
    }
}
