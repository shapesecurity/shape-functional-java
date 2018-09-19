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
import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.TestBase;
import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class ImmutableDoubleListTest extends TestBase {

    private void testWithSpecialLists(@Nonnull Effect<ImmutableDoubleList<Integer>> f) {
        f.apply(ImmutableList.ofDouble(0));
        f.apply(ImmutableList.ofDouble(0, 1, 2));
        f.apply(ImmutableList.ofDouble(3, 2, 1));
        f.apply(LONG_DOUBLE_LIST);
    }

    @Test
    public void testList() {
        int a = rand();
        ImmutableDoubleList<Integer> list = (ImmutableDoubleList<Integer>)ImmutableList.fromLinkedList(new LinkedList<>(Arrays.asList(a)));
        assertEquals(list.length, 1);
        assertEquals(list.head().intValue(), a);
        assertEquals(list.tail(), ImmutableList.<Integer>empty());
    }

    @Test
    public void testEquals() {
        testWithSpecialLists(this::testEquals);
    }

    public void testEquals(ImmutableDoubleList<Integer> list) {
        assertEquals(list, list);
        assertNotEquals(list, ImmutableList.empty());
    }

    @Test
    public void testReverse() {
        assertEquals(ImmutableList.empty().reverse(), ImmutableList.empty());
        assertEquals(ImmutableList.ofDouble(1).reverse(), ImmutableList.ofDouble(1));
        assertEquals(ImmutableList.ofDouble(1, 2, 3).reverse(), ImmutableList.ofDouble(3, 2, 1));
    }

    @Test
    public void testLast() {
        testWithSpecialLists(this::testLast);
    }

    private void testLast(ImmutableDoubleList<Integer> list) {
        assertEquals(list.last(), list.index(list.length - 1).fromJust());
    }

    @Test
    public void testInit() {
        testWithSpecialLists(this::testInit);
    }

    private void testInit(ImmutableDoubleList<Integer> list) {
        assertEquals(list.init(), list.take(list.length - 1));
    }

    @Test
    public void testExists() {
        ImmutableDoubleList<Integer> list = ImmutableList.ofDouble(1, 2, 3);
        assertTrue(list.exists(x -> x == 2));
        assertFalse(list.exists(x -> x == 4));
    }

    @Test
    public void testContains() {
        ImmutableDoubleList<Integer> list = ImmutableList.ofDouble(1, 2, 3);
        ImmutableDoubleList<IImmutableList<Integer>> listOfLists = ImmutableList.ofDouble(list);
        assertTrue(listOfLists.contains(list));
        assertFalse(listOfLists.contains(ImmutableList.ofDouble(1, 2, 3)));
        assertFalse(listOfLists.contains(ImmutableList.emptyI()));
    }

    @Test
    public void testMap() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        assertEquals(list, list.map(str -> str));
        ImmutableDoubleList<String> listExpected = ImmutableList.ofDouble("str1x", "str2x", "str3x");
        assertEquals(listExpected, list.map(str -> str + "x"));
        ImmutableDoubleList<String> pre = ImmutableList.ofDouble("str", "str", "str");
        assertEquals(list, pre.mapWithIndex((i, str) -> str + Integer.toString(i + 1)));
        assertEquals(6, list.chain(str -> ImmutableList.of(str + "n", str + "x")).length());
        Pair<StringBuilder, INonEmptyImmutableList<String>> newList = list.mapAccumL((acc, str) -> new Pair<>(acc.append(str), str + "x"), new StringBuilder());
        assertEquals(listExpected, newList.right);
        assertEquals("str1str2str3", newList.left.toString());
    }

    @Test
    public void testFold() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        String concatLeft = list.foldLeft((acc, str) -> acc + str, "");
        assertEquals("str1str2str3", concatLeft);
        String concatRight = list.foldRight((str, acc) -> acc + str, "");
        assertEquals("str3str2str1", concatRight);
    }

    @Test
    public void testSlice() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> listSlice1 = ImmutableList.ofDouble("str1", "str2");
        ImmutableDoubleList<String> listSlice2 = ImmutableList.ofDouble("str2", "str3");
        assertEquals(list.slice(0, list.length - 1), listSlice1);
        assertEquals(list.init(), listSlice1);
        assertEquals(list.slice(1, list.length), listSlice2);
        assertEquals(list.tail(), listSlice2);
    }

    @Test
    public void testCons() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> listAppend = ImmutableList.ofDouble("str1", "str2", "str3", "test");
        ImmutableDoubleList<String> listPrepend = ImmutableList.ofDouble("test", "str1", "str2", "str3");
        assertEquals(listAppend, list.append("test"));
        assertEquals(listPrepend, list.prepend("test"));
        assertEquals(listPrepend, list.cons("test"));
    }

    @Test
    public void testHashCode() {
        ImmutableDoubleList<String> list1 = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> list2 = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> list3 = ImmutableList.ofDouble("str1", "str2", "str3", "test");
        assertEquals(list1.hashCode(), list2.hashCode());
        assertNotEquals(list1.hashCode(), list3.hashCode());
    }

    @Test
    public void testFilter() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3", "test");
        ImmutableDoubleList<String> listFiltered = ImmutableList.ofDouble("str1", "str2", "str3");
        assertEquals(listFiltered, list.filter(str -> str.startsWith("str")));
        assertEquals(listFiltered, list.removeAll(str -> !str.startsWith("str")));
    }

    @Test
    public void testCount() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3", "test");
        assertEquals(3, list.count(str -> str.startsWith("str")));
        assertEquals(1, list.count(str -> !str.startsWith("str")));
    }

    @Test
    public void testNonEmptyListConversion() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        NonEmptyImmutableList<String> nonEmptyList = ImmutableList.of("str1", "str2", "str3");
        assertEquals(nonEmptyList, list.toNonEmptyList().fromJust());
    }

    @Test
    public void testDecons() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> expectedTail = ImmutableList.ofDouble("str2", "str3");
        assertEquals("test", list.decons((head, tail) -> {
            assertEquals("str1", head);
            assertEquals(expectedTail, tail);
            return "test";
        }).fromJust());
    }

    @Test
    public void testZipWith() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> expectedDoubledList = ImmutableList.ofDouble("str1str1", "str2str2", "str3str3");
        INonEmptyImmutableList<String> doubledList = list.zipWith((str1, str2) -> str1 + str2, list);
        assertEquals(expectedDoubledList, doubledList);
    }

    @Test
    public void testEmpty() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        assertFalse(list.isEmpty());
        assertTrue(list.isNotEmpty());
        assertEquals(3, list.length);
        assertTrue(ImmutableList.empty().isEmpty());
        assertFalse(ImmutableList.empty().isNotEmpty());
        assertEquals(0, ImmutableList.empty().length);
    }

    @Test
    public void testAppend() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> expectedDupList = ImmutableList.ofDouble("str1", "str2", "str3", "str1", "str2", "str3");
        INonEmptyImmutableList<String> dupList = list.append(list);
        assertEquals(expectedDupList, dupList);
    }

    @Test
    public void testFind() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        Maybe<String> foundString = list.find(str -> str.equals("str2"));
        assertTrue(foundString.isJust());
        assertEquals("str2", foundString.fromJust());
        foundString = list.findMap(str -> str.equals("str3") ? Maybe.of(str) : Maybe.empty());
        assertTrue(foundString.isJust());
        assertEquals("str3", foundString.fromJust());
        foundString = list.find(str -> str.equals("nothere"));
        assertFalse(foundString.isJust());
        foundString = list.findMap(str -> str.equals("nothere") ? Maybe.of(str) : Maybe.empty());
        assertFalse(foundString.isJust());
        Maybe<Integer> foundIndex = list.findIndex(str -> str.equals("str2"));
        assertTrue(foundIndex.isJust());
        assertEquals(1, foundIndex.fromJust().intValue());
        assertEquals("str2", list.index(foundIndex.fromJust()).fromJust());
        foundIndex = list.findIndex(str -> str.equals("nothere"));
        assertFalse(foundIndex.isJust());
        foundIndex = list.findIndexEquality("str1");
        assertTrue(foundIndex.isJust());
        assertEquals(0, foundIndex.fromJust().intValue());
        assertEquals("str1", list.index(foundIndex.fromJust()).fromJust());
        foundIndex = list.findIndexEquality("nothere");
        assertFalse(foundIndex.isJust());
        foundIndex = list.findIndexIdentity("str2"); // const strs do equal identity
        assertTrue(foundIndex.isJust());
        assertEquals(1, foundIndex.fromJust().intValue());
        assertEquals("str2", list.index(foundIndex.fromJust()).fromJust());
        foundIndex = list.findIndexIdentity("nothere");
        assertFalse(foundIndex.isJust());
    }

    @Test
    public void testSpan() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3", "test");
        ImmutableDoubleList<String> expectedSpan = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> expectedLeftover = ImmutableList.ofDouble("test");
        Pair<IImmutableList<String>, IImmutableList<String>> result = list.span(f -> f.startsWith("str"));
        assertEquals(expectedSpan, result.left);
        assertEquals(expectedLeftover, result.right);
    }

    @Test
    public void testIteration() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list) {
            stringBuilder.append(str);
        }
        assertEquals("str1str2str3", stringBuilder.toString());
    }

    @Test
    public void testForEach() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        StringBuilder stringBuilder = new StringBuilder();
        list.forEach(stringBuilder::append);
        assertEquals("str1str2str3", stringBuilder.toString());
    }

    @Test
    public void testReverseIteration() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = list.reverseIterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
        }
        assertEquals("str3str2str1", stringBuilder.toString());
    }

    @Test
    public void testReverseIterable() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list.reverseIterable()) {
            stringBuilder.append(str);
        }
        assertEquals("str3str2str1", stringBuilder.toString());
    }

    @Test
    public void testToArray() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        String[] array = list.toArray(new String[0]);
        assertArrayEquals(new String[]{"str1", "str2", "str3"}, array);
    }

    @Test
    public void testPatch() {
        ImmutableDoubleList<String> list = ImmutableList.ofDouble("str1", "str2", "str3");
        ImmutableDoubleList<String> expectedPatchList = ImmutableList.ofDouble("str1", "test", "str3");
        assertEquals(expectedPatchList, list.patch(1, 1, ImmutableList.ofDouble("test")));
    }

    private static <A, B> Pair<A, B> p(A a, B b) {
        return new Pair<>(a, b);
    }

    @SafeVarargs
    private static <A> ImmutableSet<A> set(A... as) {
        return ImmutableSet.<A>emptyUsingIdentity().putAll(ImmutableList.from(as));
    }

    @Test
    public void testUniq() {
        IImmutableList<Object> list;
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();

        list = ImmutableList.ofDouble(a, a, a);
        assertEquals(ImmutableList.of(a), list.uniqByIdentity().toList());

        list = ImmutableList.ofDouble(a, b, c);
        assertEquals(set(a, b, c), list.uniqByIdentity());

        list = ImmutableList.ofDouble(a, b, c, a, b, c, a, b, c);
        assertEquals(set(a, b, c), list.uniqByIdentity());

        list = ImmutableList.ofDouble(c, b, a, c, b, a, c, b, a);
        assertEquals(set(a, b, c), list.uniqByIdentity());
    }

    @Test
    public void testUniqOn() {
        IImmutableList<String> list =
            ImmutableList.ofDouble("aardvark", "albatross", "alligator", "beaver", "crocodile");
        assertEquals(set("aardvark", "beaver", "crocodile"), list.uniqByEqualityOn(s -> s.substring(0, 1)));
        assertEquals(set("aardvark", "beaver", "crocodile"), list.uniqByEqualityOn(s -> s.charAt(0)));
        assertEquals(set("aardvark", "albatross", "beaver", "crocodile"), list.uniqByEqualityOn(s -> s.substring(0, 2)));
        assertEquals(set("aardvark", "albatross", "alligator", "crocodile"), list.uniqByEqualityOn(s -> s.charAt(s.length() - 1)));

        Pair<String, Integer>
            a0 = p("a", 0), a1 = p("a", 1), a2 = p("a", 2),
            b0 = p("b", 0), b1 = p("b", 1), b2 = p("a", 2),
            c0 = p("c", 0), c1 = p("c", 1), c2 = p("a", 2);
        IImmutableList<Pair<String, Integer>> listOfPairs = ImmutableList.ofDouble(a0, a1, a2, b0, b2, b1, c0, c1, c2);
        assertEquals(set(a0, b0, c0), listOfPairs.uniqByEqualityOn(p -> p.left));
        assertEquals(set(a0, a1, a2), listOfPairs.uniqByEqualityOn(p -> p.right));
    }
}
