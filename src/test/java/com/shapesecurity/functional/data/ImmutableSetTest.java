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

import com.shapesecurity.functional.TestBase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ImmutableSetTest extends TestBase {

    @Test
    public void mergeTestSimple() {
        ImmutableSet<String> t1 = ImmutableSet.emptyUsingEquality();
        ImmutableSet<String> t2 = ImmutableSet.emptyUsingEquality();
        assertEquals(0, t1.union(t2).length());
        t1 = t1.put("a");
        assertEquals(1, t1.union(t2).length());
        t1 = t1.put("b");
        assertEquals(2, t1.union(t2).length());
        t2 = t2.put("b");
        assertEquals(2, t1.union(t2).length());
        assertTrue(t1.union(t2).contains("b"));
    }

    @Test
    public void unionTest() {
        ImmutableSet<String> t1 = ImmutableSet.emptyUsingEquality();
        ImmutableSet<String> t2 = ImmutableSet.emptyUsingEquality();
        int N = 10000;
        int[] shuffled = HashTableTest.shuffle(0x12345, N);
        for (int i = 0; i < N; i += 2) {
            t1 = t1.put(Integer.toString(shuffled[i]));
            t2 = t2.put(Integer.toString(shuffled[i + 1]));
        }
        ImmutableSet<String> t = t1.union(t2);
        assertEquals(N, t.length());
        for (int i = 0; i < N; i++) {
            assertTrue(t.contains(Integer.toString(i)));
        }
    }

    @Test
    public void iterableTest() {
        for (String string : ImmutableSet.<String>emptyUsingEquality()) {
            fail("Empty ImmutableSet Iterated");
        }
        int N = 10000;
        ImmutableSet<Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(i),
                ImmutableSet.emptyUsingEquality());
        int sum = 0;
        for (Integer i : t) {
            sum += i;
        }
        assertEquals(N * (N - 1) / 2, sum);
    }

    @Test
    public void mapTest() {
        assertEquals(0, ImmutableSet.<String>emptyUsingEquality().map(x -> x + 1).length());
        int N = 10000;
        ImmutableSet<Integer> t = range(0, N).foldLeft(ImmutableSet::put, ImmutableSet.emptyUsingEquality());
        ImmutableSet<String> mapped = t.map(x -> x + "_test");

        ImmutableSet<String> expected = range(0, N).foldLeft((acc, i) -> acc.put(Integer.toString(i) + "_test"), ImmutableSet.emptyUsingEquality());
        assertEquals(expected, mapped);
    }

    @Test
    public void containsKeyTest() {
        ImmutableSet<Integer> m = ImmutableSet.emptyUsingEquality();

        assertFalse(m.contains(0));
        assertFalse(m.contains(1));
        assertFalse(m.contains(2));
        assertFalse(m.contains(3));

        m = m.put(0);

        assertTrue(m.contains(0));
        assertFalse(m.contains(1));
        assertFalse(m.contains(2));
        assertFalse(m.contains(3));

        m = m.put(2);

        assertTrue(m.contains(0));
        assertFalse(m.contains(1));
        assertTrue(m.contains(2));
        assertFalse(m.contains(3));
    }

    @Test
    public void mutableUnionTest() {
        ImmutableSet<String> expected = ImmutableSet.<String>emptyUsingEquality()
            .put("key1")
            .put("key2")
            .put("key3");
        Set<String> set = new HashSet<>();
        set.add("key1");
        set.add("key2");
        set.add("key3");
        ImmutableSet<String> table = ImmutableSet.fromUsingEquality(set);
        assertEquals(expected, table);
        ImmutableSet<String> doubledSet = table.union(set);
        assertEquals(table, doubledSet);
        set.add("key4");
        expected = expected.put("key4");
        assertEquals(expected, table.union(set));
    }


    @Test
    public void flatMapTest() {
        ImmutableSet<String> expected = ImmutableSet.<String>emptyUsingEquality()
            .put("key1")
            .put("key2")
            .put("key3");
        ImmutableSet<String> mappedSet = expected.flatMap(string -> ImmutableSet.<String>emptyUsingEquality().put(string + "1").put(string + "2"));
        assertEquals(ImmutableSet.<String>emptyUsingEquality()
                .put("key11")
                .put("key21")
                .put("key31")
                .put("key12")
                .put("key22")
                .put("key32"),
            mappedSet
        );
    }

    @Test
    public void filterTestEquality() {
        ImmutableSet<String> expected = ImmutableSet.<String>emptyUsingEquality()
            .put("key1")
            .put("key2")
            .put("keyx2")
            .put("key3");
        ImmutableSet<String> filteredSet = expected.filter(string -> string.endsWith("2"));
        assertEquals(expected.hasher(), filteredSet.hasher());

        assertEquals(ImmutableSet.<String>emptyUsingEquality()
                .put("key2")
                .put("keyx2"),
            filteredSet
        );
    }

    @Test
    public void filterTestIdentity() {
        ImmutableSet<Integer> expected = ImmutableSet.<Integer>emptyUsingIdentity()
            .put(2)
            .put(3)
            .put(4)
            .put(5);
        ImmutableSet<Integer> filteredSet = expected.filter(i -> i % 2 == 0);
        assertEquals(expected.hasher(), filteredSet.hasher());

        assertEquals(ImmutableSet.<Integer>emptyUsingIdentity()
                .put(2)
                .put(4),
            filteredSet
        );
    }

    @Test
    public void toSetTest() {
        ImmutableSet<String> immutable = ImmutableSet.<String>emptyUsingEquality()
            .put("key1")
            .put("key2")
            .put("key3");
        Set<String> mutable = new HashSet<>();
        mutable.add("key1");
        mutable.add("key2");
        mutable.add("key3");
        assertEquals(mutable, immutable.toSet());
    }

    @Test
    public void mapToTableTest() {
        ImmutableSet<String> set = ImmutableSet.<String>emptyUsingEquality()
            .put("key1")
            .put("key2")
            .put("key3");
        HashTable<String, String> table = set.mapToTable(key -> key.replace("key", "value"));

        assertEquals("value1", table.get("key1").fromJust());
        assertEquals("value2", table.get("key2").fromJust());
        assertEquals("value3", table.get("key3").fromJust());
        assertEquals(set.length(), table.entries().length);
    }
    
    @Test
    public void testStream() {
        ImmutableSet<Integer> set = ImmutableList.of(1, 2, 3, 4, 5).uniqByEquality();
        List<Integer> mutableList = set.stream().sorted().collect(Collectors.toList());
        List<Integer> mutableListFromList = StreamSupport.stream(set.toList().spliterator(), false)
            .sorted(Integer::compareTo).collect(Collectors.toList());
        assertEquals(mutableList, mutableListFromList);
    }

    @Test
    public void testCollector() {
        ImmutableSet<String> set = ImmutableList.of("1", "2", "3", "4", "5", "5").uniqByEquality();
        ImmutableSet<String> streamed = Stream.of("1", "2", "3", "4", "5", "5").collect(ImmutableSet.collector());
        assertEquals(set, streamed);
        assertEquals(5, set.length());
    }

    @Test
    public void testPutAll() {
        ImmutableSet<String> set = ImmutableSet.of("key1", "key2", "key3");
        assertEquals(set, set.putArray());
        ImmutableSet<String> set2 = set.putArray("key4", "key5");
        assertTrue(set2.contains("key4"));
        assertTrue(set2.contains("key2"));
        assertEquals(set2, ImmutableList.of("key1", "key2", "key3", "key4", "key5").uniqByEquality());
    }
}
