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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class IPOG {

    private final UnboundParam[] unboundParams;
    private final int t;
    private final BoundParam[][] boundParams;
    private final Object[][] inputParams;
    private final Map<Integer, Integer> origIndex = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> inverseOrigIndex = new HashMap<Integer, Integer>();
    private final List<TestCase> constraints;

    public IPOG(final Object[][] input, final int t, final Integer[][] constraints) {
        this.t = t;
        this.inputParams = input;

        // sort input params and create a shared object pool
        final IndexArrayPair[] tmp = new IndexArrayPair[input.length];
        for (int k = 0; k < tmp.length; k++) {
            tmp[k] = new IndexArrayPair(k, input[k]);
        }
        Arrays.sort(tmp, new ArrayLengthComparator());
        this.unboundParams = new UnboundParam[tmp.length];
        this.boundParams = new BoundParam[tmp.length][];
        for (int k = 0; k < tmp.length; k++) {
            this.origIndex.put(k, tmp[k].getI());
            this.inverseOrigIndex.put(tmp[k].getI(), k);
            this.unboundParams[k] = new UnboundParam(k);
            this.boundParams[k] = new BoundParam[tmp[k].getArr().length];

            for (int h = 0; h < this.boundParams[k].length; h++) {
                this.boundParams[k][h] = new BoundParam(k, h);
            }
        }
        
        // setup constraints
        this.constraints = new ArrayList<TestCase>(constraints.length);
        for (final Integer[] constraint : constraints ) {
            final TestCase testCase = new TestCase(unboundParams, boundParams);
            for(int k = 0; k < constraint.length; k++ ) {
                if(constraint[k] != null) {
                    testCase.add(boundParams[inverseOrigIndex.get(k)][constraint[k]]);
                }
            }
        }

        // validate input
        if (t <= 1) {
            throw new DitherError("t must be >= 2");
        }

        if (t > input.length) {
            throw new DitherError("t must be <= params.length");
        }
        for (final Object[] param : input) {
            if (param.length < 2) {
                throw new DitherError("param length must be > 1");
            }
        }
    }

    List<TestCase> allCombinations() {

        final int[] prodarr = new int[t];
        for (int k = 0; k < prodarr.length; k++) {
            prodarr[k] = boundParams[k].length;
        }

        final int[][] prodArrResult = CombinatoricHelper.product(prodarr);
        final List<TestCase> testCases = new ArrayList<TestCase>(
                prodArrResult.length);
        for (final int[] innerArr : prodArrResult) {
            final TestCase testCase = new TestCase(unboundParams, boundParams,
                    Collections.EMPTY_LIST);
            for (int i = 0; i < innerArr.length; i++) {
                testCase.add(boundParams[i][innerArr[i]]);
            }
            testCases.add(testCase);
        }

        return testCases;
    }

    Collection<TestCase> combinations(final int i) {
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

        final Collection<TestCase> testCases = new HashSet<TestCase>(
                combt.size());
        for (final int[] comb : combt) {

            final int[] prodarr = new int[comb.length];
            for (int k = 0; k < prodarr.length; k++) {
                prodarr[k] = boundParams[comb[k]].length;
            }

            final int[][] prodArrResult = CombinatoricHelper.product(prodarr);

            for (final int[] innerArr : prodArrResult) {
                final TestCase testCase = new TestCase(unboundParams,
                        boundParams, Collections.EMPTY_LIST);
                for (int k = 0; k < innerArr.length; k++) {
                    testCase.add(boundParams[comb[k]][innerArr[k]]);
                }
                testCases.add(testCase);
            }
        }

        return testCases;
    }

    Object[][] run() {
        final List<TestCase> testSet = allCombinations();
        Collection<BoundParam> miss = Arrays.asList(new BoundParam(3, 0),
                new BoundParam(2, 0));
        for (int k = t; k < boundParams.length; k++) {
            final Collection<TestCase> pi = combinations(k);

            // horizontal extension for parameter i
            final Collection<TestCase> toDelete = new ArrayList<TestCase>();
            for (final TestCase testCase : testSet) {
                final Collection<TestCase> cover = maximizeCoverage(k,
                        testCase, pi);

                if (cover == null) {
                    toDelete.add(testCase);
                } else {
                    pi.removeAll(cover);
                }
            }
            testSet.removeAll(toDelete);

            // vertical extension for parameter i
            while (!pi.isEmpty()) {
                final TestCase testCase = pi.iterator().next();
                boolean isCaseCovered = false;
                for (final TestCase innerTestCase : testSet) {
                    if (innerTestCase.containsAll(testCase)) {
                        isCaseCovered = true;
                        break;
                    }
                }

                if (isCaseCovered) {
                    pi.remove(testCase);
                    continue;
                }

                boolean isMerged = false;
                for (final TestCase innerTestCase : testSet) {
                    if (innerTestCase.mergeWithoutConflict(k, testCase) != null) {
                        isMerged = true;
                        break;
                    }
                }

                if (!isMerged) {
                    testSet.add(testCase.createUnbound(k));
                }
                pi.remove(testCase);
            }
        }
        return testSetToArray(testSet);
    }

    private Object[][] testSetToArray(final List<TestCase> testSet) {
        final Object[][] results = new Object[testSet.size()][];
        int size = 0;
        for (int k = 0; k < testSet.size(); k++) {
            final Object[] result = fillUnbound(testSet.get(k));
            if (result == null) {
                continue;
            }
            results[size] = result;
            size++;
        }
        return Arrays.copyOf(results, size);
    }

    private Object[] fillUnbound(final TestCase testCase) {
        final Object[] result = new Object[boundParams.length];

        TestCase innerTestCase = testCase;
        if (testCase.size() != result.length) {
            innerTestCase = testCase.createUnbound(result.length - 1);
        }

        for (final Param param : innerTestCase) {
            final int i = origIndex.get(param.i());
            if (param.isBound()) {
                result[i] = inputParams[i][((BoundParam) param).j()];
            } else {
                result[i] = inputParams[i][0];
            }
        }

        return result;
    }

    private Collection<TestCase> maximizeCoverage(final int i,
            final TestCase testCase, final Collection<TestCase> pi) {
        int currentMax = 0;
        int currentJ = 0;
        Collection<TestCase> currentMatches = Collections.emptyList();

        for (int j = 0; j < boundParams[i].length; j++) {
            final BoundParam currentParam = boundParams[i][j];
            testCase.add(currentParam);

            final List<TestCase> matches = new ArrayList<TestCase>();
            for (final TestCase innerTestCase : pi) {
                if (testCase.containsAll(innerTestCase)) {
                    matches.add(innerTestCase);
                }
            }
            final int count = matches.size();

            if (count > currentMax) {
                currentMax = count;
                currentJ = j;
                currentMatches = matches;
            }
            testCase.remove(currentParam);
        }

        testCase.add(boundParams[i][currentJ]);
        return currentMatches;
    }
}
