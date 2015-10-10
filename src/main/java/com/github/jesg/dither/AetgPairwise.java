package com.github.jesg.dither;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/*
 * #%L
 * dither
 * %%
 * Copyright (C) 2015 Jason Gowan
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 True*/

class AetgPairwise extends Aetg {

    private final int n = 50;
    private final int[][] scratch = new int[n][];
    private final int[] fitness = new int[n];
    private final Object[][] params;
    private final Pair[][] pairCache;
    private final List<Pair[]> coverage;
    private final AtegRandom random;
    private final ExecutorService executor;
    private final Semaphore barrier;
    private final List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(n);
    private final Pair[][] constraints;

    public AetgPairwise(final int t, final Integer seed, final Object[][] params, final Integer[][] constraintsParam, final Object[][] previouslyTested, final ExecutorService executor) {
        this.params = params;
        this.executor = executor;
        this.barrier = new Semaphore(0);

        for(int i = 0; i < n; i++) {
            final int index = i;
            final Semaphore localBarrier = this.barrier;
            tasks.add(new Callable<Void>() {
                public Void call() {
                    try {
                        generate(index);
                        fitness(index);
                    } finally {
                        localBarrier.release();
                    }
                    return null;
                }
            });
        }

        if(seed == null) {
            this.random = new AtegRandom() {
                public int nextInt(final int i) { return ThreadLocalRandom.current().nextInt(0, i); }
            };
        } else {
            this.random = new AtegRandom() {
                final Random random = new Random(seed);
                public int nextInt(final int i) { return random.nextInt(i); }
            };
        }

        this.pairCache = new Pair[params.length][];
        for(int i = 0; i < params.length; i++) {
            this.pairCache[i] = new Pair[params[i].length];

            for(int j = 0; j < params[i].length; j++) {
                this.pairCache[i][j] = new Pair(i, j);
            }
        }

        this.constraints = new Pair[constraintsParam.length + previouslyTested.length][];
        int constraintIndex = 0;
        for(final Integer[] constraint : constraintsParam) {
            final List<Pair> pairs = new ArrayList<Pair>();
            for(int i = 0; i < constraint.length; i++) {
                if(constraint[i] == null) {
                    continue;
                }
                pairs.add(pairCache[i][constraint[i]]);
            }
            constraints[constraintIndex++] = pairs.toArray(new Pair[]{});
        }

        for(final Object[] testCase : previouslyTested) {
            final Pair[] pairs = new Pair[testCase.length];
            for(int i = 0; i < pairs.length; i++) {
                final Object currentObject = testCase[i];
                for(int j = 0; j < params[i].length; j++) {
                    if(currentObject.equals(params[i][j])) {
                        pairs[i] = pairCache[i][j];
                        break;
                    }
                }
            }
            constraints[constraintIndex++] = pairs;
        }


        for(int u = 0; u < n; u++) {
            scratch[u] = new int[params.length];
        }

        final int[] arr = new int[params.length];
        for(int k = 0; k < params.length; k++) {
            arr[k] = k;
        }

        this.coverage = new LinkedList<Pair[]>();
        final int[] lengths = new int[t];
        for(final int[] tmp : CombinatoricHelper.getCombinations(t, arr)) {

            for(int i = 0; i < lengths.length; i++) {
                lengths[i] = params[tmp[i]].length;
            }

            final Pair[][] pairs = new Pair[tmp.length][];
            for(int i = 0; i < pairs.length; i++) {
                pairs[i] = this.pairCache[tmp[i]];
            }

outer:
            for(final List<Pair> innerPairs : CombinatoricHelper.product(pairs)) {
                final Pair[] innerComb = innerPairs.toArray(new Pair[innerPairs.size()]);

                for(final Pair[] constraint : constraints) {
                    int count = 0;
                    for(final Pair pair : constraint) {
                        for(final Pair innerCombPair : innerComb) {
                            if(pair.equals(innerCombPair)) {
                                count++;
                                break;
                            }
                        }
                    }
                    if(count == constraint.length) {
                        continue outer;
                    }
                }
                coverage.add(innerComb);
            }
        }
    }


    public boolean hasNext() {
        return !coverage.isEmpty();
    }

    public Object[] next() {
        try {
            executor.invokeAll(tasks);
            barrier.acquire(n);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        final int i = bestFit();
        if(fitness[i] <= 0) {
            return null;
        }
        final int[] best = scratch[i];
        final Object[] result = new Object[params.length];
        for(int k = 0; k < result.length; k++) {
            result[k] = params[k][best[k]];
        }
        removeMatches(i);
        return result;
    }

    private void removeMatches(final int i) {
        final int[] cases = scratch[i];
        final ListIterator<Pair[]> iter = coverage.listIterator(0);
        while(iter.hasNext()) {
            if(match(iter.next(), cases)) {
                iter.remove();
            }
        }
    }

    private void generate(final int i) {
        for(int j = 0; j < params.length; j++) {
            scratch[i][j] = random.nextInt(params[j].length);
        }
    }

    private void fitness(final int i) {
        final int[] cases = scratch[i];
        for(final Pair[] constraint : constraints) {
            int count = 0;
            for(final Pair pair : constraint) {
                if(pair.j == cases[pair.i]) {
                    count++;
                }
            }
            if(count == constraint.length) {
                fitness[i] = -1;
                return;
            }
        }
        int count = 0;
        for(final Pair[] pairs : coverage) {
            if(match(pairs, cases)) {
                count++;
            }
        }
        fitness[i] = count;
    }

    private int bestFit() {
        int index = 0;
        int max = 0;
        for(int i = 0; i < fitness.length; i++) {
            final int value = fitness[i];
            if(value > max) {
                max = value;
                index = i;
            }
        }
        return index;
    }

    private interface AtegRandom {
        int nextInt(int a);
    }

    private static boolean match(final Pair[] pairs, final int[] cases) {
        boolean flag = true;
        for(final Pair pair : pairs) {
            if(cases[pair.i] != pair.j) {
                flag = false;
                break;
            }
        }
        return flag;
    }
}
