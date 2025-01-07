package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    private double duplicatesProbability;
    private Random randomGenerator;

    public IntegerBiclusterCrossover(double crossoverProbability) {
        this(crossoverProbability, 1, new Random());
    }

    public IntegerBiclusterCrossover(double crossoverProbability, double duplicatesProbability) {
        this (crossoverProbability, duplicatesProbability, new Random());
    }

    public IntegerBiclusterCrossover(double crossoverProbability, double duplicatesProbability, Random random) {
        this.crossoverProbability = crossoverProbability;
        this.duplicatesProbability = duplicatesProbability;
        this.randomGenerator = random;
    }

    @Override
    public List<CompositeSolution> execute(List<CompositeSolution> source) {
        /* Lo que se pasa como parametro es una lista de listas, porque CompositeSolution
         * a su vez tendrá dos listas de IntegerSolution, representando a los genes
         * y a las condiciones respectivamente
         */
        Check.notNull(source);
        Check.that(source.size() == 2, "There must be two parents instead of " + source.size());
        Check.that(source.get(0).variables().get(0).variables().size() == source.get(1).variables().get(0).variables().size(), "The parents should have the same size");
        Check.that(source.get(0).variables().get(1).variables().size() == source.get(1).variables().get(1).variables().size(), "The parents should have the same size");

        CompositeSolution parent1 = source.get(0);
        CompositeSolution parent2 = source.get(1);

        List<CompositeSolution> offspring = new ArrayList<>(2);
        offspring.add((CompositeSolution) parent1.copy());
        offspring.add((CompositeSolution) parent2.copy());

        if (this.randomGenerator.nextDouble() <= this.crossoverProbability) {
            /*
             * Define the solutions that will contain the genes and conditions for the parents
             * We will have to access the lists in the solutions using variables() later
             */
            IntegerSolution parentGenes1 = (IntegerSolution) parent1.variables().get(0);
            IntegerSolution parentGenes2 = (IntegerSolution) parent2.variables().get(0);
            IntegerSolution parentConditions1 = (IntegerSolution) parent1.variables().get(1);
            IntegerSolution parentConditions2 = (IntegerSolution) parent2.variables().get(1);

            Check.that(parentGenes1.variables().size() == parentGenes2.variables().size(), "Gene index lists must have the same length");
            Check.that(parentConditions1.variables().size() == parentConditions2.variables().size(), "Condition index lists must have the same length");

            /* To ensure that crossover will happen we establish these indexes in the ints operator */
            int crossoverPointGenes = this.randomGenerator.ints(1, parentGenes1.variables().size() - 1).findFirst().getAsInt();
            int crossoverPointConditions = this.randomGenerator.ints(1, parentConditions1.variables().size() - 1).findFirst().getAsInt();

            List<Integer> offspringGenes1 = new ArrayList<>()
                , offspringGenes2 = new ArrayList<>(), offspringConditions1 = new ArrayList<>()
                , offspringConditions2 = new ArrayList<>();

            // Tenemos que asegurarnos de que no haya ningun elemento repetido en la misma lista de genes o de condiciones
            // Tambien de que la lista de genes sea de longitud numGenes y de que la lista de condiciones sea numConditions
            offspringGenes1.addAll(parentGenes1.variables().subList(0, crossoverPointGenes));
            offspringGenes1.addAll(parentGenes2.variables().subList(crossoverPointGenes, parentGenes2.variables().size()));
            Check.that(offspringGenes1.size() == parentGenes1.variables().size(), "The parentGenes1 and the offspringGenes1 must have the same length");
            replaceDuplicates(offspringGenes1);

            offspringConditions1.addAll(parentConditions1.variables().subList(0, crossoverPointConditions));
            offspringConditions1.addAll(parentConditions2.variables().subList(crossoverPointConditions, parentConditions2.variables().size()));
            Check.that(offspringConditions1.size() == parentConditions1.variables().size(), "The parentConditions1 and the offspringConditions1 must have the same length");
            replaceDuplicates(offspringGenes2);

            offspringGenes2.addAll(parentGenes2.variables().subList(0, crossoverPointGenes));
            offspringGenes2.addAll(parentGenes1.variables().subList(crossoverPointGenes, parentGenes1.variables().size()));
            Check.that(offspringGenes2.size() == parentGenes2.variables().size(), "The parentGenes2 and the offspringGenes2 must have the same length");
            replaceDuplicates(offspringConditions1);

            offspringConditions2.addAll(parentConditions2.variables().subList(0, crossoverPointConditions));
            offspringConditions2.addAll(parentConditions1.variables().subList(crossoverPointConditions, parentConditions1.variables().size()));
            Check.that(offspringConditions2.size() == parentConditions2.variables().size(), "The parentConditions2 and the offspringConditions2 must have the same length");
            replaceDuplicates(offspringConditions2);

            Collections.sort(offspringGenes1);
            Collections.sort(offspringGenes2);
            Collections.sort(offspringConditions1);
            Collections.sort(offspringConditions2);

            /* We get the gene bounds in order to create the IntegerSolution that will be stored in the offspring. 
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

    private void replaceDuplicates(List<Integer> list) {
        HashSet<Integer> seen = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            int element = list.get(i);
            if (element != -1 && !seen.add(element)) {
                if (this.randomGenerator.nextDouble() <= this.duplicatesProbability) {
                    list.set(i, -1); // Replace the duplicate with -1
                } else {
                    list.set(i, missingElement(list)); // Replace the duplicate with an element that is not present in the list
                }
            }
        }
    }

    private Integer missingElement(List<Integer> list) {
        HashSet<Integer> set = new HashSet<>(list);

        for (int i = 0; i < list.size(); i++) {
            if (!set.contains(i)) {
                return i;
            }
        }

        return null;
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
