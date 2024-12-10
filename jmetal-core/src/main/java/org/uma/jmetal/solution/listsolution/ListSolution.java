package org.uma.jmetal.solution.listsolution;

import org.uma.jmetal.solution.Solution;

/**
 * Interface representing a list of values of the same type
 *
 * @param <T>
 */

public interface ListSolution<T>  extends Solution<T>{
    int getLength();
    boolean repeated();
}
