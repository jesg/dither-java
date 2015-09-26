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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class IPOGTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = DitherError.class)
    public void tMustBeGreaterThan2() {
        Dither.ipog(0, new Object[][] {});
    }

    @Test(expected = DitherError.class)
    public void tMustBeGreaterThanParamLength() {
        Dither.ipog(3, new Object[][] { new Object[] {} });
    }

    @Test(expected = DitherError.class)
    public void paramsLengthMustBeGreaterThan2() {
        Dither.ipog(new Object[][] { new Object[] {}, new Object[] {} });
    }

    @Test
    public void canGetAllCombinations() {
        IPOG ipog = new IPOG(new Object[][] { new Object[] { 1, 2 },
                new Object[] { 3, 4 } }, 2);
        List<int[]> testCases = ipog.allCombinations();
        assertArrayEquals(testCases.get(0), new int[]{0, 0});
        assertArrayEquals(testCases.get(1), new int[]{0, 1});
    }

    @Test
    public void canPerfectMerge() {
        IPOG ipog = new IPOG(new Object[][] {
            new Object[] { 1, 2 },
            new Object[] { 3, 4 },
            new Object[] { 1, 2, 3}}, 2);
        int actual = ipog.merge(3, new Pair[]{new Pair(1, 0), new Pair(2, 0)}, new int[]{0, 0, -1});
        assertTrue(actual == 0);
    }

    @Test
    public void canPartialMerge() {
        IPOG ipog = new IPOG(new Object[][] {
            new Object[] { 1, 2 },
            new Object[] { 3, 4 },
            new Object[] { 1, 2, 3}}, 2);
        int actual = ipog.merge(3, new Pair[]{new Pair(1, 0), new Pair(2, 0)}, new int[]{-1, -1, -1});
        assertTrue(actual == 1);
    }

    @Test
    public void canDetectConstraintInMerge() {
        IPOG ipog = new IPOG(new Object[][] {
            new Object[] { 1, 2 },
            new Object[] { 3, 4 },
            new Object[] { 1, 2, 3}}, 2, new Integer[][]{new Integer[] { 0, 0, 0}}, new Object[][]{});
        int[] test = new int[]{0,0,-1};
        int actual = ipog.merge(3, new Pair[]{new Pair(1, 0), new Pair(2, 0)}, test);
        assertTrue(actual == -1);
    }

    @Test
    public void canGen3WayCases() {
        Object[][] results = Dither.ipog(3, new Object[][] { new Object[] { 1, 2 },
                new Object[] { "1", "2" },
                new Object[] { 1.0, 2.0 }, new Object[] { true, false, 3 } });

        assertTrue(results.length == 12);
    }

    @Test
    public void canCompute3WayIPOGWithConstraintsAndPreviouslyTested() {
        Object[][] results = Dither.ipog(3, new Object[][] { new Object[] { 0, 1 },
                new Object[] { 0, 1 },
                new Object[] { 0, 1, 2, 3 }},
                new Integer[][]{ new Integer[]{0, null, 2}, new Integer[]{0, 1, 0}},
                new Object[][]{new Object[]{0, 0, 0}}
        );

        Set<List<Object>> actuals = new HashSet<List<Object>>();
        for(Object[] result : results) {
            actuals.add(Arrays.asList(result));
        }

        List<List<Integer>> expected = Arrays.asList(
                Arrays.asList(1, 0, 0),
                Arrays.asList(1, 1, 0),
                Arrays.asList(0, 0, 1),
                Arrays.asList(1, 0, 1),
                Arrays.asList(0, 1, 1),
                Arrays.asList(1, 1, 1),
                Arrays.asList(1, 0, 2),
                Arrays.asList(1, 1, 2),
                Arrays.asList(0, 0, 3),
                Arrays.asList(1, 0, 3),
                Arrays.asList(0, 1, 3),
                Arrays.asList(1, 1, 3));

        for(List expectedResult : expected) {
            assertTrue("expected " + expectedResult, actuals.contains(expectedResult));
        }
    }

    @Test
    public void anotherCompute3WayIPOGWithConstraints() {
        System.out.println("begin");
        Object[][] results = Dither.ipog(3, new Object[][] { new Object[] { 0, 1 },
                new Object[] { 0, 1 },
                new Object[] { 0, 1 },
                new Object[] { 0, 1, 2, 3 }},
                new Integer[][]{ new Integer[]{0, 1, 0}});

        Set<List<Object>> actuals = new HashSet<List<Object>>();
        for(Object[] result : results) {
            actuals.add(Arrays.asList(result));
        }

        List<List<Integer>> expected = Arrays.asList(
                Arrays.asList(0, 0, 0, 0),
                Arrays.asList(1, 1, 0, 0),
                Arrays.asList(1, 0, 1, 0),
                Arrays.asList(0, 1, 1, 0),
                Arrays.asList(1, 0, 0, 1),
                Arrays.asList(1, 1, 0, 1),
                Arrays.asList(0, 0, 1, 1),
                Arrays.asList(1, 1, 1, 1),
                Arrays.asList(0, 0, 0, 2),
                Arrays.asList(1, 1, 0, 2),
                Arrays.asList(1, 0, 1, 2),
                Arrays.asList(0, 1, 1, 2),
                Arrays.asList(0, 0, 0, 3),
                Arrays.asList(1, 1, 0, 3),
                Arrays.asList(1, 0, 1, 3),
                Arrays.asList(0, 1, 1, 3),
                Arrays.asList(0, 0, 0, 1),
                Arrays.asList(0, 1, 1, 1));

        for(List expectedResult : expected) {
            assertTrue("expected " + expectedResult, actuals.contains(expectedResult));
        }
    }
}
