package org.uma.jmetal.solution.compositesolution;

import org.uma.jmetal.solution.Solution;

/**
 * Interface representing solutions composed of a list of solutions. The idea is that each decision
 * variable can be a solution of any type, so we can create mixed solutions (e.g., solutions
 * combining any of the existing encodings).
 *
 * The adopted approach has the advantage of easing the reuse of existing variation operators,
 * but all the solutions in the list will need to have the same function and constraint violation
 * values.
 *
 * It is assumed that problems using instances of this class will properly manage the solutions it contains.
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public interface  CompositeSolution<T> extends Solution<T> {
  
}
