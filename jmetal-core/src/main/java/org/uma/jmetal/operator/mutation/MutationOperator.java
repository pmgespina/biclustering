package org.uma.jmetal.operator.mutation;

import org.uma.jmetal.operator.Operator;

/**
 * Interface representing mutation operators
 *
 * @author Antonio J. Nebro 
 *
 * @param <Source> The solution class of the solution to be mutated
 */
public interface MutationOperator<Source> extends Operator<Source, Source> {
    Source execute(Source solution);
    double mutationProbability() ;
}
