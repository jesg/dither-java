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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class TestCaseTest {

    private static final UnboundParam[] unboundParams = new UnboundParam[] {
            new UnboundParam(0), new UnboundParam(1), new UnboundParam(2),
            new UnboundParam(3) };

    private static final BoundParam[][] boundParams = new BoundParam[][] {
            new BoundParam[] { new BoundParam(0, 0), new BoundParam(0, 1) },
            new BoundParam[] { new BoundParam(1, 0), new BoundParam(1, 1) } };
    
    private static final TestCase[] EMPTY_CONSTRAINTS = new TestCase[]{};

    @Test
    public void testCreateUnboundDoesNotOverrideBoundParams() {
        TestCase testCase = new TestCase(unboundParams, null, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 1)));
        TestCase fillWithUnbound = testCase.createUnbound(1);
        assertFalse(fillWithUnbound.contains(unboundParams[0]));
    }

    @Test
    public void testCreateUnboundAddsUnboundParams() {
        TestCase testCase = new TestCase(unboundParams, null, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 1)));
        TestCase fillWithUnbound = testCase.createUnbound(2);
        assertFalse(fillWithUnbound.contains(unboundParams[1]));
    }

    @Test
    public void canCreateIpogArray() {
        TestCase testCase = new TestCase(unboundParams, null, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 1)));
        int[] ipogArray = testCase.toIpogArray(3);
        assertArrayEquals(new int[] { 1, -1, -1 }, ipogArray);
    }

    @Test
    public void canNotMergeIfConflict() {
        TestCase testCase1 = new TestCase(unboundParams, boundParams, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 1)));
        TestCase testCase2 = new TestCase(unboundParams, boundParams, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 0)));
        TestCase testCase1Merge = testCase1.mergeWithoutConflict(1, testCase2);
        assertNull(testCase1Merge);
        assertTrue(testCase1.contains(new BoundParam(0, 1)));
        assertFalse(testCase1.contains(new BoundParam(0, 0)));
    }

    @Test
    public void canMergeIfSame() {
        TestCase testCase1 = new TestCase(unboundParams, boundParams, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 1)));
        TestCase testCase2 = new TestCase(unboundParams, boundParams, EMPTY_CONSTRAINTS,
                Arrays.asList(new BoundParam(0, 1)));
        TestCase testCase1Merge = testCase1.mergeWithoutConflict(2, testCase2);
        assertNotNull(testCase1Merge);
        assertTrue(testCase1.contains(new BoundParam(0, 1)));
        assertTrue(testCase1.contains(new UnboundParam(1)));
    }

    @Test
    public void canMergeIfFirstNull() {
        TestCase testCase1 = new TestCase(unboundParams, boundParams);
        testCase1.addAll(Arrays.asList(new BoundParam(0, 1)));
        TestCase testCase2 = new TestCase(unboundParams, boundParams);
        testCase2.addAll(Arrays.asList(new BoundParam(0, 1), new BoundParam(1, 0)));
        TestCase testCase1Merge = testCase1.mergeWithoutConflict(1, testCase2);
        assertNotNull(testCase1Merge);
        assertTrue(testCase1.contains(new BoundParam(0, 1)));
        assertTrue(testCase1.contains(new BoundParam(1, 0)));
    }
}
