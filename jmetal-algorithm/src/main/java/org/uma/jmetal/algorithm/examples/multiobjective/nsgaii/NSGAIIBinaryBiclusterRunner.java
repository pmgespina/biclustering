package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.problem.multiobjective.MultiBinaryBiclustering;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.NormalizeUtils;
import static org.uma.jmetal.util.genedataloader.GeneDataLoader.loadGeneExpressionMatrix;

/**
 * Class for configuring and running the NSGA-II algorithm (binary encoding)
 *
 * @author Antonio J. Nebro
 */

public class NSGAIIBinaryBiclusterRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {

    
    double[][] matrix = loadGeneExpressionMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x1000.csv");
    matrix = NormalizeUtils.normalize(matrix);

    BinaryProblem problem = new MultiBinaryBiclustering(matrix) ;

    double crossoverProbability = 0.9;
    CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(crossoverProbability);

    double mutationProbability = 0.2;
    MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationProbability);

    SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<>();

    int populationSize = 100;
    int maxEvaluations = 100000;
    Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation,
        populationSize)
        .setSelectionOperator(selection)
        .setMaxEvaluations(maxEvaluations)
        .build();

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute();

    List<BinarySolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
  }
}
