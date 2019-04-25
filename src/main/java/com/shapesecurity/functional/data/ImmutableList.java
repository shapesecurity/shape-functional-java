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
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * An immutable singly linked list implementation. None of the operations in {@link ImmutableList}
 * changes the list itself. Therefore you can freely share the list in your system. <p> This is a
 * "classical" list implementation that the list is only allowed to prepend and to remove the first
 * element efficiently. Therefore it is essentially equivalent to a stack. <p> It is either an empty
 * list, or a record that contains the first element (called "head") and a list that follows(called
 * "tail"). With the assumption that all the elements in the list are also immutable, the sharing of
 * the tails is possible. <p> For a data structure that allows O(1) concatenation, try {@link
 * ConcatList}. A BinaryTree can be converted into a List in O(n) time.
 *
 * @param <A> The super type of all the elements.
 */
@CheckReturnValue
public abstract class ImmutableList<A> implements Iterable<A> {
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    private static final ImmutableList<Object> EMPTY = new Nil<>();
    @Nullable
    private volatile Integer hashCode = null;

    /**
     * The length of the list.
     */
    public final int length;

    // package local
    ImmutableList(int length) {
        super();
        this.length = length;
    }

    /**
     * Creating ImmutableList from an ArrayList.
     *
     * @param list      The {@link java.util.ArrayList} to construct the {@link ImmutableList}
     *                  from.
     * @param <A>       The type of the elements of the list.
     * @return a new {@link ImmutableList} that is comprised of all the elements in the {@link
     * java.util.ArrayList}.
     */
    @Nonnull
    public static <A> ImmutableList<A> from(@Nonnull ArrayList<A> list) {
        ImmutableList<A> l = empty();
        int size = list.size();
        for (int i = size - 1; i >= 0; i--) {
            l = cons(list.get(i), l);
        }
        return l;
    }

    /**
     * Creating ImmutableList from a Deque. Generally used for {@link java.util.LinkedList}, but applicable to reverse iterable types in general.
     *
     * @param list      The {@link java.util.Deque} to construct the {@link ImmutableList}
     *                  from.
     * @param <A>       The type of the elements of the list.
     * @return a new {@link ImmutableList} that is comprised of all the elements in the {@link
     * java.util.Deque}.
     */
    @Nonnull
    public static <A> ImmutableList<A> from(@Nonnull Deque<A> list) {
        ImmutableList<A> l = empty();
        for (Iterator<A> iterator = list.descendingIterator(); iterator.hasNext();) {
            A item = iterator.next();
            l = cons(item, l);
        }
        return l;
    }

    /**
     * Creating ImmutableList from an Iterable.
     *
     * @param list      The {@link java.lang.Iterable} to construct the {@link ImmutableList}
     *                  from.
     * @param <A>       The type of the elements of the list.
     * @return a new {@link ImmutableList} that is comprised of all the elements in the {@link
     * java.util.ArrayList}.
     */
    @Nonnull
    public static <A> ImmutableList<A> from(@Nonnull Iterable<A> list) {
        ImmutableList<A> l = empty();
        for (A item : list) {
            l = cons(item, l);
        }
        return l.reverse();
    }

    @Nonnull
    @Deprecated
    public static <A> ImmutableList<A> list(@Nonnull List<A> arrayList) {
        return ImmutableList.from(arrayList);
    }

    /**
     * Prepends "cons" an head element to a {@link ImmutableList}.
     *
     * @param head The head element to be prepended to the {@link ImmutableList}.
     * @param tail The {@link ImmutableList} to be prepended to.
     * @param <T>  The super type of both the element and the {@link ImmutableList}
     * @return A {@link ImmutableList} that is comprised of the head then the tail.
     */
    public static <T> NonEmptyImmutableList<T> cons(@Nonnull T head, @Nonnull ImmutableList<T> tail) {
        return new NonEmptyImmutableList<>(head, tail);
    }

    // Construction

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> empty() {
        return (ImmutableList<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> ImmutableList<T> nil() {
        return ImmutableList.empty();
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> ImmutableList<T> list() {
        return ImmutableList.empty();
    }

    /**
     * A helper constructor to create a {@link NonEmptyImmutableList}.
     *
     * @param head The first element
     * @param el   rest of the elements
     * @param <T>  The type of the element
     * @return a <code>NonEmptyImmutableList</code> of type <code>T</code>.
     */
    @Nonnull
    @SafeVarargs
    public static <T> NonEmptyImmutableList<T> of(@Nonnull T head, @Nonnull T... el) {
        if (el.length == 0) {
            return cons(head, ImmutableList.empty());
        }
        NonEmptyImmutableList<T> l = cons(el[el.length - 1], ImmutableList.empty());
        for (int i = el.length - 2; i >= 0; i--) {
            l = cons(el[i], l);
        }
        return cons(head, l);
    }

    /**
     * A helper constructor to create a potentially empty {@link ImmutableList}.
     *
     * @param el  Elements of the list
     * @param <A> The type of elements
     * @return a <code>ImmutableList</code> of type <code>A</code>.
     */
    @Nonnull
    @SafeVarargs
    public static <A> ImmutableList<A> from(@Nonnull A... el) {
        return fromBounded(el, 0, el.length);
    }

    @Nonnull
    @SafeVarargs
    @Deprecated
    public static <T> NonEmptyImmutableList<T> list(@Nonnull T head, @Nonnull T... el) {
        return ImmutableList.of(head, el);
    }

    /**
     * A helper constructor to create a potentially empty {@link ImmutableList} from part of an array.
     *
     * @param el    Elements of the list
     * @param start The index to start conversion
     * @param end   The index before which the conversion stops. <code>end</code> will not be used as
     *              an index to access <code>el</code>
     * @param <A>   The type of elements
     * @return a <code>ImmutableList</code> of type <code>A</code>.
     */
    @Nonnull
    public static <A> ImmutableList<A> fromBounded(@Nonnull A[] el, int start, int end) {
        if (end == start) {
            return empty();
        }
        NonEmptyImmutableList<A> l = cons(el[end - 1], ImmutableList.empty());
        for (int i = end - 2; i >= start; i--) {
            l = cons(el[i], l);
        }
        return l;
    }

    protected abstract int calcHashCode();

    @Override
    public final int hashCode() {
        // Manually expanded thunk
        Integer hashCodeCached = this.hashCode;
        if (hashCodeCached == null) {
            // This is safe because calcHashCode has no side-effects.
            int hc = this.calcHashCode();
            this.hashCode = hc;
            return hc;
        } else {
            return hashCodeCached;
        }
    }

    protected final Integer getCachedHashCode() {
        return this.hashCode;
    }

    protected final void setCachedHashCode(Integer hashCode) {
        this.hashCode = hashCode;
    }

    /**
     * This function is provided by Iterable that can be used to avoid a creation
     * of an Iterator instance.
     *
     * @param action The action to be performed for each element
     */
    @Override
    public void forEach(@Nonnull Consumer<? super A> action) {
        // Manually expanded recursion.
        ImmutableList<A> list = this;
        while (list instanceof NonEmptyImmutableList) {
            A head = ((NonEmptyImmutableList<A>) list).head;
            action.accept(head);
            list = ((NonEmptyImmutableList<A>) list).tail;
        }
    }

    @Override
    public Iterator<A> iterator() {
        return new Iterator<A>() {
            private ImmutableList<A> curr = ImmutableList.this;

            @Override
            public boolean hasNext() {
                return !this.curr.isEmpty();
            }

            @Override
            public A next() {
                if (this.curr instanceof NonEmptyImmutableList) {
                    NonEmptyImmutableList<A> nel = (NonEmptyImmutableList<A>) this.curr;
                    this.curr = nel.tail;
                    return nel.head;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public final Spliterator<A> spliterator() {
        return Spliterators.spliterator(iterator(), this.length, Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }

    @Nonnull
    public final Stream<A> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    // Methods

    /**
     * Prepend an element to the list.
     *
     * @param left The element to prepend.
     * @return A list with <code>left</code> as the first element followed by <code>this</code>.
     */
    @Nonnull
    public final NonEmptyImmutableList<A> cons(@Nonnull A left) {
        return cons(left, this);
    }

    /**
     * Classic "foldl" operation on the {@link ImmutableList}.
     *
     * @param f    The function.
     * @param init The initial value.
     * @param <B>  The type of the result of the folding.
     * @return The result of the folder.
     * @see <a href="http://en.wikipedia.org/wiki/Fold_(higher-order_function)#Folds_as_structural_transformations">http://en.wikipedia.org/wiki/Fold_
     * (higher-order_function)</a>
     */
    @Nonnull
    public abstract <B> B foldLeft(@Nonnull F2<B, ? super A, B> f, @Nonnull B init);

    /**
     * Classic "foldr" operation on the {@link ImmutableList}.
     *
     * @param f    The function.
     * @param init The initial value.
     * @param <B>  The type of the result of the folding.
     * @return The result of the folder.
     * @see <a href="http://en.wikipedia.org/wiki/Fold_(higher-order_function)#Folds_as_structural_transformations">http://en.wikipedia.org/wiki/Fold_
     * (higher-order_function)</a>
     */
    @Nonnull
    public abstract <B> B foldRight(@Nonnull F2<? super A, B, B> f, @Nonnull B init);

    /**
     * Returns the head of the {@link ImmutableList}.
     *
     * @return Maybe.of(head of the list), or Maybe.empty() if the list is empty.
     */
    @Nonnull
    public abstract Maybe<A> maybeHead();

    /**
     * Returns the last of the {@link ImmutableList}.
     *
     * @return Maybe.of(last of the list), or Maybe.empty() if the list is empty.
     */
    @Nonnull
    public abstract Maybe<A> maybeLast();

    /**
     * Returns the tail of the {@link ImmutableList}.
     *
     * @return Maybe.of(tail of the list), or Maybe.empty() if the list is empty.
     */
    @Nonnull
    public abstract Maybe<ImmutableList<A>> maybeTail();

    /**
     * Returns the init of the {@link ImmutableList}. The init of a List is defined as the rest of
     * List removing the last element.
     *
     * @return Maybe.of(init of the list), or Maybe.empty() if the list is empty.
     */
    @Nonnull
    public abstract Maybe<ImmutableList<A>> maybeInit();

    /**
     * Returns a new list of elements when applying <code>f</code> to an element returns true.
     *
     * @param f The "predicate" function.
     * @return A new list of elements that satisfies the predicate.
     */
    @Nonnull
    public abstract ImmutableList<A> filter(@Nonnull F<A, Boolean> f);

    /**
     * Returns a count of elements for which applying <code>f</code> returns true.
     *
     * @param f The "predicate" function.
     * @return A count of elements that satisfies the predicate.
     */
    public abstract int count(@Nonnull F<A, Boolean> f);

    /**
     * Applies the <code>f</code> function to each of the elements of the list and collect the
     * result. It will be a new list with the same length of the original one.
     *
     * @param f   The function to apply.
     * @param <B> The type of the new {@link ImmutableList}.
     * @return The new {@link ImmutableList} containing the result.
     */
    @Nonnull
    public abstract <B> ImmutableList<B> map(@Nonnull F<A, B> f);

    /**
     * Applies the <code>f</code> function to each of the elements of the list and collect the
     * result. This method also provides an extra index parameter to <code>f</code> function as the
     * first parameter.
     *
     * @param f   The function to apply.
     * @param <B> The type of the new {@link ImmutableList}.
     * @return The new {@link ImmutableList} containing the result.
     */
    @Nonnull
    public abstract <B> ImmutableList<B> mapWithIndex(@Nonnull F2<Integer, A, B> f);

    /**
     * The the first <code>n</code> elements of the list and create a new List of them. If the
     * original list contains less than <code>n</code> elements, returns a copy of the original
     * List.
     *
     * @param n The number of elements to take.
     * @return A new list containing at most <code>n</code> elements that are the first elements of
     * the original list.
     */
    @Nonnull
    public abstract ImmutableList<A> take(int n);

    /**
     * Removes the first <code>n</code> elements of the list and return the rest of the List by
     * reference. If the original list contains less than <code>n</code> elements, returns an empty
     * list as if it is returned by {@link #empty}.
     *
     * @param n The number of elements to skip.
     * @return A shared list containing at most <code>n</code> elements removed from the original
     * list.
     */
    @Nonnull
    public abstract ImmutableList<A> drop(int n);

    /**
     * Specialize this type to be a {@link NonEmptyImmutableList} if possible.
     *
     * @return Returns is <code>Maybe.of(this)</code> if this is indeed non-empty. Otherwise
     * returns <code>Maybe.empty()</code>.
     */
    @Nonnull
    public abstract Maybe<NonEmptyImmutableList<A>> toNonEmptyList();

    /**
     * Deconstruct the list in to its head and tail, and feed them into another function
     * <code>f</code>.
     *
     * @param f   The function to receive the head and tail if they exist.
     * @param <B> The return type of <code>f</code>
     * @return If the list is an non-empty list, returns <code>Maybe.of(f(head, tail))</code>;
     * otherwise returns <code>Maybe.empty()</code>.
     */
    @Nonnull
    public abstract <B> Maybe<B> decons(@Nonnull F2<A, ImmutableList<A>, B> f);

    /**
     * Takes another list and feeds the elements of both lists to a function at the same pace, then
     * collects the result and forms another list. Stops once either of the two lists came to an
     * end. <p> Another way to visualize this operation is to imagine this operation as if it's
     * zipping a zipper. taking two lists of things, merge them one by one, and collect the
     * results.
     *
     * @param f    The function to apply
     * @param list the other list to zip with <code>this</code>.
     * @param <B>  The type of the element of the other list.
     * @param <C>  The type of the result of the merging function.
     * @return The return type of the merging function.
     */
    @Nonnull
    public abstract <B, C> ImmutableList<C> zipWith(@Nonnull F2<A, B, C> f, @Nonnull ImmutableList<B> list);

    /**
     * Converts this list into an array. <p> Due to type erasure, the type of the resulting array
     * has to be determined at runtime. Fortunately, you can create a zero length array and this
     * method can create an large enough array to contain all the elements. If the given array is
     * large enough, this method will put elements in it.
     *
     * @param target The target array.
     * @return The array that contains the elements. It may or may not be the same reference of
     * <code>target</code>.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public final A[] toArray(@Nonnull A[] target) {
        int length = this.length;
        if (target.length < length) {
            // noinspection unchecked
            target = (A[]) Array.newInstance(target.getClass().getComponentType(), length);
        }
        ImmutableList<A> l = this;
        for (int i = 0; i < length; i++) {
            target[i] = ((NonEmptyImmutableList<A>) l).head;
            l = ((NonEmptyImmutableList<A>) l).tail;
        }
        return target;
    }

    /**
     * Converts this list into a java.util.ArrayList.
     *
     * @return The list that contains the elements.
     */
    @Nonnull
    public final ArrayList<A> toArrayList() {
        ArrayList<A> list = new ArrayList<>(this.length);
        ImmutableList<A> l = this;
        for (int i = 0; i < length; i++) {
            list.add(((NonEmptyImmutableList<A>) l).head);
            l = ((NonEmptyImmutableList<A>) l).tail;
        }
        return list;
    }

    /**
     * Converts this list into a java.util.LinkedList.
     *
     * @return The list that contains the elements.
     */
    @Nonnull
    public final LinkedList<A> toLinkedList() {
        LinkedList<A> list = new LinkedList<>();
        ImmutableList<A> l = this;
        for (int i = 0; i < length; i++) {
            list.add(((NonEmptyImmutableList<A>) l).head);
            l = ((NonEmptyImmutableList<A>) l).tail;
        }
        return list;
    }

    /**
     * Converts this list into an array. <p> Due to type erasure, the type of the resulting array
     * has to be determined at runtime. Fortunately, you can create a zero length array and this
     * method can create an large enough array to contain all the elements. If the given array is
     * large enough, this method will put elements in it.
     *
     * @param constructor The constructor of the target array
     * @return The array that contains the elements. It may or may not be the same reference of
     * <code>target</code>.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public final A[] toArray(@Nonnull F<Integer, A[]> constructor) {
        int length = this.length;
        A[] target = constructor.apply(length);
        return this.toArray(target);
    }

    /**
     * Runs an effect function across all the elements.
     *
     * @param f The Effect function.
     * @deprecated Use {@link #forEach(Consumer)} instead.
     */
    @Deprecated
    public final void foreach(@Nonnull Effect<A> f) {
        this.forEach(f::e);
    }

    public abstract boolean isEmpty();

    /**
     * Creates a list with the content of the current list followed by another list. If the current
     * list is empty, simply return the second one.
     *
     * @param defaultClause The list to concatenate with. It will be reused as part of the returned
     *                      list.
     * @param <B>           The type of the resulting list.
     * @return The concatenation of the two lists.
     */
    @Nonnull
    public abstract <B extends A> ImmutableList<A> append(@Nonnull ImmutableList<B> defaultClause);

    /**
     * Tests all the elements in the {@link ImmutableList} with predicate <code>f</code> until it
     * finds the element or reaches the end, then returns whether an element has been found or not.
     *
     * @param f The predicate.
     * @return Whether an elements satisfies the predicate <code>f</code>.
     */
    public abstract boolean exists(@Nonnull F<A, Boolean> f);

    /**
     *
     * Apply <code>f</code> to each element of the list, returning false if f is false for an element,
     * or true if true for all elements.
     *
     * @param f The function to test against
     * @return true IFF f is true for all entries in this list
     */
    public abstract boolean every(@Nonnull F<A, Boolean> f);

    /**
     * Tests using object identity whether this list contains the element <code>a</code>.
     * <p>
     * WARNING: object identity is tests using the <code>==</code> operator.
     * To test if object exists by equality, use {@link #exists(F)}
     *
     * @param a An element.
     * @return Whether this list contains the element <code>a</code>.
     */
    public abstract boolean contains(@Nonnull A a);

    /**
     * Separates the list into a pair of lists such that 1. the concatenation of the lists is equal
     * to <code>this</code>; 2. The first list is the longest list that every element of the list
     * fails the predicate <code>f</code>.
     *
     * @param f The predicate.
     * @return The pair.
     */
    @Nonnull
    public abstract Pair<ImmutableList<A>, ImmutableList<A>> span(@Nonnull F<A, Boolean> f);

    /**
     * A synonym of {@link #flatMap}.
     *
     * @param f   The function.
     * @param <B> The type of result function.
     * @return The result of bind.
     */
    @Nonnull
    public final <B> ImmutableList<B> bind(@Nonnull F<A, ImmutableList<B>> f) {
        return this.flatMap(f);
    }

    /**
     * Apply <code>f</code> to each element of this list to get a list of lists (of not necessarily
     * the same type), then concatenate all these lists to get a single list. <p> This operation can
     * be thought of as a generalization of {@link #map} and {@link #filter}, which, instead of
     * keeping the number of elements in the list, changes the number and type of the elements in an
     * customizable way but keeps the original order. <p> This operation can also be thought of as
     * an assembly line that takes one stream of input and returns another stream of output, but not
     * necessarily of the same size, type or number. <p> This operation is often called "bind" or
     * "&gt;&gt;=" of a monad in pure functional programming context.
     *
     * @param f   The function to expand the list element.
     * @param <B> The type of the result list.
     * @return The result list.
     */
    @Nonnull
    public abstract <B> ImmutableList<B> flatMap(@Nonnull F<A, ImmutableList<B>> f);

    public final boolean isNotEmpty() {
        return !this.isEmpty();
    }

    /**
     * Tests the elements of the list with a predicate <code>f</code> and returns the first one that
     * satisfies the predicate without testing the rest of the list.
     *
     * @param f The predicate.
     * @return <code>Maybe.of(the found element)</code> if an element is found or
     * <code>Maybe.empty()</code> if none is found.
     */
    @Nonnull
    public final Maybe<A> find(@Nonnull F<A, Boolean> f) {
        ImmutableList<A> self = this;
        while (self instanceof NonEmptyImmutableList) {
            NonEmptyImmutableList<A> selfNel = (NonEmptyImmutableList<A>) self;
            boolean result = f.apply(selfNel.head);
            if (result) {
                return Maybe.of(selfNel.head);
            }
            self = selfNel.tail();
        }
        return Maybe.empty();
    }

    /**
     * Tests the elements of the list with a predicate <code>f</code> and returns the index of the first one that
     * satisfies the predicate without testing the rest of the list.
     *
     * @param f The predicate.
     * @return <code>Maybe.of(the found element index)</code> if an element is found or
     * <code>Maybe.empty()</code> if none is found.
     */
    @Nonnull
	public final Maybe<Integer> findIndex(@Nonnull F<A, Boolean> f) {
        ImmutableList<A> self = this;
        int i = 0;
        while (self instanceof NonEmptyImmutableList) {
            NonEmptyImmutableList<A> selfNel = (NonEmptyImmutableList<A>) self;
            if (f.apply(selfNel.head)) {
                return Maybe.of(i);
            }
            self = selfNel.tail();
            ++i;
        }
        return Maybe.empty();
	}

    /**
     * Run <code>f</code> on each element of the list and return the result immediately if it is a
     * <code>Maybe.of</code>. Other wise return <code>Maybe.empty()</code>
     *
     * @param f   The predicate.
     * @param <B> The type of the result of the mapping function.
     * @return <code>Maybe.of(the found element)</code> if an element is found or
     * <code>Maybe.empty()</code> if none is found.
     */
    @Nonnull
    public final <B> Maybe<B> findMap(@Nonnull F<A, Maybe<B>> f) {
        ImmutableList<A> self = this;
        while (self instanceof NonEmptyImmutableList) {
            NonEmptyImmutableList<A> selfNel = (NonEmptyImmutableList<A>) self;
            Maybe<B> result = f.apply(selfNel.head);
            if (result.isJust()) {
                return result;
            }
            self = selfNel.tail();
        }
        return Maybe.empty();
    }

    /**
     * Creats a new list with all the elements but those satisfying the predicate.
     *
     * @param f The predicate.
     * @return A new list of filtered elements.
     */
    @Nonnull
    public abstract ImmutableList<A> removeAll(@Nonnull F<A, Boolean> f);

    /**
     * Reverses the list in linear time.
     *
     * @return Reversed list.
     */
    @Nonnull
    public abstract ImmutableList<A> reverse();

    /**
     * Patches the current list. Patching a list first takes the first <code>index</code> elements
     * then concatenates it with <code>replacements</code> and then concatenates it with the
     * original list dropping <code>index + patchLength</code> elements. <p> A visualization of this
     * operation is to replace the <code>patchLength</code> elements in the list starting from
     * <code>index</code> with a list of new elements given by <code>replacements</code>.
     *
     * @param index        The index to start patching.
     * @param patchLength  The length to patch.
     * @param replacements The replacements of the patch.
     * @param <B>          The type of the replacements. It must be A or a subtype of A.
     * @return The patched list.
     */
    @Nonnull
    public <B extends A> ImmutableList<A> patch(int index, int patchLength, @Nonnull ImmutableList<B> replacements) {
        return this.take(index).append(replacements).append(this.drop(index + patchLength));
    }

    /**
     * <code>mapAccumL</code> performs {@link #map} and {@link #foldLeft} method at the same time.
     * It is similar to {@link #foldLeft}, but instead of returning the new accumulation value, it
     * also allows the user to return an extra value which will be collected and returned.
     *
     * @param f   The accumulation function.
     * @param acc The initial value of the fold part.
     * @param <B> The type of the initial value.
     * @param <C> The type of the result of map.
     * @return A pair of the accumulation value and a mapped list.
     */
    @Nonnull
    public abstract <B, C> Pair<B, ImmutableList<C>> mapAccumL(@Nonnull F2<B, A, Pair<B, C>> f, @Nonnull B acc);

    /**
     * Get the <code>index</code>th element of the list. It is comparable to the <code>[]</code>
     * operator for array but instead of returning the element, it returns an <code>Maybe</code> to
     * indicate whether the element can be found or not.
     *
     * @param index The index.
     * @return <code>Maybe.of(found element)</code>if the element can be retrieved; or
     * <code>Maybe.empty()</code> if index out of range().
     */
    @Nonnull
    public final Maybe<A> index(int index) {
        ImmutableList<A> l = this;
        if (index < 0) {
            return Maybe.empty();
        }
        while (index > 0) {
            if (l.isEmpty()) {
                return Maybe.empty();
            }
            index--;
            l = ((NonEmptyImmutableList<A>) l).tail;
        }
        return l.maybeHead();
    }

    @Nonnull
    public abstract ImmutableSet<A> uniqByEquality();

    @Nonnull
    public abstract ImmutableSet<A> uniqByIdentity();

    @Nonnull
    public abstract <B> ImmutableSet<A> uniqByEqualityOn(@Nonnull F<A, B> f);

    @Override
    public abstract boolean equals(Object o);

    @Nonnull
    public Pair<ImmutableList<A>, ImmutableList<A>> partition(@Nonnull Predicate<A> predicate) {
        @SuppressWarnings("unchecked")
        A[] result = (A[]) new Object[this.length];
        int[] l = new int[]{0};
        int[] r = new int[]{this.length};
        this.forEach(a -> {
            if (predicate.test(a)) {
                result[l[0]++] = a;
            } else {
                result[--r[0]] = a;
            }
        });
        ImmutableList<A> right = empty();
        for (int i = l[0]; i < this.length; i++) {
            right = right.cons(result[i]);
        }
        return Pair.of(fromBounded(result, 0, l[0]), right);
    }

    @Nonnull
    public static <T> Collector<T, ?, ImmutableList<T>> collector() {
        return new Collector<T, ArrayList<T>, ImmutableList<T>>() {
            @Override
            public Supplier<ArrayList<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<ArrayList<T>, T> accumulator() {
                return ArrayList::add;
            }

            @Override
            public BinaryOperator<ArrayList<T>> combiner() {
                return (left, right) -> {
                    left.addAll(right);
                    return left;
                };
            }

            @Override
            public Function<ArrayList<T>, ImmutableList<T>> finisher() {
                return ImmutableList::from;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }
}

