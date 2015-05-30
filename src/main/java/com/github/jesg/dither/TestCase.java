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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

class TestCase extends HashSet<Param> {

    private final UnboundParam[] unboundParams;
    private final BoundParam[][] boundParams;

    public TestCase(final UnboundParam[] unboundParams,
            final BoundParam[][] boundParams,
            final Collection<BoundParam> params) {
        super(params);
        this.unboundParams = unboundParams;
        this.boundParams = boundParams;
    }

    public TestCase(final UnboundParam[] unboundParams,
            final BoundParam[][] boundParams) {
        super();
        this.unboundParams = unboundParams;
        this.boundParams = boundParams;
    }
    
    public TestCase createUnbound(final int i) {
        final boolean[] missing = new boolean[i + 1];
        for (final Param param : this) {
            missing[param.i()] = false;
        }

        for (int k = 0; k < missing.length; k++) {
            if (missing[k]) {
                this.add(unboundParams[k]);
            }
        }
        return this;
    }

    public int[] toIpogArray(final int t) {
        final int[] result = new int[t];
        Arrays.fill(result, -1); // -1 for unbound params
        for (final Param param : this) {
            if (!param.isBound()) {
                continue;
            }
            final BoundParam boundParam = (BoundParam) param;
            result[boundParam.i()] = boundParam.j();
        }
        return result;
    }

    public TestCase mergeWithoutConflict(final int i, final TestCase testCase) {
        final Param[] newElements = new Param[i + 1];
        int newElementsIndex = 0;
        final int[] thisArr = this.toIpogArray(i + 1);
        final int[] testCaseArr = testCase.toIpogArray(i + 1);

        for (int k = 0; k <= i; k++) {
            final int thisk = thisArr[k];
            final int testCasek = testCaseArr[k];

            if (thisk == -1 && testCasek == -1) {
                newElements[newElementsIndex] = unboundParams[k];
                newElementsIndex++;
            } else if ((thisk == testCasek) || testCasek == -1) {
                continue;
            } else if (thisk == -1) {
                newElements[newElementsIndex] = boundParams[k][testCasek];
                newElementsIndex++;
            } else if (thisk != testCasek) {
                return null;
            }
        }

        // commit merge
        for (final Param param : newElements) {
            if (param == null) {
                break;
            }
            this.add(param);
        }
        return this;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.toIpogArray(boundParams.length));
    }
}
