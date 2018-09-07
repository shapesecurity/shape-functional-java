package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Unit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.Iterator;

@CheckReturnValue
public class ImmutableSet<T> implements Iterable<T> {
    @Nonnull
    private final HashTable<T, Unit> data;

    public int length() {
        return this.data.length;
    }

    ImmutableSet(@Nonnull HashTable<T, Unit> data) {
        this.data = data;
    }

    public static <T> ImmutableSet<T> empty(@Nonnull Hasher<T> hasher) {
        return new ImmutableSet<>(HashTable.empty(hasher));
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

    public <B extends T> ImmutableSet<T> put(@Nonnull B datum) {
        return new ImmutableSet<>(this.data.put(datum, Unit.unit));
    }

    @Nonnull
    public <B extends T> ImmutableSet<T> putAll(@Nonnull ImmutableList<B> list) {
        return list.foldLeft(ImmutableSet::put, this);
    }

    public boolean contains(@Nonnull T datum) {
        return this.data.containsKey(datum);
    }

    public <A> ImmutableSet<A> map(@Nonnull F<T, A> f) {
        @SuppressWarnings("unchecked")
        ImmutableSet<A> newSet = ImmutableSet.empty((Hasher<A>)this.data.hasher);
        newSet = this.data.foldLeft((ImmutableSet<A> a, Pair<T, Unit> b) -> a.put(f.apply(b.left)), newSet);
        return newSet;
    }

    public ImmutableSet<T> remove(@Nonnull T datum) {
        return new ImmutableSet<>(this.data.remove(datum));
    }

    public <A> A foldAbelian(@Nonnull F2<T, A, A> f, @Nonnull A init) {
        return this.data.foldRight((p, acc) -> f.apply(p.left, acc), init);
    }

    public ImmutableSet<T> union(@Nonnull ImmutableSet<T> other) {
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

    @Override
    public Iterator<T> iterator() {
        final Iterator<Pair<T, Unit>> mapIterator = this.data.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return mapIterator.hasNext();
            }

            @Override
            public T next() {
                return mapIterator.next().left;
            }
        };
    }

}
