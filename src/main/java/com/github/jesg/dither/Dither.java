package com.github.jesg.dither;

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

    private Dither() {}

    public static Object[][] ipog(final int t, final Object[][] params, final Integer[][] constraints, final Object[][] previouslyTested)
            throws DitherError {
        validateInput(t, params);
        return new Ipog(params, t, constraints, previouslyTested).run();
    }

    public static Object[][] ipog(final int t, final Object[][] params, final Integer[][] constraints)
            throws DitherError {
        validateInput(t, params);
        return new Ipog(params, t, constraints, EMPTY_PREVIOUSLY_TESTED).run();
    }

    public static Object[][] ipog(final int t, final Object[][] params)
            throws DitherError {
        validateInput(t, params);
        return new Ipog(params, t, EMPTY_CONSTRAINTS, EMPTY_PREVIOUSLY_TESTED).run();
    }

    public static Object[][] ipog(final Object[][] params)
            throws DitherError {
        validateInput(2, params);
        return new Ipog(params, 2, EMPTY_CONSTRAINTS, EMPTY_PREVIOUSLY_TESTED).run();
    }

    public static Object[][] ipog(final int t, final Object[] params, final Object[] constraints, final Object[] previouslyTested)
        throws DitherError {
        final Object[][] innerParams = new Object[params.length][];
        for(int i = 0; i < innerParams.length; i++) {
            innerParams[i] = (Object[]) params[i];
        }
        validateInput(t, innerParams);

        final Integer[][] innerConstraints = new Integer[constraints.length][];
        for(int i = 0; i < innerConstraints.length; i++) {
            innerConstraints[i] = (Integer[]) constraints[i];
        }

        final Object[][] innerPreviouslyTested = new Object[previouslyTested.length][];
        for(int i = 0; i < innerPreviouslyTested.length; i++) {
            innerPreviouslyTested[i] = (Object[]) previouslyTested[i];
        }

        return new Ipog(innerParams, t, innerConstraints, innerPreviouslyTested).run();
    }

    @Deprecated
    public static Object[][] ateg(final Object[][] params)
        throws DitherError {
        return aetg(params);
    }

    public static Object[][] aetg(final Object[][] params)
        throws DitherError {
        return aetg(2, params);
    }

    @Deprecated
    public static Object[][] ateg(final int t, final Object[][] params) {
        return aetg(t, params);
    }

    public static Object[][] aetg(final int t, final Object[][] params) {
        return aetg(t, null, params, EMPTY_CONSTRAINTS, EMPTY_PREVIOUSLY_TESTED);
    }

    @Deprecated
    public static Object[][] ateg(final int t, final Integer seed, final Object[][] params, final Integer[][] constraints, final Object[][] previouslyTested)
        throws DitherError {
        return aetg(t, seed, params, constraints, previouslyTested);
    }

    public static Object[][] aetg(final int t, final Integer seed, final Object[][] params, final Integer[][] constraints, final Object[][] previouslyTested)
        throws DitherError {
        validateInput(t, params);
        final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Object[][] result = new Object[][]{};
        try {
            result = new AetgPairwise(t, seed, params, constraints, previouslyTested, executor).toArray();
        } finally {
            executor.shutdownNow();
        }
        return result;
    }

    @Deprecated
    public static Object[][] ateg(final int t, final Integer seed, final Object[] params, final Object[] constraints, final Object[] previouslyTested) {
        return aetg(t, seed, params, constraints, previouslyTested);
    }

    public static Object[][] aetg(final int t, final Integer seed, final Object[] params, final Object[] constraints, final Object[] previouslyTested) {
        final Object[][] innerParams = new Object[params.length][];
        for(int i = 0; i < innerParams.length; i++) {
            innerParams[i] = (Object[]) params[i];
        }
        validateInput(t, innerParams);

        final Integer[][] innerConstraints = new Integer[constraints.length][];
        for(int i = 0; i < innerConstraints.length; i++) {
            innerConstraints[i] = (Integer[]) constraints[i];
        }

        final Object[][] innerPreviouslyTested = new Object[previouslyTested.length][];
        for(int i = 0; i < innerPreviouslyTested.length; i++) {
            innerPreviouslyTested[i] = (Object[]) previouslyTested[i];
        }
		return aetg(t, seed, innerParams, innerConstraints, innerPreviouslyTested);
	}

    private static void validateInput(final int t, final Object[][] params) throws DitherError {
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
    }
}
