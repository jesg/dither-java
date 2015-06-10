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
    private final Map<Integer, Map<Object, BoundParam>> origParamMap = new HashMap<Integer, Map<Object, BoundParam>>();
    private final TestCase[] constraints;
    private final Collection<Collection<BoundParam>> tested;

    public IPOG(final Object[][] input, final int t) {
        this(input, t, new Integer[][] {}, new Object[][] {});
    }

    public IPOG(final Object[][] input, final int t,
            final Integer[][] constraints, final Object[][] tested) {
        this.t = t;
        this.inputParams = input;
        this.tested = new ArrayList<Collection<BoundParam>>(tested.length);

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

            this.origParamMap.put(tmp[k].getI(),
                    new HashMap<Object, BoundParam>());
            for (int h = 0; h < this.boundParams[k].length; h++) {
                this.boundParams[k][h] = new BoundParam(k, h);
                this.origParamMap.get(tmp[k].getI()).put(tmp[k].getArr()[h],
                        this.boundParams[k][h]);
            }
        }

        for (final Object[] innerTestCase : tested) {
            final Collection<BoundParam> newCase = new ArrayList<BoundParam>(
                    innerTestCase.length);
            for (int k = 0; k < innerTestCase.length; k++) {
                newCase.add(this.origParamMap.get(k).get(innerTestCase[k]));
            }
            this.tested.add(newCase);
        }

        // setup constraints
        this.constraints = new TestCase[constraints.length];
        for (int i = 0; i < constraints.length; i++) {
            final Integer[] constraint = constraints[i];
            final TestCase testCase = new TestCase(unboundParams, boundParams);
            this.constraints[i] = testCase;

            for (int k = 0; k < constraint.length; k++) {
                if (constraint[k] != null) {
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
                    constraints);
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
                        boundParams, constraints);
                for (int k = 0; k < innerArr.length; k++) {
                    testCase.add(boundParams[comb[k]][innerArr[k]]);
                }
                if (!hasTested(testCase)) {
                    testCases.add(testCase);
                }
            }
        }

        return testCases;
    }

    private boolean hasTested(final TestCase testCase) {
        boolean result = false;
        for (final Collection<BoundParam> innerCase : tested) {
            if (testCase.containsAll(innerCase)) {
                result = true;
                break;
            }
        }
        return result;
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
        for (final Param param : testCase) {
            if (param.isBound()) {
                final int i = origIndex.get(param.i());
                result[i] = inputParams[i][((BoundParam) param).j()];
            }
        }

        TestCase innerTestCase = testCase;

        for (int k = 0; k < result.length; k++) {
            if (result[k] != null) {
                continue;
            }

            final Object[] origParams = inputParams[k];
            for (int h = 0; h < origParams.length; h++) {
                final BoundParam innerParam = boundParams[inverseOrigIndex
                        .get(k)][h];
                testCase.add(innerParam);
                if (testCase.hasAnyConstraint()) {
                    testCase.remove(innerParam);
                    continue;
                } else {
                    result[k] = origParams[h];
                    break;
                }
            }
            if (result[k] == null) {
                return null;
            }
        }

        if (innerTestCase.hasAnyConstraint() || hasTested(innerTestCase)) {
            return null;
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

            if (!testCase.hasAnyConstraint()) {
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
            }
            testCase.remove(currentParam);
        }

        if (testCase.hasAnyConstraint()) {
            return null;
        }

        testCase.add(boundParams[i][currentJ]);
        return currentMatches;
    }
}
