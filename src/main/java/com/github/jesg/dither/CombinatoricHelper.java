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

import java.util.ArrayList;
import java.util.List;

class CombinatoricHelper {

    public static List<int[]> getCombinations(final int n, final int[] input) {
        final List<int[]> result = new ArrayList<int[]>();
        final int[] scratch = new int[n];

        if (n <= input.length) {
            for (int i = 0; (scratch[i] = i) < n - 1; i++)
                ;
            result.add(_getCombinations(input, scratch));
            for (;;) {
                int i;
                for (i = n - 1; i >= 0 && scratch[i] == input.length - n + i; i--)
                    ;
                if (i < 0) {
                    break;
                } else {
                    scratch[i]++;
                    for (++i; i < n; i++) {
                        scratch[i] = scratch[i - 1] + 1;
                    }
                    result.add(_getCombinations(input, scratch));
                }
            }
        }

        return result;
    }

    private static int[] _getCombinations(final int[] input, final int[] scratch) {
        final int[] result = new int[scratch.length];
        for (int i = 0; i < scratch.length; i++) {
            result[i] = input[scratch[i]];
        }
        return result;
    }


    public static <T> List<List<T>> product(final T[][] args) {
        final int[] tmp = new int[args.length];
        for(int i = 0; i < tmp.length; i++) {
            tmp[i] = args[i].length;
        }

        final int[][] solution = product(tmp);
        final List<List<T>> results = new ArrayList<List<T>>(solution.length);
        for(int i = 0; i < solution.length; i++) {
            final List<T> inner = new ArrayList<T>(args.length);
            results.add(i, inner);
            for(int j = 0; j < args.length; j++) {
                results.get(i).add(j, args[j][solution[i][j]]);
            }
        }
        return results;
    }

    public static int[][] product(final int[] ranges) {
        int length = 1;
        for(int i = 0; i < ranges.length; i++) {
            length *= ranges[i];
            --ranges[i];
        }

        final int[][] results = new int[length][ranges.length];
        final int[] scratch = new int[ranges.length];

        int k = 0;
        final int max = ranges.length - 1;
        for(int i = max;;) {

            if(i == max) {
                for(int val = 0; val <= ranges[i]; val++) {
                    for(int j = 0; j < scratch.length; j++) {
                        results[k][j] = scratch[j];
                    }
                    k++;
                    scratch[i]++;
                }
                scratch[i] = 0;
                i--;
            } else if(i == 0 && scratch[i] >= ranges[i]) {
                return results;
            } else if(scratch[i] < ranges[i]) {
                scratch[i]++;
                i++;
            } else {
                scratch[i] = -1;
                i--;
            }
        }
    }
}
