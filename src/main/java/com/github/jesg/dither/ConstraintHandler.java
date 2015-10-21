package com.github.jesg.dither;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.search.solution.Solution;

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


class ConstraintHandler {

    private final Pair[][] constraints;
    private final int[] bounds;

    ConstraintHandler(final Pair[][] constraints, final int[] bounds) {
        this.constraints = constraints;
        this.bounds = new int[bounds.length];
        for(int i = 0; i < bounds.length; i++) {
            this.bounds[i] = bounds[i] - 1;
        }
    }

    boolean violateConstraints(final int[] solution) {
        return !createSolver(solution).solver.findSolution();
    }

    boolean violateConstraints(final Pair[] pairs) {
        final int[] solution = new int[bounds.length];
        Arrays.fill(solution, -1);
        for(int i = 0; i < pairs.length; i++) {
            final Pair pair = pairs[i];
            solution[pair.i] = pair.j;
        }
        return violateConstraints(solution);
    }

    // return null if unable to find a solution
    int[] groundSolution(final int[] solution) {
        final SolverTuple solverTuple = createSolver(solution);
        final Solver solver = solverTuple.solver;
        if(!solver.findSolution()) {
            return null;
        }

        final Solution chocoSolution = solver.getSolutionRecorder().getLastSolution();
        for(int i = 0; i < solution.length; i++) {
            if(solution[i] == -1) {
                solution[i] = chocoSolution.getIntVal(solverTuple.boundVars[i]);
            }
        }

        return solution;
    }

    // TODO can we reuse the solver object?
    private SolverTuple createSolver(final int[] solution) {
        final Solver innerSolver = new Solver();
        final IntVar[] boundVars = new IntVar[solution.length];

        for(int i = 0; i < solution.length; i++) {
            boundVars[i] = VariableFactory.bounded("param-"+Integer.toString(i), 0, bounds[i], innerSolver);
            if(solution[i] != -1) {
                innerSolver.post(IntConstraintFactory.arithm(boundVars[i], "=", solution[i]));
            }
        }

        for(final Pair[] constraint : constraints) {
            final Constraint[] innerConstraints = new Constraint[constraint.length];

            for(int i = 0; i < constraint.length; i++) {
                final Pair pair = constraint[i];
                innerConstraints[i] = IntConstraintFactory.arithm(boundVars[pair.i], "=", pair.j);
            }

            // not an efficient logical expression
            innerSolver.post(LogicalConstraintFactory.not(LogicalConstraintFactory.and(innerConstraints)));
        }

        innerSolver.set(IntStrategyFactory.lexico_LB(boundVars));
        return new SolverTuple(innerSolver, boundVars);
    }

    private static class SolverTuple {

        final Solver solver;
        final IntVar[] boundVars;

        SolverTuple(final Solver solver, final IntVar[] boundVars) {
            this.solver = solver;
            this.boundVars = boundVars;
        }
    }

}
