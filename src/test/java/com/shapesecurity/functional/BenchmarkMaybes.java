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

package com.shapesecurity.functional;

import com.shapesecurity.functional.data.Maybe;

public class BenchmarkMaybes {
  public static final int SIZE = 10000;
  private static int total = 0;
  private static int warmup = 5000000;
  private static int measure = 10000;

  public static void main(String[] args) {
    System.out.print("Maybes: ");
    for (int i = 0; i < 30; i++) {
      benchmarkMaybes();
    }

    System.out.print("Naked: ");
    for (int i = 0; i < 30; i++) {
      benchmarkNaked();
    }
  }

  private static void benchmarkMaybes() {
    for (int j = 0; j < warmup; j++) {
      total += Maybe
          .of(j)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .fromJust();
    }
    System.gc();
    long start = System.nanoTime();
    for (int j = 0; j < measure; j++) {
      total += Maybe
          .of(j)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .map(i -> i % 2 == 0 ? i / 2 : i * 3 + 1)
          .fromJust();
    }
    System.gc();
    long elapse = System.nanoTime() - start;
    System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
  }


  private static void benchmarkNaked() {
    for (int j = 0; j < warmup; j++) {
      Integer i = j;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      total += i;
    }
    System.gc();
    long start = System.nanoTime();
    for (int j = 0; j < measure; j++) {
      Integer i = j;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      i = i % 2 == 0 ? i / 2 : i * 3 + 1;
      total += i;
    }
    System.gc();
    long elapse = System.nanoTime() - start;
    System.out.printf("%.3fµs\n", elapse * 1e-3 / measure);
  }
}
