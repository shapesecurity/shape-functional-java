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

public class BenchmarkImmutableList extends TestBase {
    public static void main(String[] args) {
        System.out.print("benchmarkEquals: ");
        benchmarkEquals();
        System.out.print("benchmarkForeach: ");
        benchmarkForeach();
        System.out.print("benchmarkAppend: ");
        benchmarkAppend();
        System.out.print("benchmarkReverse: ");
        benchmarkReverse();
        System.out.print("benchmarkToArray: ");
        benchmarkToArray();
        System.out.print("benchmarkPartition: ");
        benchmarkPartition();
    }

    private static void benchmarkEquals() {
        ImmutableList<Integer> l = LONG_INT_LIST;
        ImmutableList<Integer> r = ImmutableList.from(LONG_INT_LIST.toArray(Integer[]::new));
        int warmup = 100000;
        for (int i = 0; i < warmup; i++) {
            l.equals(r);
        }

        int measure = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            l.equals(r);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
    }

    private static void benchmarkForeach() {
        ImmutableList<Integer> l = LONG_INT_LIST;
        int[] acc = new int[1];
        int warmup = 100000;
        for (int i = 0; i < warmup; i++) {
            l.forEach(n -> acc[0] += (n % 137 << 7) ^ 12);
        }

        int measure = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            l.forEach(n -> acc[0] += (n % 137 << 7) ^ 12);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
    }

    private static void benchmarkAppend() {
        int warmup = 100000;
        for (int i = 0; i < warmup; i++) {
            LONG_INT_LIST.append(LONG_INT_LIST);
        }

        int measure = 1000;
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            LONG_INT_LIST.append(LONG_INT_LIST);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
    }

    private static void benchmarkReverse() {
        int warmup = 10000;
        for (int i = 0; i < warmup; i++) {
            LONG_INT_LIST.reverse();
        }

        int measure = 1000;
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            LONG_INT_LIST.reverse();
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
    }

    private static void benchmarkToArray() {
        int warmup = 10000;
        for (int i = 0; i < warmup; i++) {
            LONG_INT_LIST.toArray(Integer[]::new);
        }

        int measure = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            LONG_INT_LIST.toArray(Integer[]::new);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
    }

    private static void benchmarkPartition() {
        int warmup = 10000;
        for (int i = 0; i < warmup; i++) {
            LONG_LIST.partition(a -> a % 37 == a % 11);
        }

        int measure = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            LONG_LIST.partition(a -> a % 37 == a % 11);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
    }
}
