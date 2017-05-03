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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@CheckReturnValue
@FunctionalInterface
public interface F4<A, B, C, D, R> {
    @Nonnull
    R apply(@Nonnull A a, @Nonnull B b, @Nonnull C c, @Nonnull D d);

    @Nonnull
    default R applyTuple(@Nonnull Tuple4<A, B, C, D> args) {
        return this.apply(args.a, args.b, args.c, args.d);
    }

    @Nonnull
    default F3<B, C, D, R> curry(@Nonnull final A a) {
        return (b, c, d) -> this.apply(a, b, c, d);
    }

    @Nonnull
    default F<A, F<B, F<C, F<D, R>>>> curry() {
        return a -> b -> c -> d -> this.apply(a, b, c, d);
    }
}
