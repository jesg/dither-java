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

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CombinatoricHelperTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void canGetCombinations() {
        int[] input = { 1, 2, 3 };
        int k = 2;
        List<int[]> combs = CombinatoricHelper.getCombinations(k, input);
        assertArrayEquals(new int[][] { { 1, 2 }, { 1, 3 }, { 2, 3 } },
                combs.toArray());
    }

    @Test
    public void canProduct() {
        int[][] product = CombinatoricHelper.product(new int[] { 2, 2, 2 });
        assertArrayEquals(
                new int[][] { { 0, 0, 0 }, { 0, 0, 1 }, { 0, 1, 0 },
                        { 0, 1, 1 }, { 1, 0, 0 }, { 1, 0, 1 }, { 1, 1, 0 },
                        { 1, 1, 1 } }, product);
    }

    @Test
    public void canGetCombinations2() {
        int[] input = { 1, 2, 3 };
        int k = 2;
        List<int[]> combs = CombinatoricHelper.getCombinations(k, input);
        assertArrayEquals(new int[][] { { 1, 2 }, { 1, 3 }, { 2, 3 } },
                combs.toArray());
    }
}
