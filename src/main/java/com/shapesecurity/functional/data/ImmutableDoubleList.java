package com.shapesecurity.functional.data;

import com.shapesecurity.functional.F;
import com.shapesecurity.functional.F2;
import com.shapesecurity.functional.Pair;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ImmutableDoubleList<T> implements INonEmptyImmutableListExt<T> {
    @Nonnull
    private final LinkedList<T> list;
    // It would be a nice optimization to use our own mutable linked list impl with restartable iterators.
    private final List<T> subList;
    private final int start;
    private final int end;
    private final int hashCode;
    public final int length;

    protected ImmutableDoubleList(@Nonnull LinkedList<T> list, int start, int end) {
        this.length = end - start;
        this.list = list;
        if (this.list.size() < end || start >= end || start < 0) {
            throw new IllegalArgumentException("invalid range");
        }
        this.subList = start == 0 && end == list.size() ? list : list.subList(start, end);
        this.start = start;
        this.end = end;
        this.hashCode = this.calcHashCode();
    }

    private List<T> subList(int start, int end) {
        return list.subList(this.start + start, this.end - (length - end));
    }

    @Nonnull
    public IImmutableList<T> slice(int start, int end) {
        return ImmutableList.fromLinkedList(list, this.start + start, this.end - (length - end));
    }

    @Nonnull
    @Override
    public ImmutableDoubleList<T> cons(T left) {
        return this.prepend(left);
    }

    @Nonnull
    @Override
    public ImmutableDoubleList<T> append(T right) {
        LinkedList<T> list = new LinkedList<>(subList);
        list.addLast(right);
        return (ImmutableDoubleList<T>) ImmutableList.fromLinkedList(list);
    }

    @Nonnull
    @Override
    public ImmutableDoubleList<T> prepend(T left) {
        LinkedList<T> list = new LinkedList<>(subList);
        list.addFirst(left);
        return (ImmutableDoubleList<T>) ImmutableList.fromLinkedList(list);
    }

    private int calcHashCode() {
        return subList.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Nonnull
    @Override
    public <B> B foldLeft(@Nonnull F2<B, ? super T, B> f, @Nonnull B init) {
        for (T entry : subList) {
            init = f.apply(init, entry);
        }
        return init;
    }

    @Nonnull
    @Override
    public <B> B foldRight(@Nonnull F2<? super T, B, B> f, @Nonnull B init) {
        Iterator<T> it = this.reverseIterator();
        int i = 0;
        for (; i < length && it.hasNext(); i++) {
            init = f.apply(it.next(), init);
        }
        if (i < length) {
            throw new RuntimeException("Unexpected short underlying linked list");
        }
        return init;
    }

    @Nonnull
    @Override
    public T head() {
        return subList.get(0);
    }

    @Nonnull
    @Override
    public T last() {
        return subList.get(subList.size() - 1);
    }

    @Nonnull
    @Override
    public IImmutableList<T> tail() {
        if (this.length == 1) {
            return ImmutableList.empty();
        }
        return this.slice(1, this.length);
    }

    @Nonnull
    @Override
    public IImmutableList<T> init() {
        if (this.length == 1) {
            return ImmutableList.empty();
        }
        return this.slice(0, this.length - 1);
    }

    @Nonnull
    @Override
    public IImmutableList<T> filter(@Nonnull F<T, Boolean> f) {
        LinkedList<T> newList = new LinkedList<>();
        for (T entry : subList) {
            if (f.apply(entry)) {
                newList.addLast(entry);
            }
        }
        return ImmutableList.fromLinkedList(newList, 0, newList.size());
    }

    @Override
    public int count(@Nonnull F<T, Boolean> f) {
        int count = 0;
        for (T entry : subList) {
            if (f.apply(entry)) {
                count++;
            }
        }
        return count;
    }

    @Nonnull
    @Override
    public <B> INonEmptyImmutableList<B> map(@Nonnull F<T, B> f) {
        LinkedList<B> newList = new LinkedList<>();
        for (T entry : subList) {
            newList.addLast(f.apply(entry));
        }
        return (INonEmptyImmutableList<B>)ImmutableList.fromLinkedList(newList, 0, newList.size());
    }

    @Nonnull
    @Override
    public <B> INonEmptyImmutableList<B> mapWithIndex(@Nonnull F2<Integer, T, B> f) {
        LinkedList<B> newList = new LinkedList<>();
        int i = 0;
        for (T entry : subList) {
            newList.addLast(f.apply(i, entry));
            i++;
        }
        return (INonEmptyImmutableList<B>)ImmutableList.fromLinkedList(newList, 0, newList.size());
    }

    @Nonnull
    @Override
    public IImmutableList<T> take(int n) {
        return this.slice(0, n >= length ? length : n);
    }

    @Nonnull
    @Override
    public IImmutableList<T> drop(int n) {
        return n >= length ? ImmutableList.empty() : this.slice(n, length);
    }

    @Nonnull
    @Override
    public Maybe<NonEmptyImmutableList<T>> toNonEmptyList() {
        return Maybe.of((NonEmptyImmutableList<T>) ImmutableList.from(subList));
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    @Nonnull
    public <B> Maybe<B> decons(@Nonnull F2<T, IImmutableList<T>, B> f) {
        return Maybe.of(f.apply(subList.get(0), this.tail())); // would be much faster if it took an Iterable
    }

    @Nonnull
    @Override
    public <B, C> INonEmptyImmutableList<C> zipWith(@Nonnull F2<T, B, C> f, @Nonnull INonEmptyImmutableList<B> list) {
        Iterator<B> otherIterator = list.iterator();
        Iterator<T> ourIterator = subList.iterator();
        LinkedList<C> output = new LinkedList<>();
        while (ourIterator.hasNext() && otherIterator.hasNext()) {
            output.addLast(f.apply(ourIterator.next(), otherIterator.next()));
        }
        return (INonEmptyImmutableList<C>) ImmutableList.fromLinkedList(output, 0, output.size());
    }

    @SuppressWarnings("unchecked")
    private static <B> List<B> listFromImmutableList(IImmutableList<B> list) {
        if (list instanceof ImmutableDoubleList) {
            return ((ImmutableDoubleList<B>) list).subList;
        } else {
            return Arrays.asList((B[]) ((ImmutableList<Object>) list).toArray(new Object[0]));
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <B extends T> INonEmptyImmutableList<T> append(@Nonnull IImmutableList<B> defaultClause) {
         if (defaultClause.isEmpty()) {
            return this;
        }
        if (defaultClause instanceof ImmutableDoubleList) {
            LinkedList<T> newList = new LinkedList<>(this.subList);
            newList.addAll(((ImmutableDoubleList<B>) defaultClause).subList);
            return (INonEmptyImmutableList<T>) ImmutableList.fromLinkedList(newList, 0, newList.size());
        } else if (defaultClause instanceof NonEmptyImmutableList && ((NonEmptyImmutableList<T>)defaultClause).length <= this.length) {
            LinkedList<T> newList = new LinkedList<>(this.subList);
            newList.addAll(listFromImmutableList(defaultClause));
            return (INonEmptyImmutableList<T>) ImmutableList.fromLinkedList(newList, 0, newList.size());
        } else {
            // contingency
            return this.toNonEmptyList().fromJust().append(defaultClause);
        }
    }

    @Override
    public boolean exists(@Nonnull F<T, Boolean> f) {
        for (T entry : subList) {
            if (f.apply(entry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(@Nonnull T t) {
        for (T entry : subList) {
            if (entry == t) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public Pair<IImmutableList<T>, IImmutableList<T>> span(@Nonnull F<T, Boolean> f) {
        int splitIndex = 0;
        for (T entry : subList) {
            if (!f.apply(entry)) {
                break;
            }
            splitIndex++;
        }
        return new Pair<>(this.slice(0, splitIndex), this.slice(splitIndex, length));
    }

    @Nonnull
    @Override
    public <B> IImmutableList<B> chain(@Nonnull F<T, IImmutableList<B>> f) {
        LinkedList<B> newList = new LinkedList<>();
        for (T entry : subList) {
            newList.addAll(listFromImmutableList(f.apply(entry)));
        }
        return ImmutableList.fromLinkedList(newList, 0, newList.size());
    }

    @Nonnull
    @Override
    public IImmutableList<T> removeAll(@Nonnull F<T, Boolean> f) {
        LinkedList<T> newList = new LinkedList<>();
        for (T entry : subList) {
            if (!f.apply(entry)) {
                newList.addLast(entry);
            }
        }
        return ImmutableList.fromLinkedList(newList, 0, newList.size());
    }

    @Nonnull
    @Override
    public INonEmptyImmutableList<T> reverse() {
        return (INonEmptyImmutableList<T>) ImmutableList.fromLinkedList(StreamSupport.stream(this.reverseIterable().spliterator(), false).collect(Collectors.toCollection(LinkedList::new)), 0, this.length);
    }

    @Override
    @Nonnull
    public <B, C> Pair<B, INonEmptyImmutableList<C>> mapAccumL(@Nonnull F2<B, T, Pair<B, C>> f, @Nonnull B acc) {
        LinkedList<C> newList = new LinkedList<>();
        for (T entry : subList) {
            Pair<B, C> result = f.apply(acc, entry);
            acc = result.left;
            newList.addLast(result.right);
        }
        return new Pair<>(acc, (INonEmptyImmutableList<C>) ImmutableList.fromLinkedList(newList, 0, newList.size()));
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
        ImmutableSet<B> set = ImmutableSet.emptyUsingEquality();
        ImmutableSet<T> out = ImmutableSet.emptyUsingIdentity();
        for (T entry : subList) {
            B result = f.apply(entry);
            if (!set.contains(result)) {
                out = out.put(entry);
                set = set.put(result);
            }
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ImmutableDoubleList) {
            if (((ImmutableDoubleList) o).length != length) {
                return false;
            }
            return ((ImmutableDoubleList) o).subList.equals(subList);
        } else if (o instanceof ImmutableList) {
            if (((ImmutableList) o).length != length) {
                return false;
            }
            Iterator otherIterator = ((ImmutableList) o).iterator();
            Iterator ourIterator = subList.iterator();
            while (ourIterator.hasNext()) {
                if (!otherIterator.next().equals(ourIterator)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return subList.iterator();
    }

    public Iterator<T> reverseIterator() {
        if (subList == list) {
            return list.descendingIterator();
        } else {
            Iterator<T> it = list.descendingIterator();
            for (int i = this.list.size(); i > end; i--) {
                it.next();
            }
            return it;
        }
    }

    public Iterable<T> reverseIterable() {
        return this::reverseIterator;
    }

    @Override
    @Nonnull
    public T[] toArray(@Nonnull T[] target) {
        return subList.toArray(target);
    }

    @Override
    public void forEach(@Nonnull Consumer<? super T> action) {
        for (T entry : subList) {
            action.accept(entry);
        }
    }

    @Nonnull
    @Override
    public <B extends T> IImmutableList<T> patch(int index, int patchLength, @Nonnull IImmutableList<B> replacements) {
        LinkedList<T> newList = new LinkedList<>(this.subList(0, index));
        newList.addAll(listFromImmutableList(replacements));
        newList.addAll(this.subList(index + patchLength, length));
        return ImmutableList.fromLinkedList(newList);
    }

    @Nonnull
    @Override
    public Maybe<T> index(int index) {
        return index < 0 || index > length ? Maybe.empty() : Maybe.of(subList.get(index));
    }

    @Override
    @Nonnull
    public Maybe<Integer> findIndexEquality(@Nonnull T t) {
        int i = 0;
        for (T entry : subList) {
            if (entry.equals(t)) {
                return Maybe.of(i);
            }
            i++;
        }
        return Maybe.empty();
    }

    @Override
    @Nonnull
    public Maybe<Integer> findIndexIdentity(@Nonnull T t) {
        int i = 0;
        for (T entry : subList) {
            if (entry == t) {
                return Maybe.of(i);
            }
            i++;
        }
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<T> find(@Nonnull F<T, Boolean> f) {
        for (T entry : subList) {
            if (f.apply(entry)) {
                return Maybe.of(entry);
            }
        }
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public Maybe<Integer> findIndex(@Nonnull F<T, Boolean> f) {
        int i = 0;
        for (T entry : subList) {
            if (f.apply(entry)) {
                return Maybe.of(i);
            }
            i++;
        }
        return Maybe.empty();
    }

    @Nonnull
    @Override
    public <B> Maybe<B> findMap(@Nonnull F<T, Maybe<B>> f) {
        for (T entry : subList) {
            Maybe<B> result = f.apply(entry);
            if (result.isJust()) {
                return result;
            }
        }
        return Maybe.empty();
    }

}
