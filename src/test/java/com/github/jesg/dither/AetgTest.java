package com.github.jesg.dither;

/*
 * #%L
 * com.github.jesg:dither
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class AetgTest {

    @Test(expected = DitherError.class)
    public void tMustBeGreaterThan2() {
        Dither.aetg(0, new Object[][]{});
    }

    @Test(expected = DitherError.class)
    public void tMustBeGreaterThanParamLength() {
        Dither.aetg(3, new Object[][] { new Object[] {} });
    }

    @Test(expected = DitherError.class)
    public void paramsLengthMustBeGreaterThan2() {
        Dither.aetg(2, new Object[][] { new Object[] {}, new Object[] {} });
    }

    @Test
    public void canRunAteg() {
        Object[][] result = Dither.aetg(
                new Object[][] {
                    new Object[] { 1, 2 },
                    new Object[] { 3, 4 }
                });
        assertTrue(result.length == 4);
    }

    @Test
    public void canRunAteg2() {
        Object[][] result = Dither.aetg(3,
                new Object[][] {
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2, 3 },
                    new Object[] { 1, 2, 3 },
                    new Object[] { 1, 2, 3, 4 },
                    new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
                    new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                });
        assertTrue(result.length > 100);
    }

    @Test
    public void canCompute3WayAtegWithConstraintsAndPreviouslyTested() {
        Object[][] results = Dither.aetg(3, 0, new Object[][] { new Object[] { 0, 1 },
                new Object[] { 0, 1 },
                new Object[] { 0, 1, 2, 3 }},
                new Integer[][]{ new Integer[]{0, null, 2}, new Integer[]{0, 1, 0}},
                new Object[][]{new Object[]{0, 0, 0}}
        );

        Set<List<Object>> actuals = new HashSet<List<Object>>();
        for(Object[] result : results) {
            actuals.add(Arrays.asList(result));
        }

        @SuppressWarnings("unchecked")
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
    public void anotherCompute3WayAtegWithConstraints() {
        Object[][] results = Dither.aetg(3, 0, new Object[][] { new Object[] { 0, 1 },
                new Object[] { 0, 1 },
                new Object[] { 0, 1 },
                new Object[] { 0, 1, 2, 3 }},
                new Integer[][]{ new Integer[]{0, 1, 0}}, Dither.EMPTY_PREVIOUSLY_TESTED);

        for(Object[] result : results) {
            assertFalse("satisfy constraint 0, 1, 0... " + Arrays.toString(result),
                    (result[0] == (Integer)0 && result[1] == (Integer)1 && result[2] == (Integer)0));
        }
    }
}
