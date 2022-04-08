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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@CheckReturnValue
public final class Either<A, B> {
    private final Object data;

    private enum Tag {
        LEFT, RIGHT
    }

    private final Tag tag;

    // class local
    private Either(Object data, Tag tag) {
        super();
        this.data = data;
        this.tag = tag;
    }

    @Nonnull
    public static <A, B> Either<A, B> left(@Nonnull A a) {
        return new Either<>(a, Tag.LEFT);
    }

    @Nonnull
    public static <A, B> Either<A, B> right(@Nonnull B b) {
        return new Either<>(b, Tag.RIGHT);
    }

    @Nonnull
    public static <A, B extends A, C extends A> A extract(Either<B, C> e) {
        return e.either(x -> x, x -> x);
    }

    @Nonnull
    public static <A> Either<Exception, A> _try(@Nonnull ThrowingSupplier<A> s) {
        try {
            return Either.right(s.get());
        } catch (Exception e) {
            return Either.left(e);
        }
    }

    public final boolean isLeft() {
        return this.tag == Tag.LEFT;
    }

    public final boolean isRight() {
        return this.tag == Tag.RIGHT;
    }

    @SuppressWarnings("unchecked")
    public <X> X either(F<A, X> f1, F<B, X> f2) {
        if (this.tag == Tag.LEFT) {
            return f1.apply((A) this.data);
        } else {
            return f2.apply((B) this.data);
        }
    }

    @SuppressWarnings("unchecked")
    public void foreach(@Nonnull Effect<A> f1, @Nonnull Effect<B> f2) {
        if (this.tag == Tag.LEFT) {
            f1.apply((A) this.data);
        } else {
            f2.apply((B) this.data);
        }
    }

    @Nonnull
    public <X, Y> Either<X, Y> map(F<A, X> f1, F<B, Y> f2) {
        return this.either(a -> Either.<X, Y>left(f1.apply(a)), b -> Either.<X, Y>right(f2.apply(b)));
    }

    @Nonnull
    public <X> Either<X, B> mapLeft(@Nonnull F<A, X> f) {
        return this.map(f, b -> b);
    }

    @Nonnull
    public <Y> Either<A, Y> mapRight(@Nonnull F<B, Y> f) {
        return this.map(a -> a, f);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public Maybe<A> left() {
        return this.tag == Tag.LEFT ? Maybe.of((A) this.data) : Maybe.empty();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public Maybe<B> right() {
        return this.tag == Tag.RIGHT ? Maybe.of((B) this.data) : Maybe.empty();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public A fromLeft() {
        if (this.tag != Tag.LEFT) {
            throw new RuntimeException("Either fromLeft called on right");
        }
        return (A) this.data;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public B fromRight() {
        if (this.tag != Tag.RIGHT) {
            throw new RuntimeException("Either fromRight called on left");
        }
        return (B) this.data;
    }

    private boolean eq(@Nonnull Either<A, B> either) {
        return either.tag == this.tag && either.data.equals(this.data);
    }

    @Override
    public int hashCode() {
        return (0b10101010 << this.tag.ordinal()) ^ this.data.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(Object object) {
        return this == object || object instanceof Either && this.eq((Either<A, B>) object);
    }
}
