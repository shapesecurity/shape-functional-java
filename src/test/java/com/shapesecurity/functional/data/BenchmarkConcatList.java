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

public class BenchmarkConcatList {

    public static final int SIZE = 10000;
    public static final int WARMUP = 50000;
    public static final int MEASURE = 1000;

    public static void main(String[] args) {
        {
            System.out.println("Balanced tree");
            ConcatList<Integer> l = gen(SIZE);
            benchmarkAll(l);
        }
        System.out.println("===============");
        {
            System.out.println("Left-leaning tree");
            ConcatList<Integer> l = genLL(SIZE);
            benchmarkAll(l);
        }
        System.out.println("===============");
        {
            System.out.println("Right-leaning tree");
            ConcatList<Integer> l = genRL(SIZE);
            benchmarkAll(l);
        }
        System.out.println("===============");
        {
            System.out.println("Random-leaning tree");
            ConcatList<Integer> l = genRandom(SIZE);
            benchmarkAll(l);
        }
    }

    private static ConcatList<Integer> gen(int size) {
        return gen(0, size);
    }

    private static ConcatList<Integer> gen(int start, int size) {
        if (size == 0) {
            return ConcatList.empty();
        } else if (size == 1) {
            return ConcatList.single(start);
        } else {
            int half = size >> 1;
            return gen(start, half).append(gen(start + half, size - half));
        }
    }

    private static ConcatList<Integer> genLL(int size) {
        ConcatList<Integer> acc = ConcatList.empty();
        for (int i = 0; i < size; i++) {
            acc = ConcatList.single(size - 1 - i).append(acc);
        }
        return acc;
    }

    private static ConcatList<Integer> genRL(int size) {
        ConcatList<Integer> acc = ConcatList.empty();
        for (int i = 0; i < size; i++) {
            acc = acc.append1(i);
        }
        return acc;
    }

    private static ConcatList<Integer> genRandom(int size) {
        return genRandom(0, size);
    }

    private static ConcatList<Integer> genRandom(int start, int size) {
        if (size == 0) {
            return ConcatList.empty();
        } else if (size == 1) {
            return ConcatList.single(start);
        } else {
            int half = (int) Math.floor(Math.random() * (size - 1)) + 1;
            return gen(start, half).append(gen(start + half, size - half));
        }
    }

    private static void benchmarkAll(ConcatList<Integer> l) {
        System.out.print("benchmarkForeachIndexUpdate: ");
        benchmarkForeachIndexUpdate(l);
        System.out.print("benchmarkForeach: ");
        benchmarkForeach(l);
        System.out.print("benchmarkForLoop: ");
        benchmarkForLoop(l);
        System.out.print("benchmarkFind: ");
        benchmarkFind(l);
        System.out.print("benchmarkToList: ");
        benchmarkToList(l);
    }

    private static void benchmarkForeachIndexUpdate(ConcatList<Integer> l) {
        for (int i = 0; i < WARMUP; i++) {
            l.update(i % SIZE, i);
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE; i++) {
            l.update(i, i);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / MEASURE);
    }

    private static void benchmarkForeach(ConcatList<Integer> l) {
        int[] acc = new int[1];
        for (int i = 0; i < WARMUP; i++) {
            l.forEach(n -> acc[0] += (n % 137 << 7) ^ 12);
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE; i++) {
            l.forEach(n -> acc[0] += (n % 137 << 7) ^ 12);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / MEASURE);
    }

    private static void benchmarkForLoop(ConcatList<Integer> l) {
        int[] acc = new int[1];
        for (int i = 0; i < WARMUP; i++) {
            for (int n : l) {
                acc[0] += (n % 137 << 7) ^ 12;
            }
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE; i++) {
            for (int n : l) {
                acc[0] += (n % 137 << 7) ^ 12;
            }
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / MEASURE);
    }


    private static void benchmarkFind(ConcatList<Integer> l) {
        for (int i = 0; i < WARMUP; i++) {
            l.find(n -> n == -1);
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE; i++) {
            l.find(n -> n == -1);
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / MEASURE);
    }

    private static void benchmarkToList(ConcatList<Integer> l) {
        for (int i = 0; i < WARMUP; i++) {
            l.toList();
        }

        long start = System.nanoTime();
        for (int i = 0; i < MEASURE; i++) {
            l.toList();
        }
        long elapse = System.nanoTime() - start;
        System.out.printf("%.3fµs\n", elapse * 1e-3 / MEASURE);
    }

}
