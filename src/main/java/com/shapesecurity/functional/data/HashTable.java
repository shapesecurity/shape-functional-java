/*
 * Copyright 2014 Shape Security, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shapesecurity.functional.data;

import com.shapesecurity.functional.Effect;
import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Unit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * An immutable hash trie tree implementation.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
@CheckReturnValue
public abstract class HashTable<K, V> implements Iterable<Pair<K, V>> {
    private final static Hasher<Object> EQUALITY_HASHER = new Hasher<Object>() {
        @Override
        public int hash(@Nonnull Object data) {
            return data.hashCode();
        }

        @Override
        public boolean eq(@Nonnull Object o, @Nonnull Object b) {
            return o.equals(b);
        }
    };

    public final static Hasher<Object> IDENTITY_HASHER = new Hasher<Object>() {
        @Override
        public int hash(@Nonnull Object data) {
            return System.identityHashCode(data);
        }

        @Override
        public boolean eq(@Nonnull Object o, @Nonnull Object b) {
            return o == b;
        }
    };

    @Nonnull
    public final Hasher<K> hasher;
    public final int length;

    protected HashTable(@Nonnull Hasher<K> hasher, int length) {
        super();
        this.hasher = hasher;
        this.length = length;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <K> Hasher<K> equalityHasher() {
        return (Hasher<K>) EQUALITY_HASHER;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Deprecated
    public static <K> Hasher<K> defaultHasher() {
        return HashTable.equalityHasher();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <K> Hasher<K> identityHasher() {
        return (Hasher<K>) IDENTITY_HASHER;
    }

    @Nonnull
    public static <K, V> HashTable<K, V> empty(@Nonnull Hasher<K> hasher) {
        return new Empty<>(hasher);
    }

    @Nonnull
    public static <K, V> HashTable<K, V> emptyUsingEquality() {
        return empty(HashTable.equalityHasher());
    }

    @Nonnull
    public static <K, V> HashTable<K, V> emptyUsingIdentity() {
        return empty(HashTable.identityHasher());
    }

    @Nonnull
    @Deprecated
    public static <K, V> HashTable<K, V> empty() {
        return HashTable.emptyUsingEquality();
    }

    @Nonnull
    @Deprecated
    public static <K, V> HashTable<K, V> emptyP() {
        return HashTable.emptyUsingIdentity();
    }

    @Nonnull
    public static <K, V> HashTable<K, V> fromUsingEquality(@Nonnull Map<K, V> map) {
        return HashTable.<K, V>emptyUsingEquality().putAllFrom(map);
    }

    @Nonnull
    public static <K, V> HashTable<K, V> fromUsingIdentity(@Nonnull IdentityHashMap<K, V> map) {
        return HashTable.<K, V>emptyUsingIdentity().putAllFrom(map);
    }

    @Nonnull
    public static <K, V> HashTable<K, V> from(@Nonnull Hasher<K> hasher, @Nonnull Map<K, V> map) {
        return HashTable.<K, V>empty(hasher).putAllFrom(map);
    }

    @Nonnull
    public final HashMap<K, V> toHashMap() {
        if (!this.hasher.equals(HashTable.equalityHasher())) {
            throw new UnsupportedOperationException("HashTable::toHashMap requires an equality hasher.");
        }
        HashMap<K, V> map = new HashMap<>();
        for (Pair<K, V> pair : this) {
            map.put(pair.left, pair.right);
        }
        return map;
    }

    @Nonnull
    public final IdentityHashMap<K, V> toIdentityHashMap() {
        if (!this.hasher.equals(HashTable.identityHasher())) {
            throw new UnsupportedOperationException("HashTable::toIdentityHashMap requires an identity hasher.");
        }
        IdentityHashMap<K, V> map = new IdentityHashMap<>();
        for (Pair<K, V> pair : this) {
            map.put(pair.left, pair.right);
        }
        return map;
    }

    @Nonnull
    public static <K, V> HashTable<K, V> fromUsingEquality(@Nonnull ImmutableList<Pair<K, V>> list) {
        return HashTable.<K, V>emptyUsingEquality().putAll(list);
    }

    @Nonnull
    public static <K, V> HashTable<K, V> fromUsingIdentity(@Nonnull ImmutableList<Pair<K, V>> list) {
        return HashTable.<K, V>emptyUsingIdentity().putAll(list);
    }

    @Nonnull
    public static <K, V> HashTable<K, V> from(@Nonnull Hasher<K> hasher, @Nonnull ImmutableList<Pair<K, V>> list) {
        return HashTable.<K, V>empty(hasher).putAll(list);
    }

    @Nonnull
    public final HashTable<K, V> put(@Nonnull K key, @Nonnull V value) {
        return this.put(key, value, this.hasher.hash(key));
    }

    @Nonnull
    public final HashTable<K, V> put(@Nonnull Pair<K, V> pair) {
        return this.put(pair.left, pair.right);
    }

    @Nonnull
    public final HashTable<K, V> putAll(@Nonnull ImmutableList<Pair<K, V>> pairs) {
        return pairs.foldLeft((acc, pair) -> acc.put(pair.left, pair.right), this);
    }

    @Nonnull
    public final HashTable<K, V> remove(@Nonnull K key) {
        return this.remove(key, this.hasher.hash(key)).orJust(this);
    }

    @Nonnull
    protected abstract HashTable<K, V> put(@Nonnull K key, @Nonnull V value, int hash);

    @Nonnull
    protected abstract Maybe<HashTable<K, V>> remove(@Nonnull K key, int hash);

    @Nonnull
    public final Maybe<V> get(@Nonnull K key) {
        return this.get(key, this.hasher.hash(key));
    }

    @Nonnull
    protected abstract Maybe<V> get(@Nonnull K key, int hash);

    @SuppressWarnings("unchecked")
    @Nonnull
    public final HashTable<K, V> merge(@Nonnull HashTable<K, V> tree) {
        return this.merge(tree, (a, b) -> b);
    }

    @Nonnull
    public final HashTable<K, V> putAllFrom(@Nonnull Map<K, V> map) {
        HashTable<K, V> table = this;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            table = table.put(entry.getKey(), entry.getValue());
        }
        return table;
    }

    @Nonnull
    public abstract HashTable<K, V> merge(@Nonnull HashTable<K, V> tree, @Nonnull F2<V, V, V> merger);

    @Nonnull
    public abstract <A> A foldLeft(@Nonnull F2<A, Pair<K, V>, A> f, @Nonnull A init);

    @Nonnull
    public abstract <A> A foldRight(@Nonnull F2<Pair<K, V>, A, A> f, @Nonnull A init);

    @Nonnull
    public ImmutableList<Pair<K, V>> entries() {
        //noinspection unchecked
        Pair<K, V>[] pairs = ((Pair<K, V>[]) new Pair[this.length]);
        int[] i = new int[1];
        this.forEach(x -> pairs[i[0]++] = x);
        return ImmutableList.from(pairs);
    }

    public final void foreach(@Nonnull Effect<Pair<K, V>> e) {
        this.forEach(e::e);
    }

    public abstract void forEach(@Nonnull Consumer<? super Pair<K, V>> e);

    @Nonnull
    public abstract Maybe<Pair<K, V>> find(@Nonnull F<Pair<K, V>, Boolean> f);

    @Nonnull
    public abstract <R> Maybe<R> findMap(@Nonnull F<Pair<K, V>, Maybe<R>> f);

    public abstract <B> HashTable<K, B> map(@Nonnull F<V, B> f);

    public boolean containsKey(@Nonnull K key) {
        return this.containsKey(key, this.hasher.hash(key));
    }

    public abstract boolean containsKey(@Nonnull K key, int hash);

    public boolean containsValue(@Nonnull V value) {
        return this.find(p -> p.right == value).isJust();
    }

    @Nonnull
    public ImmutableSet<K> keys() {
        return new ImmutableSet<>(this.map(F.constant(Unit.unit)));
    }

    /**
     * An empty hash table.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    private final static class Empty<K, V> extends HashTable<K, V> {
        protected Empty(@Nonnull Hasher<K> hasher) {
            super(hasher, 0);
        }

        @Nonnull
        @Override
        protected HashTable<K, V> put(@Nonnull K key, @Nonnull V value, int hash) {
            return new Leaf<>(this.hasher, ImmutableList.of(new Pair<>(key, value)), hash, 1);
        }

        @Nonnull
        @Override
        protected Maybe<HashTable<K, V>> remove(@Nonnull K key, int hash) {
            return Maybe.empty();
        }

        @Nonnull
        @Override
        protected Maybe<V> get(@Nonnull K key, int hash) {
            return Maybe.empty();
        }

        @Nonnull
        @Override
        public HashTable<K, V> merge(@Nonnull HashTable<K, V> tree, @Nonnull F2<V, V, V> merger) {
            return tree;
        }

        @Nonnull
        @Override
        public <A> A foldLeft(@Nonnull F2<A, Pair<K, V>, A> f, @Nonnull A init) {
            return init;
        }

        @Nonnull
        @Override
        public <A> A foldRight(@Nonnull F2<Pair<K, V>, A, A> f, @Nonnull A init) {
            return init;
        }

        @Override
        public Iterator<Pair<K, V>> iterator() {
            return new Iterator<Pair<K, V>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Pair<K, V> next() {
                    throw new NoSuchElementException();
                }
            };
        }

        @Override
        public void forEach(@Nonnull Consumer<? super Pair<K, V>> e) {

        }

        @Nonnull
        @Override
        public Maybe<Pair<K, V>> find(@Nonnull F<Pair<K, V>, Boolean> f) {
            return Maybe.empty();
        }

        @Nonnull
        @Override
        public <R> Maybe<R> findMap(@Nonnull F<Pair<K, V>, Maybe<R>> f) {
            return Maybe.empty();
        }

        @Override
        public <B> HashTable<K, B> map(@Nonnull F<V, B> f) {
            return empty(this.hasher);
        }

        @Override
        public boolean containsKey(@Nonnull K key, int hash) {
            return false;
        }
    }

    /**
     * A leaf node that contains a list of pairs where all the keys have exactly the same hash
     * code.
     *
     * @param <K> Key type
     * @param <V> Value type
     */
    private final static class Leaf<K, V> extends HashTable<K, V> {
        @Nonnull
        private final ImmutableList<Pair<K, V>> dataList;
        public int baseHash;

        protected Leaf(@Nonnull Hasher<K> hasher, @Nonnull ImmutableList<Pair<K, V>> dataList, int baseHash, int length) {
            super(hasher, length);
            this.dataList = dataList;
            this.baseHash = baseHash;
        }

        @Nonnull
        @Override
        protected HashTable<K, V> put(@Nonnull final K key, @Nonnull final V value, final int hash) {
            if (hash == this.baseHash) {
                Pair<Boolean, ImmutableList<Pair<K, V>>> result = this.dataList.mapAccumL((found, kvPair) -> {
                    if (found) {
                        return new Pair<>(true, kvPair);
                    }
                    if (Leaf.this.hasher.eq(kvPair.left, key)) {
                        return new Pair<>(true, new Pair<>(key, value));
                    }
                    return new Pair<>(false, kvPair);
                }, false);
                if (result.left) {
                    return new Leaf<>(this.hasher, result.right, hash, this.length);
                }
                return new Leaf<>(this.hasher, this.dataList.cons(new Pair<>(key, value)), hash, this.length + 1);
            }
            return this.toFork().put(key, value, hash);
        }

        @Nonnull
        @Override
        protected Maybe<HashTable<K, V>> remove(@Nonnull final K key, int hash) {
            if (this.baseHash != hash) {
                return Maybe.empty();
            }
            Pair<Boolean, ImmutableList<Pair<K, V>>> result = this.dataList.foldRight((i, p) -> {
                if (p.left) {
                    return new Pair<>(true, p.right.cons(i));
                }
                if (Leaf.this.hasher.eq(i.left, key)) {
                    return new Pair<>(true, p.right);
                }
                return new Pair<>(false, p.right.cons(i));
            }, new Pair<>(false, ImmutableList.empty()));
            if (result.left) {
                if (this.length == 1) {
                    return Maybe.of(empty(this.hasher));
                }
                return Maybe.of(new Leaf<>(this.hasher, result.right, this.baseHash, this.length - 1));
            }
            return Maybe.empty();
        }

        @SuppressWarnings("unchecked")
        private Fork<K, V> toFork() {
            int subHash = this.baseHash & 31;
            HashTable<K, V>[] children = new HashTable[32];
            children[subHash] = new Leaf<>(this.hasher, this.dataList, this.baseHash >>> 5, this.length);
            return new Fork<>(this.hasher, children, this.length);
        }

        @Nonnull
        @Override
        protected Maybe<V> get(@Nonnull final K key, final int hash) {
            if (this.baseHash != hash) {
                return Maybe.empty();
            }
            Maybe<Pair<K, V>> pairMaybe = this.dataList.find(kvPair -> Leaf.this.hasher.eq(kvPair.left, key));
            return pairMaybe.map(p -> p.right);
        }

        @SuppressWarnings("unchecked")
        @Nonnull
        @Override
        public HashTable<K, V> merge(@Nonnull HashTable<K, V> tree, @Nonnull final F2<V, V, V> merger) {
            if (tree instanceof Empty) {
                return this;
            } else if (tree instanceof Leaf) {
                final Leaf<K, V> leaf = (Leaf<K, V>) tree;
                if (leaf.baseHash == this.baseHash) {
                    final Pair<K, V>[] pairs = this.dataList.toArray(new Pair[this.dataList.length]);
                    ImmutableList<Pair<K, V>> right = leaf.dataList.foldLeft(
                            (@Nonnull ImmutableList<Pair<K, V>> result, @Nonnull Pair<K, V> kvPair) -> {
                                for (int i = 0; i < pairs.length; i++) {
                                    if (Leaf.this.hasher.eq(pairs[i].left, kvPair.left)) {
                                        pairs[i] = new Pair<>(pairs[i].left, merger.apply(pairs[i].right, kvPair.right));
                                        return result;
                                    }
                                }
                                return result.cons(kvPair);
                            }, ImmutableList.empty());
                    ImmutableList<Pair<K, V>> newList = ImmutableList.from(pairs).append(right);
                    return new Leaf<>(this.hasher, newList, this.baseHash, newList.length);
                }
            }
            return this.toFork().merge(tree, merger);
        }

        @Nonnull
        public <A> A foldLeft(@Nonnull F2<A, Pair<K, V>, A> f, @Nonnull A init) {
            return this.dataList.foldLeft(f, init);
        }

        @Nonnull
        @Override
        public <A> A foldRight(@Nonnull F2<Pair<K, V>, A, A> f, @Nonnull A init) {
            return this.dataList.foldRight(f, init);
        }

        @Override
        public Iterator<Pair<K, V>> iterator() {
            return dataList.iterator();
        }

        @Override
        public void forEach(@Nonnull Consumer<? super Pair<K, V>> e) {
            this.dataList.forEach(e);
        }

        @Nonnull
        @Override
        public Maybe<Pair<K, V>> find(@Nonnull F<Pair<K, V>, Boolean> f) {
            return this.dataList.find(f);
        }

        @Nonnull
        @Override
        public <R> Maybe<R> findMap(@Nonnull F<Pair<K, V>, Maybe<R>> f) {
            return this.dataList.findMap(f);
        }

        @Override
        public <B> Leaf<K, B> map(@Nonnull F<V, B> f) {
            return new Leaf<>(this.hasher, this.dataList.map(pair -> pair.mapRight(f)), this.baseHash, this.length);
        }

        @Override
        public boolean containsKey(@Nonnull K key, int hash) {
            return hash == this.baseHash
                    && this.dataList.exists(kvPair -> Leaf.this.hasher.eq(kvPair.left, key));
        }
    }

    private final static class Fork<K, V> extends HashTable<K, V> {
        @Nonnull
        private final HashTable<K, V>[] children;

        private Fork(@Nonnull Hasher<K> hasher, @Nonnull HashTable<K, V>[] children, int length) {
            super(hasher, length);
            this.children = children;
        }

        @Nonnull
        @Override
        protected HashTable<K, V> put(@Nonnull K key, @Nonnull V value, int hash) {
            int subHash = hash & 31;
            HashTable<K, V>[] cloned = Fork.this.children.clone();
            if (cloned[subHash] == null) {
                cloned[subHash] = new Leaf<>(Fork.this.hasher, ImmutableList.empty(), hash >>> 5, 0);
            }
            //noinspection UnnecessaryLocalVariable
            int oldLength = cloned[subHash].length;
            cloned[subHash] = cloned[subHash].put(key, value, hash >>> 5);
            return new Fork<>(this.hasher, cloned, this.length - oldLength + cloned[subHash].length);
        }

        @Nonnull
        @Override
        protected Maybe<HashTable<K, V>> remove(@Nonnull K key, int hash) {
            final int subHash = hash & 31;
            if (this.children[subHash] == null) {
                return Maybe.empty();
            }
            Maybe<HashTable<K, V>> removed = this.children[subHash].remove(key, hash >>> 5);
            return removed.map(newChild -> {
                if (Fork.this.length == 1) {
                    return new Empty<>(Fork.this.hasher);
                }
                HashTable<K, V>[] cloned = Fork.this.children.clone();
                cloned[subHash] = newChild;
                return new Fork<>(Fork.this.hasher, cloned, Fork.this.length - 1);
            });
        }

        @Nonnull
        @Override
        protected Maybe<V> get(@Nonnull K key, int hash) {
            int subHash = hash & 31;
            if (this.children[subHash] == null) {
                return Maybe.empty();
            }
            return this.children[subHash].get(key, hash >>> 5);
        }

        @Nonnull
        @Override
        public Fork<K, V> merge(@Nonnull HashTable<K, V> tree, @Nonnull F2<V, V, V> merger) {
            if (tree instanceof Empty) {
                return this;
            } else if (tree instanceof Leaf) {
                Leaf<K, V> leaf = (Leaf<K, V>) tree;
                return this.mergeFork(leaf.toFork(), merger);
            }
            return this.mergeFork(((Fork<K, V>) tree), merger);
        }

        @Nonnull
        private Fork<K, V> mergeFork(@Nonnull Fork<K, V> tree, @Nonnull F2<V, V, V> merger) {
            // Mutable array.
            HashTable<K, V>[] cloned = Fork.this.children.clone();
            int count = 0;
            for (int i = 0; i < cloned.length; i++) {
                if (cloned[i] == null) {
                    cloned[i] = tree.children[i];
                } else if (tree.children[i] != null) {
                    cloned[i] = cloned[i].merge(tree.children[i], merger);
                }
                if (cloned[i] != null) {
                    count += cloned[i].length;
                }
            }
            return new Fork<>(this.hasher, cloned, count);
        }

        @Nonnull
        @Override
        public <A> A foldLeft(@Nonnull F2<A, Pair<K, V>, A> f, @Nonnull A init) {
            for (@Nullable HashTable<K, V> child : this.children) {
                if (child != null) {
                    init = child.foldLeft(f, init);
                }
            }
            return init;
        }

        @Nonnull
        @Override
        public <A> A foldRight(@Nonnull F2<Pair<K, V>, A, A> f, @Nonnull A init) {
            for (int i = this.children.length - 1; i >= 0; i--) {
                if (this.children[i] == null) {
                    continue;
                }
                init = this.children[i].foldRight(f, init);
            }
            return init;
        }

        public Iterator<Pair<K, V>> iterator() {
            return new Iterator<Pair<K, V>>() {
                @SuppressWarnings("unchecked")
                private final HashTable<K, V>[] stack = new HashTable[Fork.this.length];
                private Iterator<Pair<K, V>> currentIterator = null;
                int i = 0;

                {
                    this.stack[this.i++] = Fork.this;
                }

                private void updateState() {
                    if (currentIterator != null && currentIterator.hasNext()) {
                        return;
                    } else {
                        currentIterator = null;
                    }
                    while (this.i > 0) {
                        this.i--;
                        HashTable<K, V> curr = this.stack[this.i];
                        if (curr instanceof Fork) {
                            Fork<K, V> fork = (Fork<K, V>) curr;
                            for (HashTable<K, V> child : fork.children) {
                                if (child != null && child.length > 0) {
                                    this.stack[this.i] = child;
                                    this.i++;
                                }
                            }
                        } else if (curr instanceof Leaf) {
                            currentIterator = ((Leaf<K, V>) curr).dataList.iterator();
                            if (currentIterator.hasNext()) {
                                return;
                            } else {
                                currentIterator = null;
                            }
                        }
                    }
                }

                @Override
                public boolean hasNext() {
                    updateState();
                    return currentIterator != null;
                }

                @Override
                public Pair<K, V> next() {
                    updateState();
                    if (currentIterator == null) {
                        throw new NoSuchElementException();
                    }
                    return currentIterator.next();
                }
            };
        }

        @Override
        public void forEach(@Nonnull Consumer<? super Pair<K, V>> e) {
            for (@Nullable HashTable<K, V> child : this.children) {
                if (child != null) {
                    child.forEach(e);
                }
            }
        }

        @Nonnull
        @Override
        public Maybe<Pair<K, V>> find(@Nonnull F<Pair<K, V>, Boolean> f) {
            HashTable<K, V>[] children = this.children;
            for (HashTable<K, V> child : children) {
                if (child != null) {
                    Maybe<Pair<K, V>> p = child.find(f);
                    if (p.isJust()) {
                        return p;
                    }
                }
            }
            return Maybe.empty();
        }

        @Nonnull
        @Override
        public <R> Maybe<R> findMap(@Nonnull F<Pair<K, V>, Maybe<R>> f) {
            HashTable<K, V>[] children = this.children;
            for (HashTable<K, V> child : children) {
                if (child != null) {
                    Maybe<R> p = child.findMap(f);
                    if (p.isJust()) {
                        return p;
                    }
                }
            }
            return Maybe.empty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <B> Fork<K, B> map(@Nonnull F<V, B> f) {
            HashTable<K, B>[] clone = new HashTable[this.children.length];
            for (int i = 0; i < clone.length; i++) {
                if (this.children[i] != null) {
                    clone[i] = this.children[i].map(f);
                }
            }
            return new Fork<>(this.hasher, clone, this.length);
        }

        @Override
        public boolean containsKey(@Nonnull K key, int hash) {
            int subHash = hash & 31;
            return this.children[subHash] != null && this.children[subHash].containsKey(key, hash >>> 5);
        }
    }
}
