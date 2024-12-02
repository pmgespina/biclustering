package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.compositesolution.IntegerCompositeSolution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;


public class IntegerBiclusterCrossover implements CrossoverOperator<IntegerCompositeSolution> {

    private double crossoverProbability;
    private final PseudoRandomGenerator crossoverRandomGenerator;
    private final PseudoRandomGenerator pointRandomGenerator;

    public IntegerBiclusterCrossover(double crossoverProbability, PseudoRandomGenerator crossRandomGenerator, PseudoRandomGenerator pointRandomGenerator) {
        this.crossoverProbability = crossoverProbability;
        this.crossoverRandomGenerator = crossRandomGenerator;
        this.pointRandomGenerator = pointRandomGenerator;
    }

    @Override
    public List<IntegerCompositeSolution> execute(List<IntegerCompositeSolution> source) {
        /* Lo que se pasa como parametro es una lista de listas, porque IntegerCompositeSolution
         * a su vez tendrá dos listas de IntegerPermutationSolutions, representando a los genes
         * y a las condiciones respectivamente
         */
        Check.notNull(source);
        Check.that(source.size() == 2, "There must be two parents instead of " + source.size());

        return doCrossover(this.crossoverProbability, source.get(0), source.get(1));
    }
        
    private List<IntegerCompositeSolution> doCrossover(double probability,
            IntegerCompositeSolution parent1, IntegerCompositeSolution parent2) {

        List<IntegerCompositeSolution> offspring = new ArrayList<>(2);
        offspring.add((IntegerCompositeSolution)parent1.copy());
        offspring.add((IntegerCompositeSolution)parent2.copy());

        if (this.crossoverRandomGenerator.nextDouble() <= probability) {
            // Define the lists that will contain the genes and conditions for the parents
            List<Integer> parent1Genes = parent1.variables().get(0).variables();
            List<Integer> parent2Genes = parent2.variables().get(0).variables();
            List<Integer> parent1Conditions = parent1.variables().get(1).variables();
            List<Integer> parent2Conditions = parent2.variables().get(1).variables();

            int crossoverPointGenes = this.pointRandomGenerator.nextInt(1, parent1Genes.size() - 1);
            int crossoverPointConditions = this.pointRandomGenerator.nextInt(1, parent1Conditions.size() - 1);

            List<Integer> offspring1Genes = new ArrayList<>()
                , offspring2Genes = new ArrayList<>(), offspring1Conditions = new ArrayList<>()
                , offspring2Conditions = new ArrayList<>();

            offspring1Genes.addAll(parent1Genes.subList(0, crossoverPointGenes));
            offspring1Genes.addAll(parent2Genes.subList(crossoverPointGenes, parent2Genes.size()));
            offspring1Conditions.addAll(parent1Conditions.subList(0, crossoverPointConditions));
            offspring1Conditions.addAll(parent2Conditions.subList(crossoverPointConditions, parent2Conditions.size()));

            offspring2Genes.addAll(parent2Genes.subList(0, crossoverPointGenes));
            offspring2Genes.addAll(parent1Genes.subList(crossoverPointGenes, parent1Genes.size()));
            offspring2Conditions.addAll(parent2Conditions.subList(0, crossoverPointConditions));
            offspring2Conditions.addAll(parent1Conditions.subList(crossoverPointConditions, parent1Conditions.size()));

            offspring.get(0).variables().set(0, new IntegerPermutationSolution(offspring1Genes, parent1.objectives().length, parent1.constraints().length));
            offspring.get(0).variables().set(1,new IntegerPermutationSolution(offspring1Conditions, parent1.objectives().length, parent1.constraints().length));
            offspring.get(1).variables().set(0,new IntegerPermutationSolution(offspring2Genes, parent1.objectives().length, parent1.constraints().length));
            offspring.get(1).variables().set(1, new IntegerPermutationSolution(offspring2Conditions, parent1.objectives().length, parent1.constraints().length));

        }

        return offspring;
    }
        
    @Override
    public double crossoverProbability() {
        return this.crossoverProbability;
    }

    public void setCrossoverProbability(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
      }

    @Override
    public int numberOfRequiredParents() {
        return 2;
    }

    @Override
    public int numberOfGeneratedChildren() {
        return 2;
    }

}
