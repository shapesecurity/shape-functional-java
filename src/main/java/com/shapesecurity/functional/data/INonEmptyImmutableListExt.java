package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;

public interface INonEmptyImmutableListExt<T> extends INonEmptyImmutableList<T> {

    @Nonnull
    Pair<IImmutableList<T>, IImmutableList<T>> span(@Nonnull F<T, Boolean> f);

    @Nonnull
    <B, C> Pair<B, INonEmptyImmutableList<C>> mapAccumL(@Nonnull F2<B, T, Pair<B, C>> f, @Nonnull B acc);

    @Nonnull
    <B> Maybe<B> decons(@Nonnull F2<T, IImmutableList<T>, B> f);
}
