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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.shapesecurity.functional.Effect;
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.TestBase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ImmutableListTest extends TestBase {
    protected void testWithSpecialLists(@NotNull Effect<ImmutableList<Integer>> f) {
        f.apply(Nil.empty());
        f.apply(ImmutableList.<Integer>empty());
        f.apply(ImmutableList.of(0));
        f.apply(ImmutableList.of(0, 1, 2));
        f.apply(ImmutableList.of(3, 2, 1));
        f.apply(LONG_LIST);
    }

    @Test
    public void testEmpty() {
        ImmutableList<Integer> list = ImmutableList.empty();
        assertTrue(list.maybeHead().isNothing());
        assertTrue(list.maybeTail().isNothing());
        assertEquals(list, list);
        assertEquals(list, ImmutableList.<Integer>empty());
        assertNotEquals(list, ImmutableList.cons(0, list));
    }

    @Test
    public void testCons() {
        testWithSpecialLists(this::testCons);
    }

    public void testCons(@NotNull ImmutableList<Integer> list) {
        int a = rand();
        NonEmptyImmutableList<Integer> listP = ImmutableList.cons(a, list);
        assertEquals(list.length + 1, listP.length);
        assertEquals(a, listP.head.intValue());
        assertEquals(list, listP.tail());
        assertEquals(listP, list.cons(a));
    }

    @Test
    public void testToArray() {
        testWithSpecialLists(this::testToArray);
    }

    private void testToArray(ImmutableList<Integer> list) {
        final Integer[] a = new Integer[list.length];
        list.mapWithIndex((i, x) -> a[i] = x);
        Integer[] a2 = new Integer[0];
        a2 = list.toArray(a2);
        assertEquals(a.length, a2.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], a2[i]);
        }
    }

    @Test
    public void testIndex() {
        ImmutableList<Integer> l = ImmutableList.of(0, 1, 2, 3, 4);
        assertTrue(l.index(0).fromJust() == 0);
        assertFalse(l.index(2).fromJust() == 0);
        assertEquals(Maybe.<Integer>empty(), l.index(5)); //index out of range returns empty
        assertEquals(Maybe.<Integer>empty(), l.index(-1));
        assertEquals(Maybe.<Integer>empty(), ImmutableList.<Integer>empty().index(1));
    }

    @Test
    public void testFrom() {
        testWithSpecialLists(this::testFrom);
        testWithSpecialLists(this::testFromArray);
    }

    private void testFromArray(ImmutableList<Integer> list) {
        final Integer[] array = new Integer[list.length];
        list.mapWithIndex((i, x) -> array[i] = x);
        ImmutableList<Integer> list1 = ImmutableList.from(array);
        assertEquals(list, list1);
    }

    private void testFrom(ImmutableList<Integer> list) {
        final ArrayList<Integer> arrList = new ArrayList<>();
        list.foreach(arrList::add);
        ImmutableList<Integer> listP = ImmutableList.from(arrList);
        assertEquals(list, listP);
    }

    @Test
    public void testFindMap() {
        ImmutableList<Integer> list = ImmutableList.of(0, 1, 2, 3, 4);
        assertTrue(list.findMap(x -> x == 2 ? Maybe.of(x - 1) : Maybe.<Integer>empty()).fromJust() == 1);
        assertEquals(Maybe.<Integer>empty(), list.findMap(x -> x == 5 ? Maybe.of(x - 1) : Maybe.<Integer>empty()));
        assertEquals(Maybe.<Integer>empty(), ImmutableList.<Integer>empty().findMap(x -> x == 5 ? Maybe.of(x - 1) :
                Maybe.<Integer>empty()));
    }

    @Test
    public void testMaybeInit() {
        testWithSpecialLists(this::testInit);
    }

    private void testInit(ImmutableList<Integer> list) {
        //if list is empty maybeInit returns empty else returns of
        Maybe<ImmutableList<Integer>> taken = list.isEmpty() ? Maybe.empty() : Maybe.of(list.take(list.length - 1));
        assertEquals(list.maybeInit(), taken);
    }

    @Test
    public void testMaybeLast() {
        testWithSpecialLists(this::testMaybeLast);
    }

    private void testMaybeLast(ImmutableList<Integer> list) {
        Maybe<Integer> last = list.index(list.length - 1);
        assertEquals(last, list.maybeLast());
    }

    @Test
    public void testMap() {
        testWithSpecialLists(this::testMap);
    }

    private void testMap(ImmutableList<Integer> list) {
        Integer[] addArray = new Integer[list.length];
        for (int i = 0; i < addArray.length; i++) {
            addArray[i] = list.index(i).fromJust() + 1;
        }
        assertEquals(list.map(x -> x + 1), ImmutableList.from(addArray));
    }

    @Test
    public void testFlatMap() {
        testWithSpecialLists(this::testFlatMap);
    }

    private void testFlatMap(ImmutableList<Integer> list) {
        Integer[] dups = new Integer[list.length * 2];
        for (int i = 0; i < dups.length / 2; i++) {
            dups[i * 2] = list.index(i).fromJust();
            dups[i * 2 + 1] = list.index(i).fromJust();
        }
        assertEquals(ImmutableList.from(dups), list.flatMap(x -> ImmutableList.of(x, x)));
    }

    // non-static

    @Test
    public void testLengthNonNegative() {
        testWithSpecialLists(this::testLengthNonNegative);
    }

    private void testLengthNonNegative(ImmutableList<Integer> list) {
        assertTrue(list.length >= 0);
    }

    @Test
    public void testEmptyNonEmptyMutualExclusivity() {
        testWithSpecialLists(ImmutableListTest.this::testEmptyNonEmptyMutualExclusivity);
    }

    private void testEmptyNonEmptyMutualExclusivity(ImmutableList<Integer> list) {
        if (list.length == 0) {
            assertTrue(list.isEmpty());
            assertFalse(list.isNotEmpty());
        } else {
            assertTrue(list.isNotEmpty());
            assertFalse(list.isEmpty());
        }
    }

    @Test
    public void testMaybeHeadMaybeTail() {
        testWithSpecialLists(ImmutableListTest.this::testMaybeHeadMaybeTail);
    }

    private void testMaybeHeadMaybeTail(ImmutableList<Integer> list) {
        if (list.length == 0) {
            assertTrue(list.maybeHead().isNothing());
            assertTrue(list.maybeTail().isNothing());
        } else {
            assertTrue(list.maybeHead().isJust());
            assertTrue(list.maybeTail().isJust());
            if (list.length == 1) {
                assertEquals(ImmutableList.<Integer>empty(), list.maybeTail().fromJust());
            } else {
                assertNotEquals(ImmutableList.<Integer>empty(), list.maybeTail().fromJust());
            }
        }
    }

    @Test
    public void testEquals() {
        testWithSpecialLists(this::testEquals);
    }

    private void testEquals(ImmutableList<Integer> list) {
        assertEquals(list, list);
    }

    @Test
    public void testSpan() {
        testSpan(ImmutableList.<Integer>empty(), 0, 0);
        testSpan(ImmutableList.of(1, 2, 3), 3, 0);
        testSpan(ImmutableList.of(10, 20, 30), 0, 3);
        testSpan(ImmutableList.of(5, 10, 15), 1, 2);
    }

    private void testSpan(ImmutableList<Integer> list, int lengthA, int lengthB) {
        Pair<ImmutableList<Integer>, ImmutableList<Integer>> s = list.span(i -> i < 10);
        assertEquals(s.left.length, lengthA);
        assertEquals(s.right.length, lengthB);
    }

    @Test
    public void testZipWith() {
        testWithSpecialLists(this::testZipWith);
    }

    private void testZipWith(ImmutableList<Integer> list) {
        ImmutableList<Integer> integers = list.zipWith((a, b) -> 0, ImmutableList.<Integer>empty());
        assertEquals(0, integers.length);
        integers = list.zipWith((a, b) -> a + b, list);
        list.foldLeft((l, integer) -> {
            assertEquals(integer * 2, (int) l.maybeHead().fromJust());
            return l.maybeTail().fromJust();
        }, integers);
        if (list instanceof NonEmptyImmutableList) {
            NonEmptyImmutableList<Integer> nel = (NonEmptyImmutableList<Integer>) list;
            ImmutableList<Integer> a = nel.zipWith((x, y) -> x + y, nel.tail());
            assertEquals(a.length, list.length - 1);
        }
    }

    @Test
    public void testFilter() {
        ImmutableList<Integer> integers = range(100).filter(i -> i > 15);
        assertEquals(84, integers.length);
    }

    @Test
    public void testRemoveAll() {
        ImmutableList<Integer> integers = range(100).removeAll(i -> i <= 15);
        assertEquals(84, integers.length);
    }

    @Test
    public void testTakeDrop() {
        assertEquals(85, range(100).drop(15).length);
        assertEquals(15, range(100).take(15).length);
    }

    @Test
    public void testExists() {
        ImmutableList<Integer> list = ImmutableList.empty();
        assertFalse(list.contains(0));
    }

    @Test
    public void testContains() {
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        Object d = new Object();
        ImmutableList<Object> l = ImmutableList.from(a, b, c);
        assertTrue(l.contains(a));
        assertTrue(l.contains(b));
        assertTrue(l.contains(c));
        assertFalse(l.contains(d));
    }

    @Test
    public void testToList() {
        ImmutableList<String> l = ImmutableList.from("a", "b", "c");
        List<String> jl = l.toList();

        assertEquals("a", jl.get(0));
        assertEquals("b", jl.get(1));
        assertEquals("c", jl.get(2));
        assertEquals(3, jl.size());
    }
}
