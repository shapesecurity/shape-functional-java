package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;

public interface INonEmptyImmutableList<T> extends IImmutableList<T> {

    @Nonnull
    INonEmptyImmutableList<T> append(T right);

    @Nonnull
    INonEmptyImmutableList<T> prepend(T left);

    @Nonnull
    T head();

    @Nonnull
    T last();

    @Nonnull
    IImmutableList<T> tail();

    @Nonnull
    IImmutableList<T> init();

    @Nonnull
    IImmutableList<T> take(int n);

    @Nonnull
    IImmutableList<T> drop(int n);

    @Nonnull
    Maybe<NonEmptyImmutableList<T>> toNonEmptyList();

    @Nonnull
    <B, C> INonEmptyImmutableList<C> zipWith(@Nonnull F2<T, B, C> f, @Nonnull INonEmptyImmutableList<B> list);

    @Override
    default boolean isEmpty() {
        return false;
    }

    @Override
    default boolean isNotEmpty() {
        return true;
    }

    @Override
    @Nonnull
    <B> INonEmptyImmutableList<B> map(@Nonnull F<T, B> f);

    @Override
    @Nonnull
    <B> INonEmptyImmutableList<B> mapWithIndex(@Nonnull F2<Integer, T, B> f);

    @Override
    @Nonnull
    <B> IImmutableList<B> chain(@Nonnull F<T, IImmutableList<B>> f);

    @Override
    @Nonnull
    INonEmptyImmutableList<T> reverse();

    @Override
    @Nonnull
    <B extends T> INonEmptyImmutableList<T> append(@Nonnull IImmutableList<B> defaultClause);

    @Nonnull
    T[] toArray(@Nonnull T[] target);

}
