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

import com.shapesecurity.functional.Pair;
import com.shapesecurity.functional.TestBase;
import com.shapesecurity.functional.Unit;

import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HashTableTest extends TestBase {
    private static final Hasher<String> BAD_HASHER = new Hasher<String>() {
        @Override
        public int hash(@Nonnull String data) {
            return 0;
        }

        @Override
        public boolean eq(@Nonnull String s, @Nonnull String b) {
            return s.equals(b);
        }
    };
    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;

    static class DummyWithEquals {
        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof DummyWithEquals;
        }
    }

    @Test
    public void simpleTests() {
        HashTable<String, Integer> e = HashTable.emptyUsingEquality();
        int N = 100000;
        for (int i = 0; i < N; i++) {
            e = e.put(Integer.toString(i), i);
        }
        HashTable<String, Integer> e1 = e.put("a", 3);
        HashTable<String, Integer> e2 = e1.put("b", 3);
        HashTable<String, Integer> e3 = e1.put("c", 3);
        HashTable<String, Integer> e4 = e1.put("a", 4);

        assertEquals(N, e.length);
        assertEquals(N + 1, e1.length);
        assertEquals(N + 2, e2.length);
        assertEquals(N + 2, e3.length);
        assertEquals(N + 1, e4.length);

        assertEquals(Maybe.<Integer>empty(), e.get("a"));
        assertEquals(Maybe.<Integer>empty(), e.get("b"));
        assertEquals(Maybe.<Integer>empty(), e.get("c"));

        assertEquals(Maybe.of(3), e1.get("a"));
        assertEquals(Maybe.<Integer>empty(), e1.get("b"));
        assertEquals(Maybe.<Integer>empty(), e1.get("c"));

        assertEquals(Maybe.of(3), e2.get("a"));
        assertEquals(Maybe.of(3), e2.get("b"));
        assertEquals(Maybe.<Integer>empty(), e2.get("c"));

        assertEquals(Maybe.of(3), e3.get("a"));
        assertEquals(Maybe.<Integer>empty(), e3.get("b"));
        assertEquals(Maybe.of(3), e3.get("c"));

        assertEquals(Maybe.of(4), e4.get("a"));
        assertEquals(Maybe.<Integer>empty(), e4.get("b"));
        assertEquals(Maybe.<Integer>empty(), e4.get("c"));

        e4 = e4.put("a", 5);
        assertEquals(Maybe.of(5), e4.get("a"));
        assertEquals(Maybe.<Integer>empty(), e4.get("b"));
        assertEquals(Maybe.<Integer>empty(), e4.get("c"));
    }

    protected static long next(long seed) {
        return (seed * multiplier + addend) & mask;
    }

    @Test
    public void deletionTest() {
        HashTable<String, Integer> e = HashTable.emptyUsingEquality();
        int N = 100000;
        for (int i = 0; i < N; i++) {
            e = e.put(Integer.toString(i), i);
        }
        int[] shuffled = shuffle(0x12345, N);

        for (int i = 0; i < N / 2; i++) {
            String key = Integer.toString(shuffled[i]);
            assertEquals(Maybe.of(shuffled[i]), e.get(key));
            e = e.remove(key);
            assertEquals(Maybe.<Integer>empty(), e.get(key));
            assertEquals(N - i - 1, e.length);
            assertEquals(e.length, e.remove(key).length);
        }
        for (int i = N / 2; i < N; i++) {
            String key = Integer.toString(shuffled[i]);
            assertEquals(Maybe.of(shuffled[i]), e.get(key));
        }
    }

    @Test
    public void deletionTest2() {
        HashTable<String, Integer> e = HashTable.empty(BAD_HASHER);
        int N = 1000;
        for (int i = 0; i < N; i++) {
            e = e.put(Integer.toString(i), i);
        }
        int[] shuffled = shuffle(0x12345, N);

        for (int i = 0; i < N / 2; i++) {
            String key = Integer.toString(shuffled[i]);
            assertEquals(Maybe.of(shuffled[i]), e.get(key));
            e = e.remove(key);
            assertEquals(Maybe.<Integer>empty(), e.get(key));
            assertEquals(N - i - 1, e.length);
        }
        for (int i = N / 2; i < N; i++) {
            String key = Integer.toString(shuffled[i]);
            assertEquals(Maybe.of(shuffled[i]), e.get(key));
        }
    }

    protected static int[] shuffle(long seed, int n) {
        int[] shuffled = new int[n];
        for (int i = 0; i < n; i++) {
            seed = next(seed);
            int j = (int) (seed % (i + 1));
            if (j != i) {
                shuffled[i] = shuffled[j];
            }
            shuffled[j] = i;
        }
        return shuffled;
    }

    @Test
    public void traversalTests() {
        HashTable<String, Integer> e = HashTable.emptyUsingEquality();
        int N = 10000;
        for (int i = 0; i < N; i++) {
            e = e.put(Integer.toString(i), i);
        }
        final boolean[] visited = new boolean[N];

        final int[] count = new int[1];
        assertEquals(N, e.length);
        e.entries().foreach(p -> {
            assertEquals(p.left, Integer.toString(p.right));
            assertFalse(visited[p.right]);
            visited[p.right] = true;
            count[0]++;
        });
        assertEquals(N, count[0]);
    }

    @Test
    public void mergeTestSimple() {
        HashTable<String, Integer> t1 = HashTable.emptyUsingEquality();
        HashTable<String, Integer> t2 = HashTable.emptyUsingEquality();
        assertEquals(0, t1.merge(t2).length);
        t1 = t1.put("a", 1);
        assertEquals(1, t1.merge(t2).length);
        t1 = t1.put("b", 1);
        assertEquals(2, t1.merge(t2).length);
        t2 = t2.put("b", 2);
        assertEquals(2, t1.merge(t2).length);
        assertEquals(2, (int) t1.merge(t2).get("b").fromJust());
    }

    @Test
    public void mergeTest() {
        HashTable<String, Integer> t1 = HashTable.emptyUsingEquality();
        HashTable<String, Integer> t2 = HashTable.emptyUsingEquality();
        int N = 10000;
        int[] shuffled = shuffle(0x12345, N);
        for (int i = 0; i < N; i += 2) {
            t1 = t1.put(Integer.toString(shuffled[i]), shuffled[i]);
            t2 = t2.put(Integer.toString(shuffled[i + 1]), shuffled[i + 1]);
        }
        HashTable<String, Integer> t = t1.merge(t2);
        assertEquals(N, t.length);
        for (int i = 0; i < N; i++) {
            assertTrue(t.get(Integer.toString(i)).isJust());
        }
    }

    @Test
    public void mergeTest2() {
        HashTable<String, Integer> t1 = HashTable.empty(BAD_HASHER);
        HashTable<String, Integer> t2 = HashTable.empty(BAD_HASHER);
        int N = 1000;
        int[] shuffled = shuffle(0x12345, N);
        for (int i = 0; i < N; i += 2) {
            t1 = t1.put(Integer.toString(shuffled[i]), shuffled[i]);
            t2 = t2.put(Integer.toString(shuffled[i + 1]), shuffled[i + 1]);
        }
        HashTable<String, Integer> t = t1.merge(t2);
        assertEquals(N, t.length);
        for (int i = 0; i < N; i++) {
            assertTrue(t.get(Integer.toString(i)).isJust());
        }
    }

    @Test
    public void getTest() {
        HashTable<String, Integer> t = HashTable.empty(BAD_HASHER);
        int N = 1000;
        for (int i = 0; i < N; i++) {
            t = t.put(Integer.toString(i), i);
        }

        for (int i = 0; i < N; i++) {
            assertEquals((Integer) i, t.get(Integer.toString(i)).fromJust());
        }
        for (int i = 0; i < N; i++) {
            assertEquals(Maybe.<Integer>empty(), t.get(Integer.toString(i + N)));
        }
    }

    @Test
    public void findTest() {
        HashTable<String, Integer> t = HashTable.emptyUsingEquality();
        assertEquals(Maybe.<Pair<String, Integer>>empty(), t.find(x -> x.left.equals("a")));
        int N = 1000;
        for (int i = 0; i < N; i++) {
            t = t.put(Integer.toString(i), i);
        }
        for (int i = 0; i < N; i++) {
            int iFinal = i;
            assertEquals(i, (int) t.find(x -> x.right == iFinal).fromJust().right);
        }
    }

    @Test
    public void findMapTest() {
        int N = 1000;
        HashTable<String, Integer> empty = HashTable.<String, Integer>emptyUsingEquality();
        assertEquals(Maybe.<Integer>empty(), empty.findMap(x -> Maybe.of(0)));
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> {
            return ht.put(Integer.toString(i), i);
        }, empty);

        range(0, N).foreach(i -> {
            assertEquals(Integer.toString(i), t.findMap(x -> Maybe.iff(x.right.equals(i), x.left)).fromJust());
        });
    }

    @Test
    public void foldLeftTest() {
        HashTable.<String, Integer>emptyUsingEquality().foldLeft((i, a) -> {
            throw new RuntimeException("not reached");
        }, 0);
        int N = 10000;
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(Integer.toString(i), i),
                HashTable.<String, Integer>emptyUsingEquality());
        assertEquals(N * (N - 1) / 2, (int) t.foldLeft((a, i) -> a + i.right, 0));
    }

    @Test
    public void foldRightTest() {
        HashTable.<String, Integer>emptyUsingEquality().foldRight((a, i) -> {
            throw new RuntimeException("not reached");
        }, 0);
        int N = 10000;
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(Integer.toString(i), i),
                HashTable.<String, Integer>emptyUsingEquality());
        assertEquals(N * (N - 1) / 2, (int) t.foldRight((i, a) -> a + i.right, 0));
    }

    @Test
    public void forEachTest() {
        HashTable.<String, Integer>emptyUsingEquality().foreach((p) -> {
            throw new RuntimeException("not reached");
        });
        int N = 10000;
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(Integer.toString(i), i),
                HashTable.<String, Integer>emptyUsingEquality());
        int a[] = new int[1];
        t.foreach(entry -> a[0] += entry.right);
        assertEquals(N * (N - 1) / 2, a[0]);
    }

    @Test
    public void mapTest() {
        assertEquals(0, HashTable.<String, Integer>emptyUsingEquality().map(x -> x + 1).length);
        int N = 10000;
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(Integer.toString(i), i),
                HashTable.<String, Integer>emptyUsingEquality());
        t = t.map(x -> x + 1);
        t.foreach(pair -> {
            assertEquals(Integer.parseInt(pair.left), pair.right - 1);
        });
    }

    @Test
    public void containsKeyTest() {
        HashTable<Integer, Unit> m = HashTable.emptyUsingEquality();

        assertFalse(m.containsKey(0));
        assertFalse(m.containsKey(1));
        assertFalse(m.containsKey(2));
        assertFalse(m.containsKey(3));

        m = m.put(0, Unit.unit);

        assertTrue(m.containsKey(0));
        assertFalse(m.containsKey(1));
        assertFalse(m.containsKey(2));
        assertFalse(m.containsKey(3));

        m = m.put(2, Unit.unit);

        assertTrue(m.containsKey(0));
        assertFalse(m.containsKey(1));
        assertTrue(m.containsKey(2));
        assertFalse(m.containsKey(3));


    }

    @Test
    public void removePreservesHasherTest() {
        HashTable<DummyWithEquals, Integer> table = HashTable.emptyUsingIdentity();

        DummyWithEquals one = new DummyWithEquals();
        DummyWithEquals two = new DummyWithEquals();
        table = table.put(one, 1);
        table = table.remove(one);
        table = table.put(one, 1);
        table = table.put(two, 2);
		assertEquals(1, (int) table.get(one).fromJust());
		assertEquals(2, (int) table.get(two).fromJust());
    }

    @Test
    public void mapPreservesHasherTest() {
        HashTable<DummyWithEquals, Integer> table = HashTable.emptyUsingIdentity();
        table = table.map(x -> x);

        DummyWithEquals one = new DummyWithEquals();
        DummyWithEquals two = new DummyWithEquals();
        table = table.put(one, 1);
        table = table.put(two, 2);
		assertEquals(1, (int) table.get(one).fromJust());
		assertEquals(2, (int) table.get(two).fromJust());
    }

    @Test
    public void iterableTest() {
        for (Pair<String, String> string : HashTable.<String, String>emptyUsingEquality()) {
            fail("Empty Hashtable Iterated");
        }
        int N = 10000;
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(Integer.toString(i), i),
            HashTable.emptyUsingEquality());
        int sum = 0;
        for (Pair<String, Integer> i : t) {
            assertEquals(Integer.toString(i.right), i.left);
            sum += i.right;
        }
        assertEquals(N * (N - 1) / 2, sum);
    }

    @Test
    public void clearedIterationTest() {
        HashTable<String, String> table = HashTable.emptyUsingEquality();
        table = table.put("key1", "value1")
            .put("key2", "value2")
            .put("key3", "value3");

        table = table.remove("key1")
            .remove("key2")
            .remove("key3");

        for (Pair<String, String> pair : table) {
            fail("Empty Hashtable Iterated");
        }
    }

    @Test
    public void postRemovalIterationTest() {
        int N = 10;
        HashTable<String, Integer> t = range(0, N).foldLeft((ht, i) -> ht.put(Integer.toString(i), i),
                HashTable.emptyUsingEquality());
        for (int i = 0; i < N; i += 2) {
            t = t.remove(Integer.toString(i));
        }
        int sum = 0;
        for (Pair<String, Integer> i : t) {
            assertEquals(Integer.toString(i.right), i.left);
            sum += i.right;
        }
        assertEquals(N * N / 4, sum);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    protected static <V> List<Pair<String, V>> prepareForAssertion(@Nonnull HashTable<String, V> table) {
        List<Pair<String, V>> list = new ArrayList<>(Arrays.asList(table.entries().toArray((Pair<String, V>[]) new Pair[0])));
        list.sort(Comparator.comparing(pair -> pair.left));
        return list;
    }

    @Test
    public void mutableRoundTripTest() {
        HashTable<String, String> expected = HashTable.<String, String>emptyUsingEquality()
            .put("key1", "value1")
            .put("key2", "value2")
            .put("key3", "value3");
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        HashTable<String, String> table = HashTable.fromUsingEquality(map);
        assertEquals(prepareForAssertion(expected), prepareForAssertion(table));
        HashTable<String, String> doubledTable = table.putAllFrom(map);
        assertEquals(prepareForAssertion(table), prepareForAssertion(doubledTable));
        map.put("key4", "value4");
        expected = expected.put("key4", "value4");
        assertEquals(prepareForAssertion(expected), prepareForAssertion(table.putAllFrom(map)));
        assertEquals(map, expected.toHashMap());

        IdentityHashMap<Integer, Integer> mapIdentity = new IdentityHashMap<>();
        mapIdentity.put(1, 2);
        mapIdentity.put(2, 3);
        mapIdentity.put(3, 4);
        HashTable<Integer, Integer> tableIdentity = HashTable.<Integer, Integer>emptyUsingIdentity()
            .put(1, 2)
            .put(2, 3)
            .put(3, 4);
        assertEquals(mapIdentity, tableIdentity.toIdentityHashMap());
    }
}
