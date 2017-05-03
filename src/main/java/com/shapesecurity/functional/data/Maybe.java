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

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.shapesecurity.functional.Effect;
import com.shapesecurity.functional.F;
import com.shapesecurity.functional.Thunk;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
public final class Maybe<A> {
    private final static int NOTHING_HASH_CODE = HashCodeBuilder.put(HashCodeBuilder.init(), "Nothing");

    @SuppressWarnings("unchecked")
    private final static Maybe NOTHING = new Maybe(null);

    @Nullable
    private final A value;

    // class local
    private Maybe(@Nullable A value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <A> Maybe<A> empty() {
        return (Maybe<A>) NOTHING;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Deprecated
    public static <A> Maybe<A> nothing() {
        return Maybe.empty();
    }

    @NotNull
    public static <A> Maybe<A> of(@NotNull A a) {
        return new Maybe<>(a);
    }

    @NotNull
    @Deprecated
    public static <A> Maybe<A> just(@NotNull A a) {
        return Maybe.of(a);
    }

    @NotNull
    public static <A> Maybe<A> fromNullable(@Nullable A a) {
        if (a == null) {
            return empty();
        }
        return of(a);
    }

    @Nullable
    public A toNullable() {
        return this.value;
    }

    public static <A> Maybe<A> join(@NotNull Maybe<Maybe<A>> m) {
        return m.flatMap((a) -> a);
    }

    @NotNull
    public static <A> ImmutableList<A> catMaybes(@NotNull ImmutableList<Maybe<A>> l) {
        return l.foldRight((a, b) -> a.maybe(b, c -> ImmutableList.cons(c, b)), ImmutableList.empty());
    }

    @NotNull
    public static <A, B> ImmutableList<B> mapMaybe(@NotNull final F<A, B> f, @NotNull ImmutableList<Maybe<A>> l) {
        return l.foldRight((a, b) -> a.maybe(b, v -> ImmutableList.cons(f.apply(v), b)), ImmutableList.empty());
    }

    @SuppressWarnings("BooleanParameter")
    @NotNull
    public static <A> Maybe<A> iff(boolean test, @NotNull A a) {
        if (test) {
            return of(a);
        }
        return empty();
    }

    public boolean eq(@NotNull Maybe<A> maybe) {
        return Objects.equals(maybe.value, this.value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(Object obj) {
        return obj == this || obj instanceof Maybe && this.eq((Maybe<A>) obj);
    }

    @Override
    public int hashCode() {
        return this.value == null ? NOTHING_HASH_CODE :
               HashCodeBuilder.put(this.value.hashCode(), "Just");
    }

    @NotNull
    public A fromJust() throws NullPointerException {
        return Objects.requireNonNull(this.value);
    }

    @NotNull
    @Deprecated
    public A just() throws NullPointerException {
        return this.fromJust();
    }

    @NotNull
    public <B> B maybe(@NotNull B def, @NotNull F<A, B> f) {
        return this.value == null ? def : f.apply(this.value);
    }

    public final void foreach(@NotNull Effect<A> f) {
        this.map(f);
    }

    public boolean isJust() {
        return this.value != null;
    }

    public final boolean isNothing() {
        return !this.isJust();
    }

    @NotNull
    public ImmutableList<A> toList() {
        return this.value == null ? ImmutableList.empty() : ImmutableList.from(this.value);
    }

    @NotNull
    public A orJust(@NotNull A a) {
        return this.value == null ? a : this.value;
    }

    /**
     * @deprecated Use {@link #orJustLazy(Supplier)} instead.
     */
    @NotNull
    @Deprecated
    public A orJustLazy(@NotNull Thunk<A> a) {
        return this.value == null ? a.get() : this.value;
    }

    @NotNull
    public A orJustLazy(@NotNull Supplier<A> a) {
        return this.value == null ? a.get() : this.value;
    }

    @NotNull
    public <B> Maybe<B> map(@NotNull F<A, B> f) {
        //noinspection unchecked
        return this.value == null ? ((Maybe<B>) this) : of(f.apply(this.value));
    }

    @NotNull
    public final <B> Maybe<B> bind(@NotNull F<A, Maybe<B>> f) {
        return this.flatMap(f);
    }

    @NotNull
    public <B> Maybe<B> flatMap(@NotNull F<A, Maybe<B>> f) {
        //noinspection unchecked
        return this.value == null ? ((Maybe<B>) this) : f.apply(this.value);
    }

    @NotNull
    public Maybe<A> filter(@NotNull F<A, Boolean> f) {
        return this.filterByPredicate(f::apply);
    }

    @NotNull
    public Maybe<A> filterByPredicate(@NotNull Predicate<A> f) {
        return this.value == null ? this : (f.test(this.value) ? this : empty());
    }
}
