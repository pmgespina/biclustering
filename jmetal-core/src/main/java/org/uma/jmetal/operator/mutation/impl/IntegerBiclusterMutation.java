package org.uma.jmetal.operator.mutation.impl;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.IntegerCompositeSolution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

public class IntegerBiclusterMutation implements MutationOperator<IntegerCompositeSolution> {

    private double mutationProbability;
    private final List<MutationOperator<Solution<?>>> operators;
    private final RandomGenerator<Double> randomGeneratorProbability;
    private final RandomGenerator<Integer> randomGeneratorValues;

    public IntegerBiclusterMutation(List<?> operators, double probability, 
            RandomGenerator<Double> randomGeneratorProbability, RandomGenerator<Integer> randomGeneratorValues) {
        Check.notNull(operators);
        Check.collectionIsNotEmpty(operators);

        this.operators = new ArrayList<>();
        for (Object operator: operators) {
            Check.that(operator instanceof MutationOperator, "The operator is not a Mutation Operator");
            this.operators.add((MutationOperator<Solution<?>>) operator);
        }
        this.mutationProbability = probability;
        this.randomGeneratorProbability = randomGeneratorProbability;
        this.randomGeneratorValues = randomGeneratorValues;
    }

    @Override
    public double mutationProbability() {
        return mutationProbability;
    }

    public void setMutationProbability(double probability) {
        Check.that(probability >= 0 && probability <= 1, "The probability value is out of bounds [0,1]");
        this.mutationProbability = probability;
    }

    @Override
    public IntegerCompositeSolution execute(IntegerCompositeSolution solution) {
        List<IntegerPermutationSolution> bicluster = solution.variables();
        for (int i = 0; i < bicluster.size(); i++) {
            for (int j = 0 ; j < bicluster.get(i).variables().size(); j++) {
                if (this.randomGeneratorProbability.getRandomValue() <= this.mutationProbability) {
                    if (bicluster.get(i).variables().get(j) == -1) {
                        /* numero que sea distinto de -1, este en el rango de indices posibles por la matriz, y que no este ya en la lista de indices */
                        /* para que esté en el rango, cuando definamos el operador de mutacion en el runner del algoritmo definiremos los bounds */
                        /* no va a generar un -1 porque vamos a establecer que el rango de valores del randomGenerator sea de 0 a numGenes */
                        while (bicluster.get(i).variables().contains(randomGeneratorValues.getRandomValue())) { }
                        int value = randomGeneratorValues.getRandomValue();
                        bicluster.get(i).variables().set(j, value);
                    } else {
                        bicluster.get(i).variables().set(j, -1);
                    }
                }
            }
        }
        return new IntegerCompositeSolution(bicluster);
    }

}
