package org.uma.jmetal.problem.compositeproblem;

// import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;

/**
 * Interface representing composite problems (lists of solutions of a certain type)
 *
 * @author Pablo Moreno García-Espina
 */

public interface CompositeProblem<S extends CompositeSolution<?>> extends Problem<S> {
    // List<Integer> numberOfElementsInList();
}
