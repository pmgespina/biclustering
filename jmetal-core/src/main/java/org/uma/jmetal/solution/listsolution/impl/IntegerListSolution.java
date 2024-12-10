package org.uma.jmetal.solution.listsolution.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.listsolution.ListSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

public class IntegerListSolution extends AbstractSolution<Integer> implements ListSolution<Integer>{

    public IntegerListSolution (int listLength, int numberOfObjectives) {
        super(listLength, numberOfObjectives);

        for (int i = 0; i < listLength; i++) {
            variables().add(i);
        }

        if (repeated()) { throw new JMetalException("The integer list has repeated elements"); }
    }

    public IntegerListSolution (IntegerListSolution solution) {
        super(solution.getLength(), solution.objectives().length);

        System.arraycopy(solution.objectives(), 0, objectives(), 0, objectives().length);

        for (int i = 0; i < variables().size(); i++) {
            variables().set(i, solution.variables().get(i));
        }

        System.arraycopy(solution.constraints(), 0, constraints(), 0, constraints().length);

        attributes = new HashMap<>(solution.attributes);
    }

    @Override
    public int getLength() {
        return variables().size();
    }

    @Override
    public boolean repeated() {
        /* True for repeated elements, when the set has less elements */
        Set<Integer> set = new HashSet<>(variables());
        return (set.size() < variables().size());
    }

    @Override
    public Solution<Integer> copy() {
        return (new IntegerListSolution(this));
    }

}
