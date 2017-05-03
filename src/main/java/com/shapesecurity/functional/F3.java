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

import javax.annotation.Nonnull;

import javax.annotation.CheckReturnValue;

@CheckReturnValue
@FunctionalInterface
public interface F3<A, B, C, R> {
    @Nonnull
    R apply(@Nonnull A a, @Nonnull B b, @Nonnull C c);

    @Nonnull
    default R applyTuple(@Nonnull Tuple3<A, B, C> args) {
        return this.apply(args.a, args.b, args.c);
    }

    @Nonnull
    default F2<B, C, R> curry(@Nonnull final A a) {
        return (b, c) -> this.apply(a, b, c);
    }

    @Nonnull
    default F<A, F<B, F<C, R>>> curry() {
        return a -> b -> c -> this.apply(a, b, c);
    }
}
