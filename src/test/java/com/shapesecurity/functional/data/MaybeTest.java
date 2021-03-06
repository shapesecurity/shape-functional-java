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
import static org.junit.Assert.fail;

import com.shapesecurity.functional.TestBase;
import com.shapesecurity.functional.Thunk;

import javax.annotation.Nullable;
import org.junit.Test;

public class MaybeTest extends TestBase {
    final Integer notNull = 3;
    @Nullable
    final Integer nulled = null;

    @Test
    public void testFromNullable() {
        assertEquals(Maybe.of(3), Maybe.fromNullable(notNull));
        assertEquals(Maybe.<Integer>empty(), Maybe.fromNullable(nulled));
    }

    @Test
    public void testToNullable() {
        assertEquals((Integer) 3, Maybe.of(notNull).toNullable());
        assertEquals(null, Maybe.<Integer>empty().toNullable());
    }

    @Test
    public void testIff() {
        assertEquals(Maybe.of(3), Maybe.iff(true, notNull));
        assertEquals(Maybe.<Integer>empty(), Maybe.iff(false, notNull));
    }

    @Test
    public void testCatMaybes() {
        new ImmutableListTest().testWithSpecialLists(this::testCatMaybes);
    }

    private void testCatMaybes(ImmutableList<Integer> list) {
        assertEquals(list, Maybe.catMaybes(list.map(Maybe::of)));
    }

    @Test
    public void testMapMaybe() {
        new ImmutableListTest().testWithSpecialLists(this::testMapMaybe);
    }

    private void testMapMaybe(ImmutableList<Integer> list) {
        assertEquals(list.map(x -> x + 1), Maybe.mapMaybe(x -> x + 1, list.<Maybe<Integer>>map(Maybe::of)));
    }

    @Test
    public void testBind() {
        assertEquals(Maybe.fromNullable(notNull + 1), Maybe.of(notNull).bind(x -> Maybe.of(
                x + 1))); //not fully sure why I am responsible for wrapping A into a Maybe<A>
        assertEquals(Maybe.<Integer>empty(), Maybe.fromNullable(nulled));
    }

    @Test
    public void testForEach() {
        Maybe.fromNullable(nulled).foreach(x -> {
            fail("Maybe.forEach should not execute f on a empty"); //should never call f
        });
        final int[] effect = {0};
        Maybe.fromNullable(notNull).foreach(x -> {
            effect[0] += 1;
        });
        assertEquals(1, effect[0]);//of should be side effected into incrementing once and only once
    }

    @Test
    public void testForEachOverload() {
        final int[] effect = {0};
        Maybe.empty().foreach(() -> {
            effect[0] += 1;
        }, x -> {
            fail("Maybe.forEach(r, f) should not execute f on Nothing");
        });
        assertEquals(1, effect[0]);

        effect[0] = 0;
        Maybe.of(notNull).foreach(() -> {
            fail("Maybe.forEach(r, f) should not execute r on Just");
        }, x -> {
            effect[0] += 1;
        });
        assertEquals(1, effect[0]);
    }

    @Test
    public void testToList() {
        assertEquals(ImmutableList.<Integer>empty(), Maybe.<Integer>empty().toList());
        assertEquals(ImmutableList.of(notNull), Maybe.of(notNull).toList());
    }

    @Test
    public void testEq() {
        assertTrue(Maybe.fromNullable(notNull).eq(Maybe.of(notNull)));
        assertFalse(Maybe.fromNullable(notNull).eq(Maybe.of(notNull + 1)));
        assertFalse(Maybe.fromNullable(notNull).eq(Maybe.<Integer>empty()));
        assertTrue(Maybe.fromNullable(nulled).eq(Maybe.<Integer>empty()));
        assertFalse(Maybe.fromNullable(nulled).eq(Maybe.fromNullable(notNull)));
    }

    @Test
    public void testOrJusts() {
        assertEquals(notNull, Maybe.fromNullable(notNull).orJust(notNull));
        assertEquals(notNull, Maybe.fromNullable(nulled).orJust(notNull));
        assertEquals(notNull, Maybe.fromNullable(notNull).orJustLazy(Thunk.from(() -> notNull)));
        assertEquals(notNull, Maybe.fromNullable(nulled).orJustLazy(Thunk.from(() -> notNull)));
    }

    @Test
    public void testHashCode() {
        assertEquals(Maybe.fromNullable("hash").hashCode(), Maybe.fromNullable("hash").hashCode());
        assertEquals(Maybe.fromNullable(null).hashCode(), Maybe.fromNullable(null).hashCode());
        assertNotEquals(Maybe.fromNullable("hash").hashCode(), Maybe.fromNullable(null).hashCode());
        assertNotEquals(Maybe.fromNullable(null).hashCode(), Maybe.fromNullable("hash").hashCode());
        assertNotEquals(Maybe.fromNullable("hash").hashCode(), Maybe.fromNullable("not hash").hashCode());
    }

    @Test
    public void testFlatMap() {
        assertEquals(Maybe.of(notNull + 1), Maybe.fromNullable(notNull).flatMap(x -> Maybe.fromNullable(x + 1)));
        assertEquals(Maybe.<Integer>empty(), Maybe.fromNullable(nulled).flatMap(x -> Maybe.fromNullable(x + 1)));
    }

    @Test
    public void testIs() {
        assertTrue(Maybe.fromNullable(nulled).isNothing() && !Maybe.fromNullable(nulled).isJust());
        assertTrue(!Maybe.fromNullable(notNull).isNothing() && Maybe.fromNullable(notNull).isJust());
    }

    @Test
    public void testMaybe() {
        assertEquals(notNull, Maybe.fromNullable(notNull).maybe(1, x -> x));
        assertEquals((Integer) 1, Maybe.fromNullable(nulled).maybe(1, x -> x));
    }

    @Test
    public void testJust() {
        assertEquals(notNull, Maybe.fromNullable(notNull).fromJust());
        try {
            Integer i = Maybe.fromNullable(nulled).fromJust();
            fail("Did not throw NullPointerException");
        } catch (NullPointerException e) {
            // do nothing
        }
    }

    @Test
    public void testTry() {
        try {
            Maybe<Integer> m = Maybe._try(() -> 0);
            assertEquals(Maybe.of(0), m);

            m = Maybe._try(() -> {
                throw new Exception("exception");
            });
            assertEquals(Maybe.empty(), m);
        } catch (Exception e) {
            fail("Maybe._try should never allow exceptions to propagate");
        }
    }

    @Test
    public void testJustIterable() {
        int iterationCount = 0;
        for (String item : Maybe.of("test")) {
            assertEquals("test", item);
            ++iterationCount;
        }
        assertEquals(1, iterationCount);
    }

    @Test
    public void testEmptyIterable() {
        for (String item : Maybe.<String>empty()) {
            fail("Maybe.empty() should not iterate");
        }
    }
}
