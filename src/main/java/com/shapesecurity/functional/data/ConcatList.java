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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Stack;
import java.util.function.Consumer;

import com.shapesecurity.functional.Effect;
import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConcatList<T> implements Iterable<T> {
    private static final Empty<Object> EMPTY = new Empty<>();
    private static BinaryTreeMonoid<Object> MONOID = new BinaryTreeMonoid<>();
    public final int length;
    final boolean isBalanced;

    protected ConcatList(int length, boolean isBalanced) {
        this.length = length;
        this.isBalanced = isBalanced;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> ConcatList<T> empty() {
        return (ConcatList<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Deprecated
    public static <T> ConcatList<T> nil() {
        return ConcatList.empty();
    }

    @SafeVarargs
    public static <T> ConcatList<T> of(T... elements) {
        if (elements.length == 0) {
            return empty();
        }
        return ofInternal(elements, 0, elements.length);
    }

    public abstract boolean isEmpty();

    @NotNull
    public static <T> ConcatList<T> fromList(@NotNull List<T> list) {
        return fromListInternal(list.iterator(), 0, list.size());
    }

    @NotNull
    private static <T> ConcatList<T> ofInternal(@NotNull T[] elements, int start, int end) {
        if (start == end) {
            return empty();
        } else if (start + 1 == end) {
            return single(elements[start]);
        } else {
            int mid = (start + end) / 2; // start < mid && mid < end
            return ofInternal(elements, start, mid).append(ofInternal(elements, mid, end));
        }
    }

    @NotNull
    private static <T> ConcatList<T> fromListInternal(@NotNull Iterator<T> elements, int start, int end) {
        if (start == end) {
            return empty();
        } else if (start + 1 == end) {
            return single(elements.next());
        } else {
            int mid = (start + end) / 2; // start < mid && mid < end
            return fromListInternal(elements, start, mid).append(fromListInternal(elements, mid, end));
        }
    }

    public abstract ConcatList<T> balanced();

    @NotNull
    public final Maybe<Pair<ConcatList<T>, ConcatList<T>>> split(int index) {
        if (index < 0 || index > this.length) {
            return Maybe.empty();
        }
        if (index == 0) {
            return Maybe.of(Pair.of(empty(), this));
        }
        if (index == this.length) {
            return Maybe.of(Pair.of(this, empty()));
        }
        return Maybe.of(this.splitInternal(index));
    }

    abstract Pair<ConcatList<T>, ConcatList<T>> splitInternal(int index);

    @NotNull
    public static <T> ConcatList<T> single(@NotNull T scope) {
        return new Leaf<>(scope);
    }

    @SuppressWarnings("unchecked")
    public static <T> Monoid<ConcatList<T>> monoid() {
        return (BinaryTreeMonoid<T>) MONOID;
    }

    @NotNull
    public final ImmutableList<T> toList() {
        return this.toList(ImmutableList.empty());
    }

    protected abstract ImmutableList<T> toList(@NotNull ImmutableList<T> acc);

    @NotNull
    public final <B> B foldLeft(@NotNull F2<B, ? super T, B> f, @NotNull B init) {
        if (this.isEmpty()) {
            return init;
        }
        @SuppressWarnings("unchecked")
        B[] result = (B[]) new Object[]{init};
        this.forEach(n -> result[0] = f.apply(result[0], n));
        return result[0];
    }

    @NotNull
    public final <B> B foldRight(@NotNull F2<? super T, B, B> f, @NotNull B init) {
        // Manually expanded recursion
        Deque<ConcatList<T>> stack = new ArrayDeque<>(this.length);
        stack.add(this);
        while (!stack.isEmpty()) {
            ConcatList<T> curr = stack.pop();
            if (curr instanceof Fork) {
                Fork<T> fork = (Fork<T>) curr;
                stack.push(fork.left);
                stack.push(fork.right);
            } else if (curr instanceof Leaf) {
                init = f.apply(((Leaf<T>) curr).data, init);
            }
        }
        return init;
    }

    /**
     * @deprecated Use {@link #forEach(Consumer)} instead
     */
    @Deprecated
    public final void foreach(@NotNull Effect<T> f) {
        this.forEach(f::e);
    }

    @Override
    public final void forEach(@NotNull Consumer<? super T> action) {
        // Manually expanded recursion
        @SuppressWarnings("unchecked")
        ConcatList<T>[] stack = new ConcatList[this.length];
        int i = 0;
        stack[i++] = this;
        while (i > 0) {
            ConcatList<T> curr = stack[--i];
            if (curr instanceof Fork) {
                Fork<T> fork = (Fork<T>) curr;
                stack[i++] = fork.right;
                stack[i++] = fork.left;
            } else if (curr instanceof Leaf) {
                action.accept(((Leaf<T>) curr).data);
            }
        }
    }

    @NotNull
    public abstract ConcatList<T> append(@NotNull ConcatList<? extends T> rhs);

    @NotNull
    public final ConcatList<T> append1(@NotNull T element) {
        return this.append(ConcatList.single(element));
    }

    public final boolean exists(@NotNull F<T, Boolean> f) {
        return this.find(f).isJust();
    }

    @NotNull
    public final Maybe<T> find(@NotNull F<T, Boolean> f) {
        // Manually expanded recursion
        Deque<ConcatList<T>> stack = new ArrayDeque<>(this.length);
        stack.add(this);
        while (!stack.isEmpty()) {
            ConcatList<T> curr = stack.pop();
            if (curr instanceof Fork) {
                Fork<T> fork = (Fork<T>) curr;
                stack.push(fork.right);
                stack.push(fork.left);
            } else if (curr instanceof Leaf) {
                if (f.apply(((Leaf<T>) curr).data)) {
                    return Maybe.of(((Leaf<T>) curr).data);
                }
            }
        }
        return Maybe.empty();
    }

    @NotNull
    public final ConcatList<T> reverse() {
        if (this instanceof Empty) {
            return this;
        }
        ArrayList<T> list = new ArrayList<>(this.length);
        Deque<ConcatList<T>> stack = new ArrayDeque<>(this.length);
        stack.add(this);
        while (!stack.isEmpty()) {
            ConcatList<T> curr = stack.pop();
            if (curr instanceof Fork) {
                Fork<T> fork = (Fork<T>) curr;
                // reversed
                stack.push(fork.left);
                stack.push(fork.right);
            } else if (curr instanceof Leaf) {
                list.add(((Leaf<T>) curr).data);
            }
        }
        return ConcatList.fromList(list);
    }

    @NotNull
    public final Maybe<T> index(int index) {
        ConcatList<T> list = this;
        if (index >= this.length) {
            return Maybe.empty();
        }
        // list is not an Empty
        while (list instanceof Fork) {
            ConcatList<T> left = ((Fork<T>) list).left;
            if (index < left.length) {
                list = left;
            } else {
                index -= left.length;
                list = ((Fork<T>) list).right;
            }
        }
        return Maybe.of(((Leaf<T>) list).data);
    }

    @Nullable
    abstract ConcatList<T> updateInternal(int index, @NotNull T element);

    @NotNull
    public final Maybe<ConcatList<T>> update(int index, @NotNull T element) {
        return Maybe.fromNullable(this.updateInternal(index, element));
    }

    public final static class Empty<T> extends ConcatList<T> {
        private Empty() {
            super(0, true);
        }

        @NotNull
        @Override
        protected ImmutableList<T> toList(@NotNull ImmutableList<T> acc) {
            return acc;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ConcatList<T> balanced() {
            return this;
        }

        @Override
        Pair<ConcatList<T>, ConcatList<T>> splitInternal(int index) {
            return Pair.of(this, this);
        }

        @SuppressWarnings("unchecked")
        @NotNull
        @Override
        public ConcatList<T> append(@NotNull ConcatList<? extends T> rhs) {
            return (ConcatList<T>) rhs;
        }

        @Nullable
        @Override
        public ConcatList<T> updateInternal(int index, @NotNull T element) {
            return null;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    return null;
                }
            };
        }
    }

    public final static class Leaf<T> extends ConcatList<T> {
        @NotNull
        public final T data;

        private Leaf(@NotNull T data) {
            super(1, true);
            this.data = data;
        }

        @NotNull
        @Override
        protected ImmutableList<T> toList(@NotNull ImmutableList<T> acc) {
            return acc.cons(this.data);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ConcatList<T> balanced() {
            return this;
        }

        @Override
        Pair<ConcatList<T>, ConcatList<T>> splitInternal(int index) {
            if (index == 0) {
                return Pair.of(ConcatList.empty(), this);
            } else {
                return Pair.of(this, ConcatList.empty());
            }
        }

        @SuppressWarnings("unchecked")
        @NotNull
        @Override
        public ConcatList<T> append(@NotNull ConcatList<? extends T> rhs) {
            if (rhs instanceof Empty) {
                return this;
            }
            return new Fork<>(this, (ConcatList<T>) rhs);
        }

        @Nullable
        @Override
        ConcatList<T> updateInternal(int index, @NotNull T element) {
            return index == 0 ? single(element) : null;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.singleton(this.data).iterator();
        }
    }

    public final static class Fork<T> extends ConcatList<T> {
        @NotNull
        public final ConcatList<T> left, right;

        private Fork(@NotNull ConcatList<T> left, @NotNull ConcatList<T> right) {
            super(left.length + right.length, left.isBalanced && right.isBalanced && (left.length + right.length < 16 || left.length > right.length / 2 && left.length < right.length * 2));
            this.left = left;
            this.right = right;
        }

        @NotNull
        @Override
        protected ImmutableList<T> toList(@NotNull ImmutableList<T> acc) {
            return this.left.toList(this.right.toList(acc));
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ConcatList<T> balanced() {
            if (this.isBalanced) {
                return this;
            }
            return ConcatList.fromListInternal(this.iterator(), 0, this.length);
        }

        @Override
        @NotNull
        Pair<ConcatList<T>, ConcatList<T>> splitInternal(int index) {
            // Manually expanded zipper access
            Stack<ConcatList<T>> zippers1 = new Stack<>();
            Stack<Boolean> zippers2 = new Stack<>();
            ConcatList<T> list = this;
            while (list instanceof Fork) {
                ConcatList<T> left = ((Fork<T>) list).left;
                if (index < left.length) {
                    zippers1.push(((Fork<T>) list).right);
                    zippers2.push(false);
                    list = left;
                } else {
                    index -= left.length;
                    zippers1.push(left);
                    zippers2.push(true);
                    list = ((Fork<T>) list).right;
                }
            }
            // list is the first of the right hand side
            Pair<ConcatList<T>, ConcatList<T>> result = Pair.of(empty(), list);
            while (!zippers1.isEmpty()) {
                if (zippers2.pop()) {
                    result = Pair.of(
                            zippers1.pop().append(result.left()),
                            result.right());
                } else {
                    result = Pair.of(
                            result.left(),
                            result.right().append(zippers1.pop()));
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @NotNull
        @Override
        public ConcatList<T> append(@NotNull ConcatList<? extends T> rhs) {
            if (rhs instanceof Empty) {
                return this;
            }
            return new Fork<>(this, (ConcatList<T>) rhs);
        }

        @Nullable
        @Override
        ConcatList<T> updateInternal(int index, @NotNull T element) {
            // Manually expanded zipper access
            if (index >= this.length) {
                return null;
            }
            Stack<ConcatList<T>> zippers1 = new Stack<>();
            Stack<Boolean> zippers2 = new Stack<>();
            ConcatList<T> list = this;
            while (list instanceof Fork) {
                ConcatList<T> left = ((Fork<T>) list).left;
                if (index < left.length) {
                    zippers1.push(((Fork<T>) list).right);
                    zippers2.push(false);
                    list = left;
                } else {
                    index -= left.length;
                    zippers1.push(left);
                    zippers2.push(true);
                    list = ((Fork<T>) list).right;
                }
            }
            ConcatList<T> l = ConcatList.single(element);
            while (!zippers1.isEmpty()) {
                l = zippers2.pop() ? zippers1.pop().append(l) : l.append(zippers1.pop());
            }
            return l;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @SuppressWarnings("unchecked")
                private final ConcatList<T>[] stack = new ConcatList[Fork.this.length];
                int i = 0;

                {
                    this.stack[this.i++] = Fork.this;
                }

                @Override
                public boolean hasNext() {
                    return this.i > 0;
                }

                @Override
                public T next() {
                    while (this.i > 0) {
                        ConcatList<T> curr = this.stack[--this.i];
                        if (curr instanceof Fork) {
                            Fork<T> fork = (Fork<T>) curr;
                            this.stack[this.i++] = fork.right;
                            this.stack[this.i++] = fork.left;
                        } else if (curr instanceof Leaf) {
                            return ((Leaf<T>) curr).data;
                        }
                    }
                    throw new NoSuchElementException();
                }
            };
        }
    }

    private final static class ConcatListSplitIterator<T> implements Spliterator<T> {
        @NotNull
        private final Deque<ConcatList<T>> stack;
        private int size;

        private ConcatListSplitIterator(@NotNull Deque<ConcatList<T>> stack, int size) {
            this.stack = stack;
            this.size = size;
        }


        private ConcatListSplitIterator(@NotNull ConcatList<T> list) {
            // Manually expanded recursion
            this.stack = new ArrayDeque<>(list.length);
            this.stack.add(list);
            this.size = list.length;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            while (!this.stack.isEmpty()) {
                ConcatList<T> curr = this.stack.pop();
                if (curr instanceof Fork) {
                    Fork<T> fork = (Fork<T>) curr;
                    this.stack.push(fork.right);
                    this.stack.push(fork.left);
                } else if (curr instanceof Leaf) {
                    this.size--;
                    action.accept(((Leaf<T>) curr).data);
                    return true;
                }
            }
            return false;
        }

        @Override
        public Spliterator<T> trySplit() {
            Deque<ConcatList<T>> newDequeue = new ArrayDeque<>();
            newDequeue.addAll(this.stack);
            return new ConcatListSplitIterator<>(newDequeue, this.size);
        }

        @Override
        public long estimateSize() {
            return this.size;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | ORDERED | SUBSIZED | SIZED;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (!this.stack.isEmpty()) {
                ConcatList<T> curr = this.stack.pop();
                if (curr instanceof Fork) {
                    Fork<T> fork = (Fork<T>) curr;
                    this.stack.push(fork.right);
                    this.stack.push(fork.left);
                } else if (curr instanceof Leaf) {
                    this.size--;
                    action.accept(((Leaf<T>) curr).data);
                }
            }
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return new ConcatListSplitIterator<>(this);
    }

    private static class BinaryTreeMonoid<T> implements Monoid<ConcatList<T>> {
        @NotNull
        @Override
        public ConcatList<T> identity() {
            return new Empty<>();
        }

        @NotNull
        @Override
        public ConcatList<T> append(ConcatList<T> a, ConcatList<T> b) {
            return a.append(b);
        }
    }
}
