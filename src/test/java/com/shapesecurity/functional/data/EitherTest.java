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

import com.shapesecurity.functional.F;

import org.junit.Test;

public class EitherTest {
    @Test
    public void simpleTests() {
        Either<String, Integer> e1 = Either.left("a");
        Either<String, Integer> e2 = Either.right(1);
        F<Integer, Integer> plusOne = integer -> integer + 1;

        assertTrue(e1.isLeft());
        assertFalse(e1.isRight());
        assertFalse(e2.isLeft());
        assertTrue(e2.isRight());
        assertTrue(e1.left().isJust());
        assertTrue(e1.right().isNothing());
        assertEquals("a", e1.left().fromJust());
        assertEquals("a", e1.left().fromJust());
        assertEquals("a", e1.fromLeft());
        assertTrue(e1.mapRight(plusOne).left().isJust());
        assertTrue(e1.mapRight(plusOne).right().isNothing());
        assertEquals("a", e1.mapRight(plusOne).left().fromJust());
        assertEquals("a", e1.mapRight(plusOne).left().fromJust());
        assertTrue(e1.map(F.id(), plusOne).left().isJust());
        assertTrue(e1.map(F.id(), plusOne).right().isNothing());
        assertEquals("a", e1.map(F.id(), plusOne).left().fromJust());
        assertEquals("a", e1.map(F.id(), plusOne).left().fromJust());

        assertTrue(e2.left().isNothing());
        assertTrue(e2.right().isJust());
        assertEquals(1, (int) e2.right().fromJust());
        assertEquals(1, (int) e2.right().fromJust());
        assertEquals(1, (int) e2.fromRight());
        assertTrue(e2.mapRight(plusOne).left().isNothing());
        assertTrue(e2.mapRight(plusOne).right().isJust());
        assertEquals(2, (int) e2.mapRight(plusOne).right().fromJust());
        assertEquals(2, (int) e2.mapRight(plusOne).right().fromJust());
        assertTrue(e2.map(F.id(), plusOne).left().isNothing());
        assertTrue(e2.map(F.id(), plusOne).right().isJust());
        assertEquals(2, (int) e2.map(F.id(), plusOne).right().fromJust());
        assertEquals(2, (int) e2.map(F.id(), plusOne).right().fromJust());

        assertEquals(Either.left(3), Either.left(3));
        assertEquals(Either.right(3), Either.right(3));
        assertNotEquals(Either.left(3), Either.right(3));
        assertNotEquals(Either.right(3), Either.left(3));

        Either.<Integer, Integer>left(1).foreach(x -> { assertEquals(1, x.intValue()); }, x -> { fail(); });
        Either.<Integer, Integer>right(1).foreach(x -> { fail(); }, x -> { assertEquals(1, x.intValue()); });
    }

    @Test
    public void extractTest() {
        assertEquals((Integer) 3, Either.extract(Either.left(3)));
        assertEquals((Integer) 4, Either.extract(Either.right(4)));

        assertEquals("a", Either.extract(Either.<Integer, String>right("a")));
        assertEquals(3, Either.extract(Either.<Integer, String>left(3)));
    }

    @Test
    public void tryTest() {
        Exception exception = new Exception("exception");
        try {
            Either<Exception, Integer> e = Either._try(() -> 0);
            assertEquals(Either.right(0), e);

            e = Either._try(() -> {
                throw exception;
            });
            assertEquals(Either.left(exception), e);
        } catch (Exception e) {
            fail("Either._try should never allow exceptions to propagate");
        }
    }

    @Test(expected = RuntimeException.class)
    public void testFromLeft() {
        Either.right("x").fromLeft();
    }

    @Test(expected = RuntimeException.class)
    public void testFromRight() {
        Either.left("x").fromRight();
    }
}
