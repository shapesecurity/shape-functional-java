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

import java.util.ArrayList;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import org.jetbrains.annotations.NotNull;

public final class NonEmptyImmutableList<T> extends ImmutableList<T> {
    @NotNull
    public final T head;

    @NotNull
    public final ImmutableList<T> tail;

    protected NonEmptyImmutableList(@NotNull T head, @NotNull final ImmutableList<T> tail) {
        super(tail.length + 1);
        this.head = head;
        this.tail = tail;
    }

    @NotNull
    public ImmutableList<T> tail() {
        return tail;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NonEmptyImmutableList)) {
            return false;
        }

        NonEmptyImmutableList<T> list = (NonEmptyImmutableList<T>) o;
        return this.head.equals(list.head) && this.tail().equals(list.tail());
    }

    @NotNull
    @Override
    public <A> A foldLeft(@NotNull F2<A, ? super T, A> f, @NotNull A init) {
        ImmutableList<T> list = this;
        while (list instanceof NonEmptyImmutableList) {
            init = f.apply(init, ((NonEmptyImmutableList<T>) list).head);
            list = ((NonEmptyImmutableList<T>) list).tail();
        }
        return init;
    }

    @NotNull
    @Override
    public <A> A foldRight(@NotNull F2<? super T, A, A> f, @NotNull A init) {
        return f.apply(this.head, this.tail().foldRight(f, init));
    }

    @NotNull
    public T reduceLeft(@NotNull F2<T, ? super T, T> f) {
        return this.tail.foldLeft(f, this.head);
    }

    @NotNull
    public T reduceRight(@NotNull F2<? super T, T, T> f) {
        return this.init().foldRight(f, this.last());
    }

    @NotNull
    @Override
    public Maybe<T> maybeHead() {
        return Maybe.just(this.head);
    }

    @NotNull
    @Override
    public Maybe<T> maybeLast() {
        if (this.tail().isEmpty()) {
            return Maybe.just(this.head);
        }
        return this.tail().maybeLast();
    }

    @NotNull
    @Override
    public Maybe<ImmutableList<T>> maybeTail() {
        return Maybe.just(this.tail());
    }

    @NotNull
    @Override
    public Maybe<ImmutableList<T>> maybeInit() {
        if (this.tail().isEmpty()) {
            return Maybe.just(empty());
        }
        return this.tail().maybeInit().map(t -> t.cons(this.head));
    }

    @NotNull
    public final T last() {
        NonEmptyImmutableList<T> nel = this;
        while (true) {
            if (nel.tail().isEmpty()) {
                return nel.head;
            }
            nel = (NonEmptyImmutableList<T>) nel.tail();
        }
    }

    @NotNull
    public final ImmutableList<T> init() {
        if (this.tail().isEmpty()) {
            return empty();
        }
        return cons(this.head, ((NonEmptyImmutableList<T>) this.tail()).init());
    }

    @NotNull
    @Override
    public ImmutableList<T> filter(@NotNull F<T, Boolean> f) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[length];
        ImmutableList<T> list = this;
        int j = 0;
        for (int i = 0; i < length; i++) {
            T el = ((NonEmptyImmutableList<T>) list).head;
            if (f.apply(el)) {
                result[j] = el;
                j++;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return fromBounded(result, 0, j);
    }

    @NotNull
    @Override
    public <B> NonEmptyImmutableList<B> map(@NotNull F<T, B> f) {
        @SuppressWarnings("unchecked")
        B[] result = (B[]) new Object[length];
        ImmutableList<T> list = this;
        for (int i = 0; i < length; i++) {
            result[i] = f.apply(((NonEmptyImmutableList<T>) list).head);
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return (NonEmptyImmutableList<B>) from(result);
    }

    @Override
    @NotNull
    public final <B> NonEmptyImmutableList<B> mapWithIndex(@NotNull F2<Integer, T, B> f) {
        int length = this.length;
        @SuppressWarnings("unchecked")
        B[] result = (B[]) new Object[length];
        ImmutableList<T> list = this;
        for (int i = 0; i < length; i++) {
            result[i] = f.apply(i, ((NonEmptyImmutableList<T>) list).head);
            list = ((NonEmptyImmutableList<T>) list).tail();
        }
        return (NonEmptyImmutableList<B>) from(result);
    }

    @NotNull
    @Override
    public ImmutableList<T> take(int n) {
        if (n <= 0) {
            return empty();
        }
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[n];
        ImmutableList<T> list = this;
        for (int i = 0; i < n; i++) {
            result[i] = ((NonEmptyImmutableList<T>) list).head;
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return from(result);
    }

    @NotNull
    @Override
    public ImmutableList<T> drop(int n) {
        if (n <= 0) {
            return this;
        }
        ImmutableList<T> list = this;
        while (n > 0) {
            if (list instanceof NonEmptyImmutableList) {
                list = ((NonEmptyImmutableList<T>) list).tail;
                n--;
            }
        }
        return list;
    }

    @NotNull
    @Override
    public Maybe<NonEmptyImmutableList<T>> toNonEmptyList() {
        return Maybe.just(this);
    }

    @NotNull
    @Override
    public <B> Maybe<B> decons(@NotNull F2<T, ImmutableList<T>, B> f) {
        return Maybe.just(f.apply(this.head, this.tail()));
    }

    @NotNull
    @Override
    public <B, C> ImmutableList<C> zipWith(@NotNull F2<T, B, C> f, @NotNull ImmutableList<B> list) {
        ImmutableList<T> list1 = this;
        ImmutableList<B> list2 = list;
        int n = Math.min(list1.length, list2.length);
        @SuppressWarnings("unchecked")
        C[] result = (C[]) new Object[n];
        for (int i = 0; i < n; i++) {
            result[i] = f.apply(((NonEmptyImmutableList<T>) list1).head, ((NonEmptyImmutableList<B>) list2).head);
            list1 = ((NonEmptyImmutableList<T>) list1).tail;
            list2 = ((NonEmptyImmutableList<B>) list2).tail;
        }
        return from(result);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NotNull
    @Override
    public <B extends T> ImmutableList<T> append(@NotNull ImmutableList<B> list) {
        if (list.length == 0) {
            return this;
        }
        @SuppressWarnings("unchecked")
        T[] copy = toArray((T[]) new Object[length]);
        @SuppressWarnings("unchecked")
        ImmutableList<T> listT = (ImmutableList<T>) list;
        for (int i = copy.length - 1; i >= 0; i--) {
            listT = cons(copy[i], listT);
        }
        return listT;
    }

    @Override
    public boolean exists(@NotNull F<T, Boolean> f) {
        NonEmptyImmutableList<T> list = this;
        while (true) {
            if (f.apply(list.head)) {
                return true;
            }
            if (list.tail instanceof EmptyImmutableList) {
                return false;
            }
            list = ((NonEmptyImmutableList<T>) list.tail);
        }
    }

    @Override
    public boolean contains(@NotNull T a) {
        NonEmptyImmutableList<T> list = this;
        while (true) {
            if (list.head == a) {
                return true;
            }
            if (list.tail instanceof EmptyImmutableList) {
                return false;
            }
            list = ((NonEmptyImmutableList<T>) list.tail);
        }
    }

    @NotNull
    @Override
    public Pair<ImmutableList<T>, ImmutableList<T>> span(@NotNull F<T, Boolean> f) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[length];
        ImmutableList<T> list = this;
        int j = 0;
        for (int i = 0; i < length; i++) {
            T el = ((NonEmptyImmutableList<T>) list).head;
            if (f.apply(el)) {
                result[j] = el;
                j++;
            } else {
                break;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return Pair.make(fromBounded(result, 0, j), list);
    }

    @NotNull
    @Override
    public <B> ImmutableList<B> flatMap(@NotNull F<T, ImmutableList<B>> f) {
        ArrayList<B> result = new ArrayList<>();
        ImmutableList<T> list = this;
        while (list instanceof NonEmptyImmutableList) {
            ImmutableList<B> bucket = f.apply(((NonEmptyImmutableList<T>) list).head);
            while (bucket instanceof NonEmptyImmutableList) {
                result.add(((NonEmptyImmutableList<B>) bucket).head);
                bucket = ((NonEmptyImmutableList<B>) bucket).tail;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return from(result);
    }

    @NotNull
    @Override
    public ImmutableList<T> removeAll(@NotNull F<T, Boolean> f) {
        return filter(x -> !f.apply(x));
    }

    @NotNull
    @Override
    public NonEmptyImmutableList<T> reverse() {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[length];
        ImmutableList<T> list = this;
        for (int i = 0; i < length; i++) {
            result[length - i - 1] = ((NonEmptyImmutableList<T>) list).head;
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return (NonEmptyImmutableList<T>) from(result);
    }

    @NotNull
    @Override
    public <B, C> Pair<B, ImmutableList<C>> mapAccumL(@NotNull F2<B, T, Pair<B, C>> f, @NotNull B acc) {
        @SuppressWarnings("unchecked")
        C[] result = (C[]) new Object[length];
        ImmutableList<T> list = this;
        for (int i = 0; i < length; i++) {
            Pair<B, C> pair = f.apply(acc, ((NonEmptyImmutableList<T>) list).head);
            acc = pair.a;
            result[i] = pair.b;
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return new Pair<>(acc, from(result));
    }

    @Override
    protected int calcHashCode() {
        int start = HashCodeBuilder.init();
        start = HashCodeBuilder.put(start, "List");
        start = HashCodeBuilder.put(start, head);
        return HashCodeBuilder.put(start, tail);
    }
}

