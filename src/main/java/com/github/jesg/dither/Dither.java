package com.github.jesg.dither;

import java.util.List;

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

public class Dither {
    
    private static final Integer[][] EMPTY_CONSTRAINTS = new Integer[][]{};
    private static final Object[][] EMPTY_PREVIOUSLY_TESTED = new Object[][]{};

    public static Object[][] ipog(final int t, final Object[][] params, final Integer[][] constraints, final Object[][] previouslyTested)
            throws DitherError {
        return new IPOG(params, t, constraints, previouslyTested).run();
    }
    
    public static Object[][] ipog(final int t, final Object[][] params, final Integer[][] constraints)
            throws DitherError {
        return new IPOG(params, t, constraints, EMPTY_PREVIOUSLY_TESTED).run();
    }

    public static Object[][] ipog(final int t, final Object[][] params)
            throws DitherError {
        return new IPOG(params, t, EMPTY_CONSTRAINTS, EMPTY_PREVIOUSLY_TESTED).run();
    }

    public static Object[][] ipog(final Object[][] params)
            throws DitherError {
        return new IPOG(params, 2, EMPTY_CONSTRAINTS, EMPTY_PREVIOUSLY_TESTED).run();
    }

    public static Object[][] ipog(final int t, final Object[] params, final Object[] constraints, final Object[] previouslyTested)
            throws DitherError {
        final Object[][] innerParams = new Object[params.length][];
        for(int i = 0; i < innerParams.length; i++) {
            innerParams[i] = (Object[]) params[i];
        }

        final Integer[][] innerConstraints = new Integer[constraints.length][];
        for(int i = 0; i < innerConstraints.length; i++) {
            innerConstraints[i] = (Integer[]) constraints[i];
        }

        final Object[][] innerPerviouslyTested = new Object[previouslyTested.length][];
        for(int i = 0; i < innerPerviouslyTested.length; i++) {
            innerPerviouslyTested[i] = (Object[]) previouslyTested[i];
        }
        
        return new IPOG(innerParams, t, innerConstraints, innerPerviouslyTested).run();
    }
}
