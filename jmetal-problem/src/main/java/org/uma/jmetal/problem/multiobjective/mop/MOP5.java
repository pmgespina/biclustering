package org.uma.jmetal.problem.multiobjective.mop;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.doubleproblem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

/**
 * Problem MOP5. Defined in
 * H. L. Liu, F. Gu and Q. Zhang, "Decomposition of a Multiobjective 
 * Optimization Problem Into a Number of Simple Multiobjective Subproblems,"
 * in IEEE Transactions on Evolutionary Computation, vol. 18, no. 3, pp. 
 * 450-455, June 2014.
 *
 * @author Mastermay <javismay@gmail.com> 	
 */
@SuppressWarnings("serial")
public class MOP5 extends AbstractDoubleProblem {

  /** Constructor. Creates default instance of problem MOP5 (10 decision variables) */
  public MOP5() {
    this(10);
  }

  /**
   * Creates a new instance of problem MOP5.
   *
   * @param numberOfVariables Number of variables.
   */
  public MOP5(Integer numberOfVariables) {
    numberOfObjectives(2);
    name("MOP5");

    List<Double> lowerLimit = new ArrayList<>(numberOfVariables) ;
    List<Double> upperLimit = new ArrayList<>(numberOfVariables) ;

    for (int i = 0; i < numberOfVariables; i++) {
      lowerLimit.add(0.0);
      upperLimit.add(1.0);
    }

    variableBounds(lowerLimit, upperLimit);
  }

  /** Evaluate() method */
  public DoubleSolution evaluate(DoubleSolution solution) {
    double[] f = new double[solution.objectives().length];

    double g = this.evalG(solution);
    f[0] = (1 + g) * solution.variables().get(0);
    f[1] = (1 + g) * (1 - Math.sqrt(solution.variables().get(0)));

    solution.objectives()[0] = f[0];
    solution.objectives()[1] = f[1];
    return solution ;
  }

  /**
   * Returns the value of the MOP5 function G.
   *
   * @param solution Solution
   */
  private double evalG(DoubleSolution solution) {
    double g = 0.0;
    for (int i = 1; i < solution.variables().size(); i++) {
      double t = solution.variables().get(i) - Math.sin(0.5 * Math.PI * solution.variables().get(0));
      g += -0.9 * t * t + Math.pow(Math.abs(t), 0.6);
    }
    g = 2 * Math.abs(Math.cos(Math.PI * solution.variables().get(0))) * g;
    return g;
  }

}
