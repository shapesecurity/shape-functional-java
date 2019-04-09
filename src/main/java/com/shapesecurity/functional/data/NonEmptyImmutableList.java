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

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public final class NonEmptyImmutableList<T> extends ImmutableList<T> {
    public static final int HASH_START = HashCodeBuilder.put(HashCodeBuilder.init(), "List");
    @Nonnull
    public final T head;

    @Nonnull
    public final ImmutableList<T> tail;

    protected NonEmptyImmutableList(@Nonnull T head, @Nonnull final ImmutableList<T> tail) {
        super(tail.length + 1);
        this.head = head;
        this.tail = tail;
    }

    @Nonnull
    public ImmutableList<T> tail() {
        return this.tail;
    }

    @Nonnull
    private T[] toObjectArray() {
        int length = this.length;
        Object[] target = new Object[length];
        //noinspection unchecked
        return (T[]) ((ImmutableList<Object>) this).toArray(target);
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

        // Manually expanded tail recursion
        ImmutableList<T> l = this;
        ImmutableList<T> r = (ImmutableList<T>) o;

        if (l.length != r.length) {
            return false;
        }
        
        while (l instanceof NonEmptyImmutableList && r instanceof NonEmptyImmutableList) {
            if (l == r) {
                return true;
            }
            NonEmptyImmutableList<T> nelL = (NonEmptyImmutableList<T>) l;
            NonEmptyImmutableList<T> nelR = (NonEmptyImmutableList<T>) r;
            if (!nelL.head.equals(nelR.head)) {
                return false;
            }
            l = nelL.tail;
            r = nelR.tail;
        }
        return l == r;
    }

    @Nonnull
    @Override
    public <A> A foldLeft(@Nonnull F2<A, ? super T, A> f, @Nonnull A init) {
        ImmutableList<T> list = this;
        while (list instanceof NonEmptyImmutableList) {
            init = f.apply(init, ((NonEmptyImmutableList<T>) list).head);
            list = ((NonEmptyImmutableList<T>) list).tail();
        }
        return init;
    }

    @Nonnull
    @Override
    public <A> A foldRight(@Nonnull F2<? super T, A, A> f, @Nonnull A init) {
        T[] list = this.toObjectArray();
        for (int i = this.length - 1; i >= 0; i--) {
            init = f.apply(list[i], init);
        }
        return init;
    }

    @Nonnull
    public T reduceLeft(@Nonnull F2<T, ? super T, T> f) {
        return this.tail.foldLeft(f, this.head);
    }

    @Nonnull
    public T reduceRight(@Nonnull F2<? super T, T, T> f) {
        return this.reverse().reduceLeft(f.flip());
    }

    @Nonnull
    @Override
    public Maybe<T> maybeHead() {
        return Maybe.of(this.head);
    }

    @Nonnull
    @Override
    public Maybe<T> maybeLast() {
        return Maybe.of(this.last());
    }


    @Nonnull
    @Override
    public Maybe<ImmutableList<T>> maybeTail() {
        return Maybe.of(this.tail);
    }

    @Nonnull
    @Override
    public Maybe<ImmutableList<T>> maybeInit() {
        return Maybe.of(this.init());
    }

    @Nonnull
    public T last() {
        NonEmptyImmutableList<T> other = this;
        while (true) {
            if (other.tail instanceof NonEmptyImmutableList) {
                other = ((NonEmptyImmutableList<T>) other.tail);
            } else {
                return other.head;
            }
        }
    }

    @Nonnull
    public ImmutableList<T> init() {
        return fromBounded(this.toObjectArray(), 0, this.length - 1);
    }

    @Override
    public int count(@Nonnull F<T, Boolean> f) {
        int count = 0;
        ImmutableList<T> list = this;
        for (int i = 0; i < this.length; i++) {
            if (f.apply(((NonEmptyImmutableList<T>) list).head)) {
                count++;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return count;
    }

    @Nonnull
    @Override
    public ImmutableList<T> filter(@Nonnull F<T, Boolean> f) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[this.length];
        ImmutableList<T> list = this;
        int j = 0;
        for (int i = 0; i < this.length; i++) {
            T el = ((NonEmptyImmutableList<T>) list).head;
            if (f.apply(el)) {
                result[j] = el;
                j++;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return fromBounded(result, 0, j);
    }

    @Nonnull
    @Override
    public <B> NonEmptyImmutableList<B> map(@Nonnull F<T, B> f) {
        @SuppressWarnings("unchecked")
        B[] result = (B[]) new Object[this.length];
        ImmutableList<T> list = this;
        for (int i = 0; i < this.length; i++) {
            result[i] = f.apply(((NonEmptyImmutableList<T>) list).head);
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return (NonEmptyImmutableList<B>) from(result);
    }

    @Override
    @Nonnull
    public final <B> NonEmptyImmutableList<B> mapWithIndex(@Nonnull F2<Integer, T, B> f) {
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

    @Nonnull
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

    @Nonnull
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

    @Nonnull
    @Override
    public Maybe<NonEmptyImmutableList<T>> toNonEmptyList() {
        return Maybe.of(this);
    }

    @Nonnull
    @Override
    public <B> Maybe<B> decons(@Nonnull F2<T, ImmutableList<T>, B> f) {
        return Maybe.of(f.apply(this.head, this.tail()));
    }

    @Nonnull
    @Override
    public <B, C> ImmutableList<C> zipWith(@Nonnull F2<T, B, C> f, @Nonnull ImmutableList<B> list) {
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

    @Nonnull
    @Override
    public <B extends T> ImmutableList<T> append(@Nonnull ImmutableList<B> list) {
        if (list.length == 0) {
            return this;
        }
        T[] copy = this.toObjectArray();
        @SuppressWarnings("unchecked")
        ImmutableList<T> listT = (ImmutableList<T>) list;
        for (int i = copy.length - 1; i >= 0; i--) {
            listT = cons(copy[i], listT);
        }
        return listT;
    }

    @Override
    public boolean exists(@Nonnull F<T, Boolean> f) {
        NonEmptyImmutableList<T> list = this;
        while (true) {
            if (f.apply(list.head)) {
                return true;
            }
            if (list.tail instanceof Nil) {
                return false;
            }
            list = ((NonEmptyImmutableList<T>) list.tail);
        }
    }

    @Override
    public boolean contains(@Nonnull T a) {
        NonEmptyImmutableList<T> list = this;
        while (true) {
            if (list.head == a) {
                return true;
            }
            if (list.tail instanceof Nil) {
                return false;
            }
            list = ((NonEmptyImmutableList<T>) list.tail);
        }
    }

    @Nonnull
    @Override
    public Pair<ImmutableList<T>, ImmutableList<T>> span(@Nonnull F<T, Boolean> f) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[this.length];
        ImmutableList<T> list = this;
        int j = 0;
        for (int i = 0; i < this.length; i++) {
            T el = ((NonEmptyImmutableList<T>) list).head;
            if (f.apply(el)) {
                result[j] = el;
                j++;
            } else {
                break;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return new Pair<>(fromBounded(result, 0, j), list);
    }

    @Nonnull
    @Override
    public <B> ImmutableList<B> flatMap(@Nonnull F<T, ImmutableList<B>> f) {
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

    @Override
    public boolean all(@Nonnull F<T, Boolean> f) {
        ImmutableList<T> list = this;
        while (list instanceof NonEmptyImmutableList) {
            if (!f.apply(((NonEmptyImmutableList<T>) list).head)) {
                return false;
            }
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return true;
    }

    @Nonnull
    @Override
    public ImmutableList<T> removeAll(@Nonnull F<T, Boolean> f) {
        return this.filter(x -> !f.apply(x));
    }

    @Nonnull
    @Override
    public NonEmptyImmutableList<T> reverse() {
        ImmutableList<T> list = this;
        ImmutableList<T> acc = empty();
        while (list instanceof NonEmptyImmutableList) {
            acc = acc.cons(((NonEmptyImmutableList<T>) list).head);
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return (NonEmptyImmutableList<T>) acc;
    }

    @Nonnull
    @Override
    public <B, C> Pair<B, ImmutableList<C>> mapAccumL(@Nonnull F2<B, T, Pair<B, C>> f, @Nonnull B acc) {
        @SuppressWarnings("unchecked")
        C[] result = (C[]) new Object[this.length];
        ImmutableList<T> list = this;
        for (int i = 0; i < this.length; i++) {
            Pair<B, C> pair = f.apply(acc, ((NonEmptyImmutableList<T>) list).head);
            acc = pair.left;
            result[i] = pair.right;
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return new Pair<>(acc, from(result));
    }

    @Nonnull
    @Override
    public ImmutableSet<T> uniqByEquality() {
        return ImmutableSet.<T>emptyUsingEquality().putAll(this);
    }

    @Nonnull
    @Override
    public ImmutableSet<T> uniqByIdentity() {
        return ImmutableSet.<T>emptyUsingIdentity().putAll(this);
    }

    @Nonnull
    @Override
    public <B> ImmutableSet<T> uniqByEqualityOn(@Nonnull F<T, B> f) {
        ImmutableSet<B> set = ImmutableSet.<B>emptyUsingEquality().put(f.apply(this.head));
        ImmutableSet<T> out = ImmutableSet.<T>emptyUsingIdentity().put(this.head);
        ImmutableList<T> list = this.tail;
        for (int i = 1; i < this.length; i++) {
            T a = ((NonEmptyImmutableList<T>) list).head;
            B b = f.apply(a);
            if (!set.contains(b)) {
                out = out.put(a);
            }
            set = set.put(b);
            list = ((NonEmptyImmutableList<T>) list).tail;
        }
        return out;
    }

    @Override
    protected int calcHashCode() {
        NonEmptyImmutableList<T> list = this;
        int[] hashStack = new int[list.length];
        ImmutableList<T>[] sublistStack = (ImmutableList<T>[]) new ImmutableList[list.length];
        int stackIndex = 0;
        int hash = HASH_START;
        while (true) {
            Integer cachedHashCode = list.getCachedHashCode();
            if (cachedHashCode != null) {
                hash = cachedHashCode;
                break;
            }
            hashStack[stackIndex] = list.head.hashCode();
            sublistStack[stackIndex] = list;
            ++stackIndex;
            if (list.tail instanceof NonEmptyImmutableList) {
                list = (NonEmptyImmutableList<T>) list.tail;
            } else {
                break;
            }
        }
        --stackIndex;

        for (; stackIndex >= 0; --stackIndex) {
            hash = HashCodeBuilder.put(hash, hashStack[stackIndex]);
            sublistStack[stackIndex].setCachedHashCode(hash);
        }

        return hash;
    }
}

