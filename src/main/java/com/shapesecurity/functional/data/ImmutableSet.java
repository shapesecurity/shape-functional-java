package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Unit;

import org.jetbrains.annotations.NotNull;

public class ImmutableSet<T> {
    @NotNull
    private final HashTable<T, Unit> data;

    private ImmutableSet(@NotNull HashTable<T, Unit> data) {
        this.data = data;
    }

    public static <T> ImmutableSet<T> empty() { // object equality (.equals)
        return new ImmutableSet<>(HashTable.empty());
    }

    public static <T> ImmutableSet<T> emptyP() { // object identity (==)
        return new ImmutableSet<>(HashTable.emptyP());
    }

    public ImmutableSet<T> put(@NotNull T datum) {
        return new ImmutableSet<>(this.data.put(datum, Unit.unit));
    }

    public boolean contains(@NotNull T datum) {
        return this.data.containsKey(datum);
    }

    public ImmutableSet<T> remove(@NotNull T datum) {
        return new ImmutableSet<>(this.data.remove(datum));
    }

    public <A> A foldAbelian(@NotNull F2<T, A, A> f, @NotNull A init) {
        return this.data.foldRight((p, acc) -> f.apply(p.a, acc), init);
    }

    public ImmutableSet<T> union(@NotNull ImmutableSet<T> other) {
        return new ImmutableSet<>(this.data.merge(other.data));
    }

    // Does not guarantee ordering of elements in resulting list.
    public ImmutableList<T> toList() {
        return this.foldAbelian((v, acc) -> acc.cons(v), ImmutableList.nil());
    }
}
