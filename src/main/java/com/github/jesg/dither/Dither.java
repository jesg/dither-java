package com.github.jesg.dither;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public static final Integer[][] EMPTY_CONSTRAINTS = new Integer[][]{};
    public static final Object[][] EMPTY_PREVIOUSLY_TESTED = new Object[][]{};

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

    public static List<Object[]> ateg(final Object[][] params)
        throws DitherError {
        return ateg(2, params);
    }

    public static List<Object[]> ateg(final int t, final Object[][] params) {
        return ateg(t, null, params, EMPTY_CONSTRAINTS, EMPTY_PREVIOUSLY_TESTED);
    }

    public static List<Object[]> ateg(final int t, final Integer seed, final Object[][] params, final Integer[][] constraints, final Object[][] previouslyTested)
        throws DitherError {
        // validate input
        if (t <= 1) {
            throw new DitherError("t must be >= 2");
        }

        if (t > params.length) {
            throw new DitherError("t must be <= params.length");
        }
        for (final Object[] param : params) {
            if (param.length < 2) {
                throw new DitherError("param length must be > 1");
            }
        }
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Object[]> result = Collections.emptyList();
        try {
            result = new AtegPairwise(t, seed, params, constraints, previouslyTested, executor).toList();
        } finally {
            executor.shutdownNow();
        }
        return result;
    }
}
