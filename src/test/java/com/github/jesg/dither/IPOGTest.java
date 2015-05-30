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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class IPOGTest {

    private static final UnboundParam[] unboundParams = new UnboundParam[] {
            new UnboundParam(0), new UnboundParam(1), new UnboundParam(2),
            new UnboundParam(3) };

    private static final BoundParam[][] boundParams = new BoundParam[][] {
            new BoundParam[] { new BoundParam(0, 0), new BoundParam(0, 1) },
            new BoundParam[] { new BoundParam(1, 0), new BoundParam(1, 1) } };

    @Before
    public void setUp() throws Exception {
    }

    @Test(expected = DitherError.class)
    public void tMustBeGreaterThan2() {
        new IPOG(new Object[][] {}, 0);
    }

    @Test(expected = DitherError.class)
    public void tMustBeGreaterThanParamLength() {
        new IPOG(new Object[][] { new Object[] {} }, 3);
    }

    @Test(expected = DitherError.class)
    public void paramsLengthMustBeGreaterThan2() {
        new IPOG(new Object[][] { new Object[] {}, new Object[] {} }, 2);
    }

    @Test
    public void canGetAllCombinations() {
        IPOG ipog = new IPOG(new Object[][] { new Object[] { 1, 2 },
                new Object[] { 3, 4 } }, 2);
        List<TestCase> testCases = ipog.allCombinations();
        // spot check
        assertTrue(testCases.get(0).contains(new BoundParam(0, 0)));
        assertTrue(testCases.get(0).contains(new BoundParam(1, 0)));
        assertTrue(testCases.get(2).contains(new BoundParam(0, 1)));
        assertTrue(testCases.get(2).contains(new BoundParam(1, 0)));
    }

    /*
     * @Test public void canGetCombinations() { IPOG ipog = new IPOG(new
     * Object[][]{new Object[]{1,2},new Object[]{3,4},new Object[]{5,6},new
     * Object[]{7,8}},2); List<TestCase> testCases = ipog.combinations(3);
     * assertTrue(testCases.get(4).containsAll(Arrays.asList(new
     * BoundParam(1,0),new BoundParam(3,0)))); }
     */

    @Test
    public void canGen3WayCases() {
        Object[][] results = Dither.ipog(3, new Object[][] { new Object[] { 1, 2 },
                new Object[] { "1", "2" },
                new Object[] { 1.0, 2.0 }, new Object[] { true, false, 3 } });

        assertTrue(results.length == 12);
    }
}
