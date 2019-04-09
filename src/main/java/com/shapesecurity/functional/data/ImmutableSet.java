package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.Unit;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    @SafeVarargs
    public static <T> ImmutableSet<T> ofUsingIdentity(@Nonnull T... items) {
        return ImmutableSet.<T>emptyUsingIdentity().putAll(items);
    }

    @Nonnull
    @SafeVarargs
    public static <T> ImmutableSet<T> ofUsingEquality(@Nonnull T... items) {
        return ImmutableSet.<T>emptyUsingEquality().putAll(items);
    }

    @Nonnull
    @SafeVarargs
    public static <T> ImmutableSet<T> of(@Nonnull T... items) {
        return ofUsingEquality(items);
    }

    @Nonnull
    public static <T> ImmutableSet<T> from(@Nonnull Hasher<T> hasher, @Nonnull Set<T> set) {
        return empty(hasher).union(set);
    }

    @Nonnull
    public static <T> ImmutableSet<T> fromUsingEquality(@Nonnull Set<T> set) {
        return ImmutableSet.<T>emptyUsingEquality().union(set);
    }

    @Nonnull
    public static <T> ImmutableSet<T> fromUsingIdentity(@Nonnull Set<T> set) {
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

    @Nonnull
    public <B extends T> ImmutableSet<T> putAll(@Nonnull B... list) {
        ImmutableSet<T> set = this;
        for (B b : list) {
            set = set.put(b);
        }
        return set;
    }

    public boolean contains(@Nonnull T datum) {
        return this.data.containsKey(datum);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <A> ImmutableSet<A> map(@Nonnull F<T, A> f) {
        return new ImmutableSet<>(HashTable.from((Hasher<A>) this.data.hasher, this.data.entries().map(pair -> Pair.of(f.apply(pair.left), pair.right))));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <A> ImmutableSet<A> flatMap(@Nonnull F<T, ImmutableSet<A>> f) {
        return this.foldAbelian((t, acc) -> acc.union(f.apply(t)), ImmutableSet.empty((Hasher<A>) this.data.hasher));
    }

    @Nonnull
    public ImmutableSet<T> filter(@Nonnull F<T, Boolean> f) {
        return new ImmutableSet<>(this.data.filter(pair -> f.apply(pair.left)));
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
    public ImmutableSet<T> union(@Nonnull Set<T> other) {
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
    public Set<T> toHashSet() {
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
    @Override
    public final Spliterator<T> spliterator() {
        return Spliterators.spliterator(this.iterator(), this.length(), Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }

    @Nonnull
    public final Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Nonnull
    public <V> HashTable<T, V> mapToTable(@Nonnull F<T, V> f) {
        return HashTable.from(this.data.hasher, this.data.entries().map(pair -> Pair.of(pair.left, f.apply(pair.left))));
    }


    @Nonnull
    public static <T> Collector<T, ?, ImmutableSet<T>> collector() {
        return new Collector<T, Set<T>, ImmutableSet<T>>() {
            @Override
            public Supplier<Set<T>> supplier() {
                return HashSet::new;
            }

            @Override
            public BiConsumer<Set<T>, T> accumulator() {
                return Set::add;
            }

            @Override
            public BinaryOperator<Set<T>> combiner() {
                return (left, right) -> {
                    left.addAll(right);
                    return left;
                };
            }

            @Override
            public Function<Set<T>, ImmutableSet<T>> finisher() {
                return ImmutableSet::fromUsingEquality;
            }

            @Override
            public Set<Characteristics> characteristics() {
                Set<Characteristics> set = new HashSet<>();
                set.add(Characteristics.UNORDERED);
                return set;
            }
        };
    }

}
