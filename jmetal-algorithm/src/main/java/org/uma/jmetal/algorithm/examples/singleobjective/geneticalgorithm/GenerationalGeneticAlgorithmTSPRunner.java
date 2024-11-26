package org.uma.jmetal.algorithm.examples.singleobjective.geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.TwoPointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PermutationSwapMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;
import org.uma.jmetal.problem.singleobjective.IntegerBiclustering;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.genedataloader.GeneDataLoader;

/**
 * Class to configure and run a generational genetic algorithm. The target problem is TSP.
 *
 * @author Antonio J. Nebro
 */
public class GenerationalGeneticAlgorithmTSPRunner {
  /**
   * Usage: java org.uma.jmetal.runner.singleobjective.BinaryGenerationalGeneticAlgorithmRunner
   */
  public static void main(String[] args) throws Exception {
    PermutationProblem<PermutationSolution<Integer>> problem;
    Algorithm<PermutationSolution<Integer>> algorithm;
    CrossoverOperator<PermutationSolution<Integer>> crossover;
    MutationOperator<PermutationSolution<Integer>> mutation;
    SelectionOperator<List<PermutationSolution<Integer>>, PermutationSolution<Integer>> selection;

    double[][] matrix = GeneDataLoader.loadGeneExpressionMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x100.csv");
    matrix = NormalizeUtils.normalize(matrix);

    problem = new IntegerBiclustering(matrix) ;

    crossover = new TwoPointCrossover(0.9) ;

    double mutationProbability = 0.8 ;
    mutation = new PermutationSwapMutation<Integer>(mutationProbability) ;

    selection = new BinaryTournamentSelection<PermutationSolution<Integer>>(new RankingAndCrowdingDistanceComparator<PermutationSolution<Integer>>());

    algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
            .setPopulationSize(100)
            .setMaxEvaluations(5000)
            .setSelectionOperator(selection)
            .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
            .execute() ;

    PermutationSolution<Integer> solution = algorithm.result() ;
    List<PermutationSolution<Integer>> population = new ArrayList<>(1) ;
    population.add(solution) ;

    long computingTime = algorithmRunner.getComputingTime() ;

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
            .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

  }
}
