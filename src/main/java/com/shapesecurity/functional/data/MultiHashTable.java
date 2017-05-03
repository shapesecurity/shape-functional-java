package com.shapesecurity.functional.data;


import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;

import javax.annotation.CheckReturnValue;

// Map from keys to multiple values.
// This class does not distinguish between "key is present, but associated with empty list" and "key is not present". If you need that, don't use this class.
@CheckReturnValue
public class MultiHashTable<K, V> { // TODO should be elsewhere... and better
    @Nonnull
    private final HashTable<K, ImmutableList<V>> data;

    private MultiHashTable(@Nonnull HashTable<K, ImmutableList<V>> data) {
        this.data = data;
    }

    @Nonnull
    public static <K, V> MultiHashTable<K, V> emptyUsingEquality() {
        return new MultiHashTable<>(HashTable.emptyUsingEquality());
    }

    @Nonnull
    public static <K, V> MultiHashTable<K, V> emptyUsingIdentity() {
        return new MultiHashTable<>(HashTable.emptyUsingIdentity());
    }

    @Nonnull
    @Deprecated
    public static <K, V> MultiHashTable<K, V> empty() {
        return MultiHashTable.emptyUsingEquality();
    }

    @Nonnull
    @Deprecated
    public static <K, V> MultiHashTable<K, V> emptyP() {
        return MultiHashTable.emptyUsingIdentity();
    }

    @Nonnull
    public MultiHashTable<K, V> put(@Nonnull K key, @Nonnull V value) {
        return new MultiHashTable<>(this.data.put(key, ImmutableList.cons(value, this.data.get(key).orJust(ImmutableList.empty()))));
    }

    @Nonnull
    public MultiHashTable<K, V> remove(@Nonnull K key) {
        return new MultiHashTable<>(this.data.remove(key));
    }

    @Nonnull
    public ImmutableList<V> get(@Nonnull K key) {
        return this.data.get(key).orJust(ImmutableList.empty());
    }

    @Nonnull
    public MultiHashTable<K, V> merge(@Nonnull MultiHashTable<K, V> tree) { // default merge strategy: append lists.
        return this.merge(tree, ImmutableList::append);
    }

    @Nonnull
    public MultiHashTable<K, V> merge(@Nonnull MultiHashTable<K, V> tree, @Nonnull F2<ImmutableList<V>, ImmutableList<V>, ImmutableList<V>> merger) {
        return new MultiHashTable<>(this.data.merge(tree.data, merger));
    }

    @Nonnull
    public ImmutableList<Pair<K, ImmutableList<V>>> entries() {
        return this.data.entries();
    }

    // version: key is irrelevant
    @Nonnull
    public <B> HashTable<K, B> toHashTable(@Nonnull F<ImmutableList<V>, B> conversion) {
        //return this.data.foldLeft((acc, p) -> acc.put(p.a, conversion.apply(p.b)), HashTable.empty(this.data.hasher));
        return this.toHashTable((k, vs) -> conversion.apply(vs));
    }

    // version: key is used
    @Nonnull
    public <B> HashTable<K, B> toHashTable(@Nonnull F2<K, ImmutableList<V>, B> conversion) {
        return this.data.foldLeft((acc, p) -> acc.put(p.left, conversion.apply(p.left, p.right)), HashTable.empty(this.data.hasher));
    }

    @Nonnull
    public final ImmutableList<ImmutableList<V>> values() {
        return this.data.foldLeft((acc, p) -> acc.cons(p.right), ImmutableList.empty());
    }

    @Nonnull
    public final ImmutableList<V> gatherValues() {
        return this.data.foldLeft((acc, p) -> acc.append(p.right), ImmutableList.empty());
    }

    @Nonnull
    public final <B> MultiHashTable<K, B> mapValues(@Nonnull F<V, B> f) {
        return new MultiHashTable<>(this.data.map(l -> l.map(f)));
    }
}
