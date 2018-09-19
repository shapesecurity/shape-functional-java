package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;

public interface IImmutableList<T> extends Iterable<T> {

    default boolean isEmpty() {
        return true;
    }

    default boolean isNotEmpty() {
        return false;
    }

    int length();

    @Nonnull
    INonEmptyImmutableList<T> cons(T left);

    @Nonnull
    <B> B foldLeft(@Nonnull F2<B, ? super T, B> f, @Nonnull B init);

    @Nonnull
    <B> B foldRight(@Nonnull F2<? super T, B, B> f, @Nonnull B init);

    @Nonnull
    IImmutableList<T> filter(@Nonnull F<T, Boolean> f);

    int count(@Nonnull F<T, Boolean> f);

    @Nonnull
    <B> IImmutableList<B> map(@Nonnull F<T, B> f);

    @Nonnull
    <B> IImmutableList<B> mapWithIndex(@Nonnull F2<Integer, T, B> f);

    @Nonnull
    <B> IImmutableList<B> chain(@Nonnull F<T, IImmutableList<B>> f);

    boolean exists(@Nonnull F<T, Boolean> f);

    boolean contains(@Nonnull T t);

    @Nonnull
    IImmutableList<T> removeAll(@Nonnull F<T, Boolean> f);

    @Nonnull
    IImmutableList<T> reverse();

    @Nonnull
    Maybe<T> index(int index);

    @Nonnull
    Maybe<Integer> findIndexEquality(@Nonnull T t);

    @Nonnull
    Maybe<Integer> findIndexIdentity(@Nonnull T t);

    @Nonnull
    Maybe<T> find(@Nonnull F<T, Boolean> f);

    @Nonnull
    Maybe<Integer> findIndex(@Nonnull F<T, Boolean> f);

    @Nonnull
    <B> Maybe<B> findMap(@Nonnull F<T, Maybe<B>> f);

    @Nonnull
    <B extends T> IImmutableList<T> append(@Nonnull IImmutableList<B> defaultClause);

    @Nonnull
    <B extends T> IImmutableList<T> patch(int index, int patchLength, @Nonnull IImmutableList<B> replacements);

    @Nonnull
    ImmutableSet<T> uniqByEquality();

    @Nonnull
    ImmutableSet<T> uniqByIdentity();

    @Nonnull
    <B> ImmutableSet<T> uniqByEqualityOn(@Nonnull F<T, B> f);

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
