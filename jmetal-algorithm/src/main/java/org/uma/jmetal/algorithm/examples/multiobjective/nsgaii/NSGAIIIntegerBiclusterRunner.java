package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.IntegerBiclusterCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerBiclusterMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.RandomSelection;
import org.uma.jmetal.problem.multiobjective.MultiIntegerBiclustering;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultiThreadedSolutionListEvaluator;
import static org.uma.jmetal.util.genedataloader.GeneDataLoader.loadGeneExpressionMatrix;

/**
 * Class for configuring and running the NSGA-II algorithm (binary encoding)
 *
 * @author Antonio J. Nebro
 */

public class NSGAIIIntegerBiclusterRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {

    
    double[][] matrix = loadGeneExpressionMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x1000.csv");
    matrix = NormalizeUtils.normalize(matrix);

    MultiIntegerBiclustering problem = new MultiIntegerBiclustering(matrix) ;

    double crossoverProbability = 0.9;
    IntegerBiclusterCrossover crossover = new IntegerBiclusterCrossover(crossoverProbability);

    double mutationProbability = 0.2;
    IntegerBiclusterMutation mutation = new IntegerBiclusterMutation(mutationProbability);

    SelectionOperator<List<CompositeSolution>, CompositeSolution> selection = new RandomSelection<>();

    SolutionListEvaluator<CompositeSolution> evaluator =
        new MultiThreadedSolutionListEvaluator<CompositeSolution>(8);

    int populationSize = 100;
    int maxEvaluations = 100000;
    NSGAII<CompositeSolution> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation,
        populationSize)
        .setSelectionOperator(selection)
        .setMaxEvaluations(maxEvaluations)
        .setSolutionListEvaluator(evaluator)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<CompositeSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    evaluator.shutdown();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
