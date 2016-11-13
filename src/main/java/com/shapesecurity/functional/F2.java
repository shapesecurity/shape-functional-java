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

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface F2<A, B, R> {
    @NotNull
    R apply(@NotNull A a, @NotNull B b);

    @NotNull
    default R applyTuple(@NotNull Pair<A, B> args) {
        return this.apply(args.left, args.right);
    }

    @NotNull
    default F<B, R> curry(@NotNull final A a) {
        return b -> this.apply(a, b);
    }

    @NotNull
    default F<A, F<B, R>> curry() {
        return a -> b -> this.apply(a, b);
    }

    @NotNull
    default F2<B, A, R> flip() {
        return (b, a) -> this.apply(a, b);
    }
}
