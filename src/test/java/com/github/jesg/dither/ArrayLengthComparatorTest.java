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

import org.junit.Test;

public class ArrayLengthComparatorTest {

    @Test
    public void canSortByArrayLength() {
        IndexArrayPair[] params = new IndexArrayPair[] {
                new IndexArrayPair(0, new Integer[] { 1 }),
                new IndexArrayPair(1, new Integer[] { 1, 2 }) };
        Arrays.sort(params, new ArrayLengthComparator());
        assertTrue(params[0].getArr().length == 2);
    }

}
