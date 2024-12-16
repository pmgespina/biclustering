package org.uma.jmetal.operator.mutation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

    public IntegerBiclusterMutation(double mutationProbability) {
        this(mutationProbability, new Random(), new Random());
    }

    public IntegerBiclusterMutation(double mutationProbability, 
            Random randomGeneratorProbability, Random randomGeneratorValues) {
        this.mutationProbability = mutationProbability;
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

        executeMutation(selectedGenes.variables());
        executeMutation(selectedConditions.variables());

        return solution;
    }

    private void executeMutation (List<Integer> indexList) {
        for (int i = 0 ; i < indexList.size(); i++) {
            if (this.randomGeneratorProbability.nextDouble() <= this.mutationProbability) {
                if (indexList.get(i).equals(-1)) {
                    /* A number not equals to -1, that is in the range of available values for the matrix, and not already present in the index list */
                    /* For the value to be in the range we will generate values only from 0 (inclusive) until list.size() (exclusive) (-1 value will not be possible to generate) */
                    /* We create a set where all the non-repeated indexes will be stored, and an auxiliar list where the non-present index will be stored */
                    HashSet<Integer> currentIndexes = new HashSet<>(indexList);
                    List<Integer> availableIndexes = new ArrayList<>();
                    
                    /* We start to include the available indexes in the auxiliar list */
                    for (Integer index : indexList) {
                        if (!currentIndexes.contains(index)) {
                            availableIndexes.add(index);
                        }
                    }

                    /* When there are available indexes that can be obtained, we will change the -1 index for those available indexes in a random way*/
                    if (!availableIndexes.isEmpty()) {
                        int indexValue = randomGeneratorValues.nextInt(availableIndexes.size());
                        indexList.set(i, availableIndexes.get(indexValue));
                    }
                } else {
                    indexList.set(i, -1);
                }
                Collections.sort(indexList);
            }
        }
    }

}