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

import javax.annotation.Nonnull;

public class NonEmptyImmutableListTest extends TestBase {
    // Helpers

    private void testWithSpecialLists(@Nonnull Effect<NonEmptyImmutableList<Integer>> f) {
        f.apply(ImmutableList.of(0));
        f.apply(ImmutableList.of(0, 1, 2));
        f.apply(ImmutableList.of(3, 2, 1));
        f.apply(LONG_LIST);
    }

    private void testLengthOneGreaterThanTail(NonEmptyImmutableList<Integer> list) {
        assertEquals(list.length, 1 + list.tail().length);
    }

    // Tests

    // static

    @Test
    public void testList() {
        int a = rand();
        NonEmptyImmutableList<Integer> list = ImmutableList.of(a);
        assertEquals(list.length, 1);
        assertEquals(list.head.intValue(), a);
        assertEquals(list.tail(), ImmutableList.<Integer>empty());
    }

    // non-static

    @Test
    public void testHeadTail() {
        testWithSpecialLists(this::testHeadTail);
    }

    private void testHeadTail(NonEmptyImmutableList<Integer> list) {
        int a = rand();
        assertEquals(list.maybeHead().fromJust(), list.head);
        assertEquals(list.maybeTail().fromJust(), list.tail());
        assertEquals(ImmutableList.cons(a, list).tail(), list);
        assertEquals(Integer.valueOf(a), ImmutableList.cons(a, list).head);
    }

    @Test
    public void testLengthOneGreaterThanTail() {
        testWithSpecialLists(this::testLengthOneGreaterThanTail);
    }

    @Test
    public void testEquals() {
        testWithSpecialLists(this::testEquals);
    }

    public void testEquals(NonEmptyImmutableList<Integer> list) {
        assertEquals(list, list);
        assertNotEquals(list, ImmutableList.<Integer>empty());
    }

    @Test
    public void testReverse() {
        assertEquals(ImmutableList.empty().reverse(), ImmutableList.empty());
        assertEquals(ImmutableList.of(1).reverse(), ImmutableList.of(1));
        assertEquals(ImmutableList.of(1, 2, 3).reverse(), ImmutableList.of(3, 2, 1));
    }

    @Test
    public void testLast() {
        testWithSpecialLists(this::testLast);
    }

    private void testLast(NonEmptyImmutableList<Integer> list) {
        assertEquals(list.last(), list.index(list.length - 1).fromJust());
    }

    @Test
    public void testInit() {
        testWithSpecialLists(this::testInit);
    }

    private void testInit(NonEmptyImmutableList<Integer> list) {
        assertEquals(list.init(), list.take(list.length - 1));
    }

    @Test
    public void testExists() {
        NonEmptyImmutableList<Integer> list = ImmutableList.of(1, 2, 3);
        assertTrue(list.exists((x) -> x == 2));
        assertFalse(list.exists((x) -> x == 4));
    }

    @Test
    public void testContains() {
        NonEmptyImmutableList<Integer> list = ImmutableList.of(1, 2, 3);
        NonEmptyImmutableList<ImmutableList<Integer>> listOfLists = ImmutableList.of(list);
        assertTrue(listOfLists.contains(list));
        assertFalse(listOfLists.contains(ImmutableList.of(1, 2, 3)));
        assertFalse(listOfLists.contains(ImmutableList.empty()));
    }

    @SafeVarargs
    private static <A> ImmutableSet<A> set(A... as) {
        return ImmutableSet.<A>emptyUsingIdentity().putAll(ImmutableList.from(as));
    }

    @Test
    public void testUniq() {
        ImmutableList<Object> list;
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        list = ImmutableList.of(a, a, a);
        assertEquals(ImmutableList.of(a), list.uniqByIdentity().toList());

        list = ImmutableList.of(a, b, c);
        assertEquals(set(a, b, c), list.uniqByIdentity());

        list = ImmutableList.of(a, b, c, a, b, c, a, b, c);
        assertEquals(set(a, b, c), list.uniqByIdentity());

        list = ImmutableList.of(c, b, a, c, b, a, c, b, a);
        assertEquals(set(a, b, c), list.uniqByIdentity());

        ImmutableList<Integer> intList = range(5000, 6000);
        ImmutableSet<Integer> intSet = ImmutableSet.<Integer>emptyUsingEquality().putAll(intList);
        ImmutableSet<Integer> emptySet = ImmutableSet.emptyUsingEquality();
        assertEquals(intSet, intList.uniqByEquality());
        assertNotEquals(emptySet, intList.uniqByEquality());
        assertEquals(intSet, intList.uniqByIdentity());
        assertNotEquals(emptySet, intList.uniqByIdentity());
    }

    private static <A, B> Pair<A, B> p(A a, B b) {
        return new Pair<>(a, b);
    }

    @Test
    public void testUniqOn() {
        ImmutableList<String> list =
            ImmutableList.of("aardvark", "albatross", "alligator", "beaver", "crocodile");
        assertEquals(set("aardvark", "beaver", "crocodile"), list.uniqByEqualityOn(s -> s.substring(0, 1)));
        assertEquals(set("aardvark", "beaver", "crocodile"), list.uniqByEqualityOn(s -> s.charAt(0)));
        assertEquals(set("aardvark", "albatross", "beaver", "crocodile"), list.uniqByEqualityOn(s -> s.substring(0, 2)));
        assertEquals(set("aardvark", "albatross", "alligator", "crocodile"), list.uniqByEqualityOn(s -> s.charAt(s.length() - 1)));

        Pair<String, Integer>
            a0 = p("a", 0), a1 = p("a", 1), a2 = p("a", 2),
            b0 = p("b", 0), b1 = p("b", 1), b2 = p("a", 2),
            c0 = p("c", 0), c1 = p("c", 1), c2 = p("a", 2);
        ImmutableList<Pair<String, Integer>> listOfPairs = ImmutableList.of(a0, a1, a2, b0, b2, b1, c0, c1, c2);
        assertEquals(set(a0, b0, c0), listOfPairs.uniqByEqualityOn(p -> p.left));
        assertEquals(set(a0, a1, a2), listOfPairs.uniqByEqualityOn(p -> p.right));
    }

    @Test
    public void testHashCode() {
        ImmutableList<String> list1 =
                ImmutableList.of("aardvark", "albatross", "alligator", "beaver", "crocodile");
        ImmutableList<String> list2 =
                ImmutableList.of("aardvark", "albatross", "alligator", "beaver", "crocodile");
        assertEquals(list1.hashCode(), list2.hashCode());
        ImmutableList<String> list3 = list1.maybeTail().fromJust();
        ImmutableList<String> list4 = list2.maybeTail().fromJust();
        assertEquals(list3.hashCode(), list4.hashCode());
        assertNotEquals(list1.hashCode(), list3.hashCode());
        assertEquals(list3.cons("test").hashCode(), list4.cons("test").hashCode());
        assertEquals(list3.cons("aardvark").hashCode(), list1.hashCode());

        ImmutableList<String> list5 =
                ImmutableList.of("albatross", "alligator", "beaver", "crocodile");
        int hashCode1 = list5.hashCode();
        assertEquals(list1.hashCode(), list5.cons("aardvark").hashCode());
        assertEquals(hashCode1, list5.hashCode());
    }
}
