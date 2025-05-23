package org.uma.jmetal.qualityindicator.impl;

import java.io.FileNotFoundException;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.util.VectorUtils;
import org.uma.jmetal.util.distance.impl.DominanceDistanceBetweenVectors;
import org.uma.jmetal.util.errorchecking.Check;

/**
 * This class implements the inverted generational distance metric plust (IGD+)
 * Reference: Ishibuchi et al 2015, "A Study on Performance Evaluation Ability of a Modified
 * Inverted Generational Distance Indicator", GECCO 2015
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class InvertedGenerationalDistancePlus extends QualityIndicator {

  /**
   * Default constructor
   */
  public InvertedGenerationalDistancePlus() {
  }

  /**
   * Constructor
   *
   * @param referenceFront
   * @throws FileNotFoundException
   */
  public InvertedGenerationalDistancePlus(double[][] referenceFront) {
    super(referenceFront) ;
  }

  /**
   * Evaluate() method
   * @param front
   * @return
   */
  @Override public double compute(double[][] front) {
    Check.notNull(front);

    return invertedGenerationalDistancePlus(front, referenceFront);
  }

  /**
   * Returns the inverted generational distance plus value for a given front
   *
   * @param front The front
   * @param referenceFront The reference pareto front
   */
  public double invertedGenerationalDistancePlus(double[][] front, double[][] referenceFront) {

    double sum = 0.0;
    for (int i = 0 ; i < referenceFront.length; i++) {
      sum += VectorUtils.distanceToClosestVector(referenceFront[i], front, new DominanceDistanceBetweenVectors());
    }

    // STEP 4. Divide the sum by the maximum number of points of the reference Pareto front
    return sum / referenceFront.length;
  }

  @Override public String name() {
    return "IGD+" ;
  }

  @Override public String description() {
    return "Inverted Generational Distance+" ;
  }

  @Override
  public boolean isTheLowerTheIndicatorValueTheBetter() {
    return true ;
  }
}
