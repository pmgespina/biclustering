package org.uma.jmetal.operator.mutation.impl;

import java.util.List;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

public class IntegerBiclusterMutation<IntegerPermutationCompositeSolution> implements MutationOperator<IntegerPermutationCompositeSolution> {

    private double mutationProbability;
    private final RandomGenerator<Integer> randomGenerator;
    private List<MutationOperator<Solution<?>>> operators;

    public IntegerBiclusterMutation(double probability) {
        /* The random generation of integers will be from 0 to */
        this(probability, JMetalRandom.nextInt());
    }

    public IntegerBiclusterMutation(double probability, RandomGenerator<Integer> random) {
        Check.probabilityIsValid(mutationProbability);
        Check.notNull(random);
        mutationProbability = probability;
        randomGenerator = random;
    }

    @Override
    public IntegerPermutationCompositeSolution execute(IntegerPermutationCompositeSolution solution) {
        List<IntegerPermutationSolution> bicluster = ;
        // al final de la ejecución de la mutación tenemos que ordenar los números para que me queden los -1 separado de los demás
        return solution;

    }
    @Override
    public double mutationProbability() {
        return mutationProbability;
    }

}
