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

package com.shapesecurity.functional;

import com.shapesecurity.functional.data.HashCodeBuilder;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@CheckReturnValue
public final class Pair<A, B> {
    public final A left;
    public final B right;

    @Deprecated
    public final A a;
    @Deprecated
    public final B b;

    /**
     * Constructor method to utilize type inference.
     * @param left first component
     * @param right second component
     * @param <A> type of the first component
     * @param <B> type of the second component
     * @return the pair
     */
    @Nonnull
    public static <A, B> Pair<A, B> of(A left, B right) {
        return new Pair<>(left, right);
    }

    @Nonnull
    @Deprecated
    public static <A, B> Pair<A, B> make(A left, B right) {
        return Pair.of(left, right);
    }

    public Pair(A left, B right) {
        super();
        this.left = this.a = left;
        this.right = this.b = right;

    }

    public A left() {
        return this.left;
    }

    public B right() {
        return this.right;
    }

    @Nonnull
    public Pair<B, A> swap() {
        return new Pair<>(this.right, this.left);
    }

    @Nonnull
    public <A1, B1> Pair<A1, B1> map(@Nonnull F<A, A1> fA, @Nonnull F<B, B1> fB) {
        return new Pair<>(fA.apply(this.left), fB.apply(this.right));
    }

    @Nonnull
    public <T> T map(@Nonnull F2<A, B, T> f) {
        return f.apply(this.left, this.right);
    }

    @Nonnull
    public <A1> Pair<A1, B> mapLeft(@Nonnull F<A, A1> f) {
        return new Pair<>(f.apply(this.left), this.right);
    }

    @Nonnull
    @Deprecated
    public <A1> Pair<A1, B> mapA(@Nonnull F<A, A1> f) {
        return this.mapLeft(f);
    }

    @Nonnull
    public <B1> Pair<A, B1> mapRight(@Nonnull F<B, B1> f) {
        return new Pair<>(this.left, f.apply(this.right));
    }

    @Nonnull
    @Deprecated
    public <B1> Pair<A, B1> mapB(@Nonnull F<B, B1> f) {
        return this.mapRight(f);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Pair &&
                ((Pair<A, B>) obj).left.equals(this.left) &&
                ((Pair<A, B>) obj).right.equals(this.right);
    }

    @Override
    public int hashCode() {
        int hash = HashCodeBuilder.put(HashCodeBuilder.init(), "Pair");
        hash = HashCodeBuilder.put(hash, this.left);
        return HashCodeBuilder.put(hash, this.right);
    }
}

