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

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;

public final class Nil<T> extends ImmutableList<T> {
    private final static int DEFAULT_HASH_CODE;

    static {
        int h = HashCodeBuilder.init();
        DEFAULT_HASH_CODE = HashCodeBuilder.put(h, "Nil");
    }

    Nil() {
        super(0);
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    protected int calcHashCode() {
        return DEFAULT_HASH_CODE;
    }

    @Nonnull
    @Override
    public <A> A foldLeft(@Nonnull F2<A, ? super T, A> f, @Nonnull A init) {
        return init;
    }

    @Nonnull
    @Override
    public <A> A foldRight(@Nonnull F2<? super T, A, A> f, @Nonnull A init) {
        return init;
    }

    @Nonnull
    @Override
    public Maybe<T> maybeHead() {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<T> maybeLast() {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<ImmutableList<T>> maybeTail() {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<ImmutableList<T>> maybeInit() {
        return Maybe.empty();
    }

    @Override
    public int count(@Nonnull F<T, Boolean> f) {
        return 0;
    }

    @Nonnull
    @Override
    public ImmutableList<T> filter(@Nonnull F<T, Boolean> f) {
        return this;
    }

    @Nonnull
    @Override
    public <B> ImmutableList<B> map(@Nonnull F<T, B> f) {
        return empty();
    }

    @Nonnull
    @Override
    public <B> ImmutableList<B> mapWithIndex(@Nonnull F2<Integer, T, B> f) {
        return empty();
    }

    @Nonnull
    @Override
    public ImmutableList<T> take(int n) {
        return this;
    }

    @Nonnull
    @Override
    public ImmutableList<T> drop(int n) {
        return this;
    }

    @Nonnull
    @Override
    public Maybe<NonEmptyImmutableList<T>> toNonEmptyList() {
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public <B> Maybe<B> decons(@Nonnull F2<T, ImmutableList<T>, B> f) {
        return Maybe.empty();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <B, C> ImmutableList<C> zipWith(@Nonnull F2<T, B, C> f, @Nonnull ImmutableList<B> list) {
        return (ImmutableList<C>) this;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <B extends T> ImmutableList<T> append(@Nonnull ImmutableList<B> defaultClause) {
        // This is safe due to erasure.
        return (ImmutableList<T>) defaultClause;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <B extends T> IImmutableList<T> append(@Nonnull IImmutableList<B> defaultClause) {
        // This is safe due to erasure.
        return (IImmutableList<T>) defaultClause;
    }

    @Override
    public boolean exists(@Nonnull F<T, Boolean> f) {
        return false;
    }

    @Override
    public boolean every(@Nonnull F<T, Boolean> f) {
        return true;
    }

    @Override
    public boolean contains(@Nonnull T a) {
        return false;
    }

    @Nonnull
    @Override
    public Pair<ImmutableList<T>, ImmutableList<T>> span(@Nonnull F<T, Boolean> f) {
        return new Pair<>(empty(), empty());
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <B> ImmutableList<B> flatMap(@Nonnull F<T, ImmutableList<B>> f) {
        return (ImmutableList<B>) this;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <B> IImmutableList<B> chain(@Nonnull F<T, IImmutableList<B>> f) {
        return (ImmutableList<B>) this;
    }

    @Nonnull
    @Override
    public ImmutableList<T> removeAll(@Nonnull F<T, Boolean> f) {
        return this;
    }

    @Nonnull
    @Override
    public ImmutableList<T> reverse() {
        return this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public <B extends T> ImmutableList<T> patch(int index, int patchLength, @Nonnull ImmutableList<B> replacements) {
        return (ImmutableList<T>) replacements;
    }

    @Nonnull
    @Override
    public <B, C> Pair<B, ImmutableList<C>> mapAccumL(@Nonnull F2<B, T, Pair<B, C>> f, @Nonnull B acc) {
        return new Pair<>(acc, ImmutableList.empty());
    }

    @Nonnull
    @Override
    public ImmutableSet<T> uniqByEquality() {
        return ImmutableSet.emptyUsingEquality();
    }

    @Nonnull
    @Override
    public ImmutableSet<T> uniqByIdentity() {
        return ImmutableSet.emptyUsingIdentity();
    }

    @Nonnull
    @Override
    public <B> ImmutableSet<T> uniqByEqualityOn(@Nonnull F<T, B> f) {
        return ImmutableSet.emptyUsingEquality();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
