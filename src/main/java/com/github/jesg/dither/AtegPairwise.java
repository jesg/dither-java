package com.github.jesg.dither;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

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

class AtegPairwise extends Ateg {

	private final int n = 50;
	private final int[][] scratch = new int[n][];
	private final int[] fitness = new int[n];
	private final Object[][] params;
	private final Pair[][] pairCache;
	private final List<Pair[]> coverage;
	private final AtegRandom random;


	public AtegPairwise(final Object[][] params) {
		this.params = params;

		this.random = new AtegRandom() {
			final Random random = new Random(0);
			public int nextInt(int i) { return random.nextInt(i); }
		};

		this.pairCache = new Pair[params.length][];
		for(int i = 0; i < params.length; i++) {
			this.pairCache[i] = new Pair[params[i].length];

			for(int j = 0; j < params.length; j++) {
				this.pairCache[i][j] = new Pair(i, j);
			}
		}

		for(int i = 0; i < n; i++) {
			scratch[i] = new int[params.length];
		}

		final int[] arr = new int[params.length];
		for(int i = 0; i < params.length; i++) {
			arr[i] = i;
		}

		final int t = 2;
		coverage = new LinkedList<Pair[]>();
		for(final int[] tmp : CombinatoricHelper.getCombinations(t, arr)) {
			final Pair[][] pairs = new Pair[tmp.length][];
			for(int i = 0; i < pairs.length; i++) {
				pairs[i] = this.pairCache[i];
			}

			for(final List<Pair> innerPairs : CombinatoricHelper.product(pairs)) {
				coverage.add(innerPairs.toArray(new Pair[innerPairs.size()]));
			}
		}
	}


	public boolean hasNext() {
		return !coverage.isEmpty();
	}

	public Object[] next() {
		generate();
		fitness();
		final int i = bestFit();
		if(fitness[i] == 0) {
			return null;
		}
		final int[] best = scratch[i];
		final Object[] result = new Object[params.length];
		for(int k = 0; k < result.length; k++) {
			result[k] = params[k][best[k]];
		}
		removeMatches(i);
		return result;
	}

	private void removeMatches(final int i) {
		final int[] cases = scratch[i];
		final ListIterator<Pair[]> iter = coverage.listIterator(0);
		while(iter.hasNext()) {
			if(match(iter.next(), cases)) {
				iter.remove();
			}
		}
	}

	private void generate() {
		for(int i = 0; i < n; i++) {
			generate(i);
		}
	}

	private void generate(final int i) {
		for(int j = 0; j < params.length; j++) {
			scratch[i][j] = random.nextInt(params[j].length);
		}
	}

	private void fitness() {
		for(int i = 0; i < scratch.length; i++) {
			fitness(i);
		}
	}

	private void fitness(final int i) {
		final int[] cases = scratch[i];
		int count = 0;
		for(final Pair[] pairs : coverage) {
			if(match(pairs, cases)) {
				count++;
			}
		}
		fitness[i] = count;
	}

	private int bestFit() {
		int max = 0;
		for(final int i : fitness) {
			if(i > max) {
				max = i;
			}
		}
		return max;
	}

	private interface AtegRandom {
		int nextInt(int a);
	}

	private static boolean match(Pair[] pairs, int[] cases) {
		boolean flag = true;
		for(final Pair pair : pairs) {
			if(cases[pair.i] != pair.j) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	private class Pair {
		final int i;
		final int j;

		Pair(final int i, final int j) {
			this.i = i;
			this.j = j;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + i;
			result = prime * result + j;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (i != other.i)
				return false;
			if (j != other.j)
				return false;
			return true;
		}
	}
}
