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
import com.shapesecurity.functional.ThrowingSupplier;
import com.shapesecurity.functional.Thunk;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
    @Nonnull
    public static <A> Maybe<A> empty() {
        return (Maybe<A>) NOTHING;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Deprecated
    public static <A> Maybe<A> nothing() {
        return Maybe.empty();
    }

    @Nonnull
    public static <A> Maybe<A> of(@Nonnull A a) {
        return new Maybe<>(a);
    }

    @Nonnull
    @Deprecated
    public static <A> Maybe<A> just(@Nonnull A a) {
        return Maybe.of(a);
    }

    @Nonnull
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

    public static <A> Maybe<A> join(@Nonnull Maybe<Maybe<A>> m) {
        return m.flatMap((a) -> a);
    }

    @Nonnull
    public static <A> ImmutableList<A> catMaybes(@Nonnull ImmutableList<Maybe<A>> l) {
        return l.foldRight((a, b) -> a.maybe(b, c -> ImmutableList.cons(c, b)), ImmutableList.empty());
    }

    @Nonnull
    public static <A, B> ImmutableList<B> mapMaybe(@Nonnull final F<A, B> f, @Nonnull ImmutableList<Maybe<A>> l) {
        return l.foldRight((a, b) -> a.maybe(b, v -> ImmutableList.cons(f.apply(v), b)), ImmutableList.empty());
    }

    @SuppressWarnings("BooleanParameter")
    @Nonnull
    public static <A> Maybe<A> iff(boolean test, @Nonnull A a) {
        if (test) {
            return of(a);
        }
        return empty();
    }
    @Nonnull
    public static <A> Maybe<A> _try(@Nonnull ThrowingSupplier<A> s) {
        // Note that this method does not distinguish between throwing and returning null.
        try {
            return Maybe.fromNullable(s.get());
        } catch (Exception e) {
            return Maybe.empty();
        }
    }

    public boolean eq(@Nonnull Maybe<A> maybe) {
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

    @Nonnull
    public A fromJust() throws NullPointerException {
        return Objects.requireNonNull(this.value);
    }

    @Nonnull
    @Deprecated
    public A just() throws NullPointerException {
        return this.fromJust();
    }

    @Nonnull
    public <B> B maybe(@Nonnull B def, @Nonnull F<A, B> f) {
        return this.value == null ? def : f.apply(this.value);
    }

    public final void foreach(@Nonnull Effect<A> f) {
        this.map(f);
    }

    public final void foreach(@Nonnull Runnable r, @Nonnull Effect<A> f) {
        if (this.value == null) {
            r.run();
        } else {
            f.apply(this.value);
        }
    }

    public boolean isJust() {
        return this.value != null;
    }

    public final boolean isNothing() {
        return !this.isJust();
    }

    @Nonnull
    public ImmutableList<A> toList() {
        return this.value == null ? ImmutableList.empty() : ImmutableList.from(this.value);
    }

    @Nonnull
    public A orJust(@Nonnull A a) {
        return this.value == null ? a : this.value;
    }

    /**
     * @deprecated Use {@link #orJustLazy(Supplier)} instead.
     */
    @Nonnull
    @Deprecated
    public A orJustLazy(@Nonnull Thunk<A> a) {
        return this.value == null ? a.get() : this.value;
    }

    @Nonnull
    public A orJustLazy(@Nonnull Supplier<A> a) {
        return this.value == null ? a.get() : this.value;
    }

    @Nonnull
    public <B> Maybe<B> map(@Nonnull F<A, B> f) {
        //noinspection unchecked
        return this.value == null ? ((Maybe<B>) this) : of(f.apply(this.value));
    }

    @Nonnull
    public final <B> Maybe<B> bind(@Nonnull F<A, Maybe<B>> f) {
        return this.flatMap(f);
    }

    @Nonnull
    public <B> Maybe<B> flatMap(@Nonnull F<A, Maybe<B>> f) {
        //noinspection unchecked
        return this.value == null ? ((Maybe<B>) this) : f.apply(this.value);
    }

    @Nonnull
    public Maybe<A> filter(@Nonnull F<A, Boolean> f) {
        return this.filterByPredicate(f::apply);
    }

    @Nonnull
    public Maybe<A> filterByPredicate(@Nonnull Predicate<A> f) {
        return this.value == null ? this : (f.test(this.value) ? this : empty());
    }
}
