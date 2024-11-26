package org.uma.jmetal.operator.crossover.impl;

import java.util.List;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.compositesolution.IntegerCompositeSolution;


public class IntegerBiclusterCrossover implements CrossoverOperator<IntegerCompositeSolution> {

    private double crossoverProbability;

    @Override
    public List<IntegerCompositeSolution> execute(List<IntegerCompositeSolution> source) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public double crossoverProbability() {
        return this.crossoverProbability;
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
