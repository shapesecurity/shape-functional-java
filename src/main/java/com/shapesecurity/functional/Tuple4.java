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
public final class Tuple4<A, B, C, D> {
    @Nonnull
    public final A a;
    @Nonnull
    public final B b;
    @Nonnull
    public final C c;
    @Nonnull
    public final D d;

    /**
     * Constructor method to utilize type inference.
     * @param a first component
     * @param b second component
     * @param c third component
     * @param d fourth component
     * @param <A> type of the first component
     * @param <B> type of the second component
     * @param <C> type of the third component
     * @param <D> type of the fourth component
     * @return the pair
     */
    @Nonnull
    public static <A, B, C, D> Tuple4<A, B, C, D> of(@Nonnull A a, @Nonnull B b, @Nonnull C c, @Nonnull D d) {
        return new Tuple4<>(a, b, c, d);
    }

    public Tuple4(@Nonnull A a, @Nonnull B b, @Nonnull C c, @Nonnull D d) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Nonnull
    public <A1> Tuple4<A1, B, C, D> mapA(@Nonnull F<A, A1> f) {
        return new Tuple4<>(f.apply(this.a), this.b, this.c, this.d);
    }

    @Nonnull
    public <B1> Tuple4<A, B1, C, D> mapB(@Nonnull F<B, B1> f) {
        return new Tuple4<>(this.a, f.apply(this.b), this.c, this.d);
    }

    @Nonnull
    public <C1> Tuple4<A, B, C1, D> mapC(@Nonnull F<C, C1> f) {
        return new Tuple4<>(this.a, this.b, f.apply(this.c), this.d);
    }

    @Nonnull
    public <D1> Tuple4<A, B, C, D1> mapD(@Nonnull F<D, D1> f) {
        return new Tuple4<>(this.a, this.b, this.c, f.apply(this.d));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Tuple4 &&
                ((Tuple4<A, B, C, D>) obj).a.equals(this.a) &&
                ((Tuple4<A, B, C, D>) obj).b.equals(this.b) &&
                ((Tuple4<A, B, C, D>) obj).c.equals(this.c) &&
                ((Tuple4<A, B, C, D>) obj).d.equals(this.d);
    }

    @Override
    public int hashCode() {
        int hash = HashCodeBuilder.put(HashCodeBuilder.init(), "Tuple4");
        hash = HashCodeBuilder.put(hash, this.a);
        hash = HashCodeBuilder.put(hash, this.b);
        hash = HashCodeBuilder.put(hash, this.c);
        return HashCodeBuilder.put(hash, this.d);
    }
}

