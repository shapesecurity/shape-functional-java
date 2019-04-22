package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Unit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@CheckReturnValue
public class ImmutableSet<T> implements Iterable<T> {
    @Nonnull
    private final HashTable<T, Unit> data;

    public int length() {
        return this.data.length;
    }

    @Nonnull
    public Hasher<T> hasher() {
        return this.data.hasher;
    }

    ImmutableSet(@Nonnull HashTable<T, Unit> data) {
        this.data = data;
    }

    @Nonnull
    public static <T> ImmutableSet<T> empty(@Nonnull Hasher<T> hasher) {
        return new ImmutableSet<>(HashTable.empty(hasher));
    }

    @Nonnull
    public static <T> ImmutableSet<T> emptyUsingEquality() {
        return new ImmutableSet<>(HashTable.emptyUsingEquality());
    }

    @Nonnull
    public static <T> ImmutableSet<T> emptyUsingIdentity() {
        return new ImmutableSet<>(HashTable.emptyUsingIdentity());
    }

    @Nonnull
    public static <T> ImmutableSet<T> from(@Nonnull Hasher<T> hasher, @Nonnull Iterable<T> set) {
        return empty(hasher).union(set);
    }

    @Nonnull
    public static <T> ImmutableSet<T> fromUsingEquality(@Nonnull Iterable<T> set) {
        return ImmutableSet.<T>emptyUsingEquality().union(set);
    }

    @Nonnull
    public static <T> ImmutableSet<T> fromUsingIdentity(@Nonnull Iterable<T> set) {
        return ImmutableSet.<T>emptyUsingIdentity().union(set);
    }

    @Deprecated
    @Nonnull
    public static <T> ImmutableSet<T> empty() {
        return ImmutableSet.emptyUsingEquality();
    }

    @Deprecated
    @Nonnull
    public static <T> ImmutableSet<T> emptyP() {
        return ImmutableSet.emptyUsingIdentity();
    }

    @Nonnull
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

    @Nonnull
    @SuppressWarnings("unchecked")
    public <A> ImmutableSet<A> map(@Nonnull F<T, A> f) {
        return this.foldAbelian((val, acc) -> acc.put(f.apply(val)), ImmutableSet.empty((Hasher<A>) this.data.hasher));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <A> ImmutableSet<A> flatMap(@Nonnull F<T, ImmutableSet<A>> f) {
        return this.foldAbelian((t, acc) -> {
            ImmutableSet<A> set = f.apply(t);
            if (!set.data.hasher.equals(acc.data.hasher)) {
                throw new UnsupportedOperationException("Hasher mismatch in flatMap.");
            }
            return acc.union(set);
        }, ImmutableSet.empty((Hasher<A>) this.data.hasher));
    }

    @Nonnull
    public ImmutableSet<T> filter(@Nonnull F<T, Boolean> f) {
        return this.foldAbelian((val, acc) -> f.apply(val) ? acc.put(val) : acc, ImmutableSet.empty(this.data.hasher));
    }


    public ImmutableSet<T> remove(@Nonnull T datum) {
        return new ImmutableSet<>(this.data.remove(datum));
    }

    @Nonnull
    public <A> A foldAbelian(@Nonnull F2<T, A, A> f, @Nonnull A init) {
        return this.data.foldRight((p, acc) -> f.apply(p.left, acc), init);
    }

    @Nonnull
    public ImmutableSet<T> union(@Nonnull ImmutableSet<T> other) {
        return new ImmutableSet<>(this.data.merge(other.data));
    }

    @Nonnull
    public ImmutableSet<T> union(@Nonnull Iterable<T> other) {
        ImmutableSet<T> set = this;
        for (T entry : other) {
            set = set.put(entry);
        }
        return set;
    }

    // Does not guarantee ordering of elements in resulting list.
    @Nonnull
    public ImmutableList<T> toList() {
        return this.foldAbelian((v, acc) -> acc.cons(v), ImmutableList.empty());
    }

    @Nonnull
    public Set<T> toSet() {
        if (this.data.hasher != HashTable.equalityHasher()) {
            throw new UnsupportedOperationException("Cannot call ImmutableSet::toSet on a ImmutableSet without equality hashing.");
        }
        Set<T> set = new HashSet<>();
        this.forEach(set::add);
        return set;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        return other instanceof ImmutableSet && this.data.length == ((ImmutableSet) other).data.length && this.data.foldLeft((memo, pair) -> memo && ((ImmutableSet) other).data.containsKey(pair.left), true);
    }

    @Override
    @Nonnull
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

    @Nonnull
    public <V> HashTable<T, V> mapToTable(@Nonnull F<T, V> f) {
        HashTable<T, V> table = HashTable.empty(this.data.hasher);
        for (T entry : this) {
            table = table.put(entry, f.apply(entry));
        }
        return table;
    }
    @Nonnull
    @Override
    public final Spliterator<T> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.length(), Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }

    @Nonnull
    public final Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Nonnull
    public static <T> Collector<T, ?, ImmutableSet<T>> collector(@Nonnull Hasher<T> hasher) {
        // we use a list for state because java doesnt support our Hasher type
        return new Collector<T, ArrayList<T>, ImmutableSet<T>>() {
            @Override
            public Supplier<ArrayList<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<ArrayList<T>, T> accumulator() {
                return ArrayList::add;
            }

            @Override
            public BinaryOperator<ArrayList<T>> combiner() {
                return (left, right) -> {
                    left.addAll(right);
                    return left;
                };
            }

            @Override
            public Function<ArrayList<T>, ImmutableSet<T>> finisher() {
                return list -> {
                    ImmutableSet<T> set = ImmutableSet.empty(hasher);
                    for (T entry : list) {
                        set = set.put(entry);
                    }
                    return set;
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                Set<Characteristics> set = new HashSet<>();
                set.add(Characteristics.UNORDERED);
                return set;
            }
        };
    }

    @Nonnull
    public static <T> Collector<T, ?, ImmutableSet<T>> collector() {
        return ImmutableSet.collectorUsingEquality();
    }

    @Nonnull
    public static <T> Collector<T, ?, ImmutableSet<T>> collectorUsingEquality() {
        return ImmutableSet.collector(HashTable.equalityHasher());
    }

    @Nonnull
    public static <T> Collector<T, ?, ImmutableSet<T>> collectorUsingIdentity() {
        return ImmutableSet.collector(HashTable.identityHasher());
    }
}
