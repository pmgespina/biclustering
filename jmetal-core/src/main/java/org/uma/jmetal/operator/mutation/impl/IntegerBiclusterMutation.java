package org.uma.jmetal.operator.mutation.impl;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.errorchecking.Check;

public class IntegerBiclusterMutation implements MutationOperator<CompositeSolution> {

    /* This will be the mutation that will be applied to Integer Biclusters with the CompositeMutation operator
     * defined in this folder. For each of the IntegerCompositeSolution that we have (representing a bicluster),
     * we will do a mutation in it. In the runner class for the algorithm we will define a CompositeMutation
     */

    private double mutationProbability;
    private Random randomGeneratorProbability;
    private Random randomGeneratorValues;

    public IntegerBiclusterMutation(double probability) {
        this(probability, new Random(), new Random());
    }

    public IntegerBiclusterMutation(double probability, 
            Random randomGeneratorProbability, Random randomGeneratorValues) {
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

    /*
     * For future modifications do a package where we store the implementations of mutation operators for rows and columns
     * They will extend from an interface that will store the common behaviour (the execute method)
     * In this way we can add more mutation operators simply implementing this interface
     */
    @Override
    public CompositeSolution execute(CompositeSolution solution) {
        Check.notNull(solution);

        List<Solution<?>> bicluster = solution.variables();
        IntegerSolution selectedGenes = (IntegerSolution) bicluster.get(0);
        IntegerSolution selectedConditions = (IntegerSolution) bicluster.get(1);

        for (int i = 0 ; i < selectedGenes.variables().size(); i++) {
            if (this.randomGeneratorProbability.nextDouble() <= this.mutationProbability) {
                if (selectedGenes.variables().get(i).equals(-1)) {
                    /* numero que sea distinto de -1, este en el rango de indices posibles por la matriz, y que no este ya en la lista de indices */
                    /* para que esté en el rango, cuando definamos el operador de mutacion en el runner del algoritmo definiremos los bounds */
                    /* no va a generar un -1 porque vamos a establecer que el rango de valores del randomGenerator sea de 0 a numGenes */
                    int value = randomGeneratorValues.nextInt(selectedGenes.variables().size());
                    while (selectedGenes.variables().contains(value)) { }
                    selectedGenes.variables().set(i, value);
                } else {
                    selectedGenes.variables().set(i, -1);
                }
                Collections.sort(selectedGenes.variables());
            }
        }

        for (int i = 0 ; i < selectedConditions.variables().size(); i++) {
            if (this.randomGeneratorProbability.nextDouble() <= this.mutationProbability) {
                if (selectedConditions.variables().get(i).equals(-1)) {
                    while (selectedConditions.variables().contains(randomGeneratorValues.nextInt(selectedConditions.variables().size()))) { }
                    int value = randomGeneratorValues.nextInt(selectedConditions.variables().size());
                    selectedConditions.variables().set(i, value);
                } else {
                    selectedConditions.variables().set(i, -1);
                }
                Collections.sort(selectedConditions.variables());
            }
        }

        return solution;
    }

}