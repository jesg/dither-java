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
import java.util.Arrays;
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

    public static int[][] product(final int[] input) {
        int[][] result = _product(input[0], input[1]);
        for (int i = 2; i < input.length; i++) {
            result = _product(result, input[i]);
        }
        return result;
    }

    private static int[][] _product(final int left, final int right) {
        final int[][] result = new int[left * right][];
        int k = 0;
        for (int i = 0; i < left; i++) {
            for (int j = 0; j < right; j++) {
                result[k] = new int[] { i, j };
                k++;
            }
        }
        return result;
    }

    private static int[][] _product(final int[][] left, final int right) {
        final int[][] result = new int[left.length * right][];
        int k = 0;
        for (int i = 0; i < left.length; i++) {
            for (int j = 0; j < right; j++) {
                result[k] = Arrays.copyOf(left[i], left[i].length + 1);
                result[k][left[i].length] = j;
                k++;
            }
        }
        return result;
    }
}
