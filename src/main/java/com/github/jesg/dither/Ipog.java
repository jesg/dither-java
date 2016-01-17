package com.github.jesg.dither;

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
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

class Ipog {

    private final int t;
    private final Object[][] inputParams;
    private final Map<Integer, Integer> origIndex = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> inverseOrigIndex = new HashMap<Integer, Integer>();
    private final Pair[][] constraints;
    private final int[][] previouslyTested;
    private final Pair[][] pairCache;
    private final int[] mergeScratch;
    private final ConstraintHandler constraintHandler;

    public Ipog(final Object[][] input, final int t) {
        this(input, t, new Integer[][] {}, new Object[][] {});
    }

    public Ipog(final Object[][] input, final int t,
            final Integer[][] constraints, final Object[][] tested) {
        this.t = t;
        this.inputParams = input;
        this.mergeScratch = new int[input.length];
        this.previouslyTested = new int[tested.length][input.length];

        // sort input params and create a shared object pool
        final IndexArrayPair[] tmp = new IndexArrayPair[input.length];
        for (int k = 0; k < tmp.length; k++) {
            tmp[k] = new IndexArrayPair(k, input[k]);
        }
        Arrays.sort(tmp, new ArrayLengthComparator());
        final Map<Integer, Map<Object, Pair>> origParamMap = new HashMap<Integer, Map<Object, Pair>>();
        this.pairCache = new Pair[tmp.length][];
        for (int k = 0; k < tmp.length; k++) {
            this.origIndex.put(k, tmp[k].getI());
            this.inverseOrigIndex.put(tmp[k].getI(), k);
            this.pairCache[k] = new Pair[tmp[k].getArr().length];

            origParamMap.put(tmp[k].getI(),
                    new HashMap<Object, Pair>());
            for (int h = 0; h < this.pairCache[k].length; h++) {
                this.pairCache[k][h] = new Pair(k, h);
                origParamMap.get(tmp[k].getI()).put(tmp[k].getArr()[h],
                        this.pairCache[k][h]);
            }
        }

        for (int j = 0; j < previouslyTested.length; j++) {
            final Object[] innerTestCase = tested[j];
            for (int k = 0; k < innerTestCase.length; k++) {
                previouslyTested[j][k] = origParamMap.get(k).get(innerTestCase[k]).j;
            }
        }

        // setup constraints
        this.constraints = new Pair[constraints.length][];
        for (int i = 0; i < constraints.length; i++) {
            final Integer[] constraint = constraints[i];
            final List<Pair> tmpConstraint = new ArrayList<Pair>(constraint.length);

            for (int k = 0; k < constraint.length; k++) {
                if (constraint[k] != null) {
                    tmpConstraint.add(pairCache[inverseOrigIndex.get(k)][constraint[k]]);
                }
            }
            this.constraints[i] = tmpConstraint.toArray(new Pair[]{});
        }
        final int[] bounds = new int[pairCache.length];
        for(int i = 0; i < bounds.length; i++) {
            bounds[i] = pairCache[i].length;
        }
        this.constraintHandler = new ConstraintHandler(this.constraints, bounds);
    }

    List<int[]> allCombinations() {

        final int[] prodarr = new int[t];
        for (int k = 0; k < prodarr.length; k++) {
            prodarr[k] = pairCache[k].length;
        }

        final int[][] prodArrResult = CombinatoricHelper.product(prodarr);

        final List<int[]> results = new LinkedList<int[]>();
        for(int i = 0; i < prodArrResult.length; i++) {
            final int[] result = Arrays.copyOf(prodArrResult[i], pairCache.length);
            Arrays.fill(result, t, result.length, -1);
            if(!hasTested(t, result)) {
                results.add(result);
            }
        }
        return results;
    }

    List<Pair[]> combinations(final int i) {
        final int[] innerParams = new int[i];
        for (int k = 0; k < innerParams.length; k++) {
            innerParams[k] = k;
        }

        final List<int[]> combinations = CombinatoricHelper.getCombinations(
                t - 1, innerParams);
        final List<int[]> combt = new ArrayList<int[]>(combinations.size());
        for (int k = 0; k < combinations.size(); k++) {
            final int[] newArr = Arrays.copyOf(combinations.get(k), t);
            newArr[t - 1] = i;
            combt.add(newArr);
        }

        final List<Pair[]> results = new LinkedList<Pair[]>();
        for (final int[] comb : combt) {

            final int[] prodarr = new int[comb.length];
            for (int k = 0; k < prodarr.length; k++) {
                prodarr[k] = pairCache[comb[k]].length;
            }

            final int[][] prodArrResult = CombinatoricHelper.product(prodarr);

            for (int j = 0; j < prodArrResult.length; j++) {
                final int[] innerArr = prodArrResult[j];
                final Pair[] pairs = new Pair[innerArr.length];
                for (int k = 0; k < innerArr.length; k++) {
                    pairs[k] = pairCache[comb[k]][innerArr[k]];
                }
                if (!hasTested(pairs)) {
                    results.add(pairs);
                }
            }
        }

        return results;
    }

    private boolean hasTested(final int[] testCase) {
        for (final int[] innerCase : previouslyTested) {
            if(Arrays.equals(testCase, innerCase)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTested(final int t, final int[] testCase) {
outer:
        for (final int[] innerCase : previouslyTested) {
            for(int i = 0; i < t; i++) {
                if(innerCase[i] != testCase[i]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private boolean hasTested(final Pair[] testCase) {
outer:
        for (final int[] innerCase : previouslyTested) {
            for(final Pair pair : testCase) {
                if(innerCase[pair.i] != pair.j) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    Object[][] run() {
        final List<int[]> testSet = allCombinations();
        final List<int[]> unbound = new LinkedList<int[]>();
        for (int k = t; k < pairCache.length; k++) {
            final List<Pair[]> pi = combinations(k);

            // horizontal extension for parameter i
            final ListIterator<int[]> iterTestSet = testSet.listIterator();
            while(iterTestSet.hasNext()) {
                final int[] testCase = iterTestSet.next();
                final int max = maximizeCoverage(k,
                        testCase, pi);
                if(max < 0) {
                    iterTestSet.remove();
                } else {
                    // remove matches
                    final ListIterator<Pair[]> iter = pi.listIterator();
iter:
                    while(iter.hasNext()) {
                        for(final Pair pair : iter.next()) {
                            if(testCase[pair.i] != pair.j) {
                                continue iter;
                            }
                        }
                        iter.remove();
                    }
                }
            }

            // vertical extension for parameter i
            final ListIterator<Pair[]> piIter = pi.listIterator();
            while (piIter.hasNext()) {
                final Pair[] testCase = piIter.next();
                // remove constraint violation
                boolean isCaseCovered = violateConstraints(testCase);
                if(!isCaseCovered) {

match_unbound_label:
                    for (final int[] innerTestCase : unbound) {
                        for(final Pair pair : testCase) {
                            if(innerTestCase[pair.i] != pair.j) {
                                continue match_unbound_label;
                            }
                        }
                        isCaseCovered = true;
                        break;
                    }
                }

                if (!isCaseCovered) {
                    boolean isMerged = false;
                    for(final int[] innerTestCase : unbound) {
                        // -1 => no merge, 0 perfect merge (no unbound), 1 partial merge
                        final int mergeResult = merge(k, testCase, innerTestCase);
                        if(mergeResult > 0){
                            for(final Pair localComb : testCase) {
                                innerTestCase[localComb.i] = localComb.j;
                            }
                            isMerged = true;
                            break;

                        }
                    }

                    if (!isMerged) {
                        final int[] unboundTestCase = new int[pairCache.length];
                        Arrays.fill(unboundTestCase, -1);
                        for(final Pair pair : testCase) {
                            unboundTestCase[pair.i] = pair.j;
                        }
                        if(!violateConstraints(unboundTestCase)) {
                            unbound.add(unboundTestCase);
                        }
                    }
                }
                piIter.remove();
            }
        }
        return testSetToArray(testSet, unbound);
    }

    // -1 no merge, 0 perfect merge (no unbound), 1 partial merge
    public int merge(final int k, final Pair[] pairs, final int[] testCase) {
        // verify merge
        for(final Pair pair : pairs) {
            int value = testCase[pair.i];
            if(!(value == -1 || value == pair.j)) {
                return -1;
            }
        }

        for(int i = 0; i < mergeScratch.length; i++) {
            mergeScratch[i] = testCase[i];
        }

        for(final Pair pair : pairs) {
            mergeScratch[pair.i] = pair.j;
        }

        if(violateConstraints(mergeScratch)) {
            return -1;
        }

        // find unbound
        for(int i = 0; i < k; i++) {
            if(mergeScratch[i] == -1) { return 1; }
        }
        return 0;
    }

    private Object[][] testSetToArray(final List<int[]> testSet, final List<int[]> unbound) {

        final List<Object[]> results = new ArrayList<Object[]>(testSet.size() + unbound.size());
        for(final int[] boundResult : testSet) {
            final Object[] result = new Object[boundResult.length];
            for (int k = 0; k < boundResult.length; k++) {
                final int value = boundResult[k];
                final int i = origIndex.get(k);
                result[i] = inputParams[i][value];
            }
            if(!violateConstraints(boundResult) && !hasTested(boundResult)) {
                results.add(result);
            }
        }

outer:
        for(final int[] unboundResult : unbound) {
            final Object[] result = new Object[unboundResult.length];
            final int[] groundResult = constraintHandler.groundSolution(unboundResult);
            if(groundResult == null) {
                continue outer;
            }
            for(int k = 0; k < unboundResult.length; k++) {
                final int i = origIndex.get(k);
                final int value = groundResult[k];
                result[i] = inputParams[i][value];
            }
            if(!hasTested(unboundResult)) {
                results.add(result);
            }
        }

        return results.toArray(new Object[][]{});
    }

    boolean violateConstraints(final int[] testCase) {
        if(constraints.length == 0) {
            return false;
        }
        return constraintHandler.violateConstraints(testCase);
    }

    boolean violateConstraints(final Pair[] testCase) {
        if(constraints.length == 0) {
            return false;
        }
        return constraintHandler.violateConstraints(testCase);
    }

    private int maximizeCoverage(final int i,
            final int[] testCase, final List<Pair[]> pi) {
        int currentMax = -1;
        int currentJ = 0;

        for (int j = 0; j < pairCache[i].length; j++) {
            final Pair currentPair = pairCache[i][j];
            testCase[currentPair.i] = currentPair.j;

            if (!violateConstraints(testCase)) {
                int count = 0;
outer:
                for (final Pair[] innerTestCase : pi) {
                    for(final Pair pair : innerTestCase) {
                        if (testCase[pair.i] != pair.j) {
                            continue outer;
                        }
                    }
                    count++;
                }

                if (count > currentMax) {
                    currentMax = count;
                    currentJ = j;
                }
            }
            testCase[i] = -1;
        }

        if (currentMax == -1) {
            return -1;
        }

        final Pair pair = pairCache[i][currentJ];
        testCase[pair.i] = pair.j;
        return currentMax;
    }
}
