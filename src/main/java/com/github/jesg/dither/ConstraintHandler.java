package com.github.jesg.dither;

import java.util.Arrays;

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

import java.util.Comparator;

class ConstraintHandler {

    private final Pair[][] constraints;
    private final int[] bounds;
    private final int[] scratch;

    ConstraintHandler(final Pair[][] constraints, final int[] bounds) {
        this.constraints = constraints;
        this.bounds = new int[bounds.length];
        this.scratch = new int[bounds.length];
        for(int i = 0; i < bounds.length; i++) {
            this.bounds[i] = bounds[i] - 1;
        }
        Arrays.fill(scratch, -1);
        Arrays.sort(this.constraints, new Comparator<Pair[]>() {
            public int compare(final Pair[] a, final Pair[] b) { return a.length - b.length; }
        });
    }

    boolean violateConstraints_(final int[] testCase) {
outer:
        for(final Pair[] pairs : constraints) {
            for(final Pair pair : pairs) {
                final int value = testCase[pair.i];
                if(value == -1 || value != pair.j) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    boolean violateConstraints(final int[] solution) {
        if(violateConstraints_(solution)) {
            return true;
        }
        for(int i = 0; i < solution.length; i++) {
            scratch[i] = solution[i];
        }
        return groundSolution(scratch) == null;
    }

    boolean violateConstraints(final Pair[] pairs) {
        Arrays.fill(scratch, -1);
        for(int i = 0; i < pairs.length; i++) {
            final Pair pair = pairs[i];
            scratch[pair.i] = pair.j;
        }
        if(violateConstraints_(scratch)) {
            return true;
        }
        return groundSolution(scratch) == null;
    }

    // return null if unable to find a solution
    int[] groundSolution(final int[] solution) {
        final int[] indexes = new int[solution.length];
        int last_index = 0;
        for(int i = 0; i < solution.length; i++) {
            if(solution[i] == -1) {
                indexes[last_index] = i;
                ++last_index;
            }
        }
        final int[] bound_values = new int[last_index + 1];
        Arrays.fill(bound_values, -1);
        int i = 0;

outer:
        while(i < bound_values.length) {
            final int max = bounds[indexes[i]];
            for(int value = bound_values[i] + 1; value <= max; value++) {
                solution[indexes[i]] = value;
                if(violateConstraints_(solution)) {
                    continue;
                }
                bound_values[i] = value;
                ++i;
                continue outer;
            }

            if(i == 0) {
                return null;
            }

            // unwind
            bound_values[i] = -1;
            solution[indexes[i]] = -1;
            --i;
        }
        return solution;
    }
}
