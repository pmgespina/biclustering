package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.errorchecking.Check;


public class IntegerBiclusterCrossover implements CrossoverOperator<CompositeSolution> {

    private double crossoverProbability;
    private Random crossoverRandomGenerator;
    private Random pointRandomGenerator;

    public IntegerBiclusterCrossover(double crossoverProbability) {
        this(crossoverProbability, new Random(), new Random());
    }

    public IntegerBiclusterCrossover(double crossoverProbability, Random crossRandomGenerator, Random pointRandomGenerator) {
        this.crossoverProbability = crossoverProbability;
        this.crossoverRandomGenerator = crossRandomGenerator;
        this.pointRandomGenerator = pointRandomGenerator;
    }

    @Override
    public List<CompositeSolution> execute(List<CompositeSolution> source) {
        /* Lo que se pasa como parametro es una lista de listas, porque IntegerCompositeSolution
         * a su vez tendrá dos listas de IntegerPermutationSolutions, representando a los genes
         * y a las condiciones respectivamente
         */
        Check.notNull(source);
        Check.that(source.size() == 2, "There must be two parents instead of " + source.size());
        Check.that(source.get(0).variables().get(0).variables().size() == source.get(1).variables().get(0).variables().size(), "The parents should have the same size");
        Check.that(source.get(0).variables().get(1).variables().size() == source.get(1).variables().get(1).variables().size(), "The parents should have the same size");

        CompositeSolution parent1 = source.get(0);
        CompositeSolution parent2 = source.get(1);

        List<CompositeSolution> offspring = new ArrayList<>();
        offspring.add((CompositeSolution) parent1.copy());
        offspring.add((CompositeSolution) parent2.copy());

        if (this.crossoverRandomGenerator.nextDouble() <= this.crossoverProbability) {
            /*
             * Define the solutions that will contain the genes and conditions for the parents
             * We will have to access the lists in the solutions using variables() later
             */
            IntegerSolution parentGenes1 = (IntegerSolution) parent1.variables().get(0);
            IntegerSolution parentGenes2 = (IntegerSolution) parent2.variables().get(0);
            IntegerSolution parentConditions1 = (IntegerSolution) parent1.variables().get(1);
            IntegerSolution parentConditions2 = (IntegerSolution) parent2.variables().get(1);

            /* To ensure that crossover will happen we establish these indexes in the ints operator */
            int crossoverPointGenes = this.pointRandomGenerator.ints(1, parentGenes1.variables().size() - 1).findFirst().getAsInt();
            int crossoverPointConditions = this.pointRandomGenerator.ints(1, parentConditions1.variables().size() - 1).findFirst().getAsInt();

            List<Integer> offspringGenes1 = new ArrayList<>()
                , offspringGenes2 = new ArrayList<>(), offspringConditions1 = new ArrayList<>()
                , offspringConditions2 = new ArrayList<>();

            offspringGenes1.addAll(parentGenes1.variables().subList(0, crossoverPointGenes));
            offspringGenes1.addAll(parentGenes2.variables().subList(crossoverPointGenes, parentGenes2.variables().size()));
            offspringConditions1.addAll(parentConditions1.variables().subList(0, crossoverPointConditions));
            offspringConditions1.addAll(parentConditions2.variables().subList(crossoverPointConditions, parentConditions2.variables().size()));

            offspringGenes2.addAll(parentGenes2.variables().subList(0, crossoverPointGenes));
            offspringGenes2.addAll(parentGenes1.variables().subList(crossoverPointGenes, parentGenes1.variables().size()));
            offspringConditions2.addAll(parentConditions2.variables().subList(0, crossoverPointConditions));
            offspringConditions2.addAll(parentConditions1.variables().subList(crossoverPointConditions, parentConditions1.variables().size()));

            /* We get the gene bounds in order to create the Integersolution that will be stored in the offspring. 
             * Like this we will ensure that the bounds are preserved from the parents to the offspring.
             * The bounds in the parent number one have to be the same as the bounds in parent number two for genes.
             * The same happens with the bounds for the conditions in both parents.
             */
            List<Bounds<Integer>> genesBounds = new ArrayList<>();
            List<Bounds<Integer>> conditionsBounds = new ArrayList<>();
            for (int i = 0; i < parentGenes1.variables().size(); i++) {
                genesBounds.add(parentGenes1.getBounds(i));
            }
            for (int j = 0; j < parentConditions1.variables().size(); j++) {
                conditionsBounds.add(parentConditions1.getBounds(j));
            }

            offspring.get(0).variables().set(0, new DefaultIntegerSolution(genesBounds, parent1.objectives().length, parent1.constraints().length));
            List<Integer> auxListA = (List<Integer>) offspring.get(0).variables().get(0).variables();
            auxListA.clear();
            auxListA.addAll(offspringGenes1);

            offspring.get(0).variables().set(1,new DefaultIntegerSolution(conditionsBounds, parent1.objectives().length, parent1.constraints().length));
            List<Integer> auxListB = (List<Integer>) offspring.get(0).variables().get(1).variables();
            auxListB.clear();
            auxListB.addAll(offspringConditions1);

            offspring.get(1).variables().set(0,new DefaultIntegerSolution(genesBounds, parent1.objectives().length, parent1.constraints().length));
            List<Integer> auxListC = (List<Integer>) offspring.get(1).variables().get(0).variables();
            auxListC.clear();
            auxListC.addAll(offspringGenes2);

            offspring.get(1).variables().set(1, new DefaultIntegerSolution(conditionsBounds, parent1.objectives().length, parent1.constraints().length));
            List<Integer> auxListD = (List<Integer>) offspring.get(1).variables().get(1).variables();
            auxListD.clear();
            auxListD.addAll(offspringConditions2);

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
