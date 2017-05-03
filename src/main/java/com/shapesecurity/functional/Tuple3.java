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

import javax.annotation.Nonnull;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
public final class Tuple3<A, B, C> {
    @Nonnull
    public final A a;
    @Nonnull
    public final B b;
    @Nonnull
    public final C c;

    public Tuple3(@Nonnull A a, @Nonnull B b, @Nonnull C c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Nonnull
    public <A1> Tuple3<A1, B, C> mapA(@Nonnull F<A, A1> f) {
        return new Tuple3<>(f.apply(this.a), this.b, this.c);
    }

    @Nonnull
    public <B1> Tuple3<A, B1, C> mapB(@Nonnull F<B, B1> f) {
        return new Tuple3<>(this.a, f.apply(this.b), this.c);
    }

    @Nonnull
    public <C1> Tuple3<A, B, C1> mapC(@Nonnull F<C, C1> f) {
        return new Tuple3<>(this.a, this.b, f.apply(this.c));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Tuple3 &&
                ((Tuple3<A, B, C>) obj).a.equals(this.a) &&
                ((Tuple3<A, B, C>) obj).b.equals(this.b) &&
                ((Tuple3<A, B, C>) obj).c.equals(this.c);
    }

    @Override
    public int hashCode() {
        int hash = HashCodeBuilder.put(HashCodeBuilder.init(), "Tuple3");
        hash = HashCodeBuilder.put(hash, this.a);
        hash = HashCodeBuilder.put(hash, this.b);
        return HashCodeBuilder.put(hash, this.c);
    }
}

