package org.uma.jmetal.operator.mutation.impl;

import java.util.List;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.compositesolution.IntegerCompositeSolution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

public class IntegerBiclusterMutation implements MutationOperator<IntegerCompositeSolution> {

    /* This will be the mutation that will be applied to Integer Biclusters with the CompositeMutation operator
     * defined in this folder. For each of the IntegerCompositeSolution that we have (representing a bicluster),
     * we will do a mutation in it. In the runner class for the algorithm we will define a CompositeMutation
     */

    private double mutationProbability;
    private final PseudoRandomGenerator randomGeneratorProbability;
    private final PseudoRandomGenerator randomGeneratorValues;

    public IntegerBiclusterMutation(double probability, 
            PseudoRandomGenerator randomGeneratorProbability, PseudoRandomGenerator randomGeneratorValues) {
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
                if (this.randomGeneratorProbability.nextDouble() <= this.mutationProbability) {
                    if (bicluster.get(i).variables().get(j) == -1) {
                        /* numero que sea distinto de -1, este en el rango de indices posibles por la matriz, y que no este ya en la lista de indices */
                        /* para que esté en el rango, cuando definamos el operador de mutacion en el runner del algoritmo definiremos los bounds */
                        /* no va a generar un -1 porque vamos a establecer que el rango de valores del randomGenerator sea de 0 a numGenes */
                        while (bicluster.get(i).variables().contains(randomGeneratorValues.nextInt(0, bicluster.get(i).variables().size() - 1))) { }
                        int value = randomGeneratorValues.nextInt(0, bicluster.get(i).variables().size() - 1);
                        bicluster.get(i).variables().set(j, value);
                    } else {
                        bicluster.get(i).variables().set(j, -1);
                    }
                }
            }
        }

        return solution;
    }

}
