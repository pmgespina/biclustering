package org.uma.jmetal.algorithm.examples.singleobjective.geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerBiclusterCrossover;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.mutation.impl.IntegerBiclusterMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.singleobjective.IntegerBiclustering;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.IntegerCompositeSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.genedataloader.GeneDataLoader;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;
/**
 * Class to configure and run a generational genetic algorithm. The target problem is OneMax.
 *
 * @author Antonio J. Nebro
 */
public class GenerationalGeneticAlgorithmIntegerBiclusterRunner {
  /**
   * Usage: java org.uma.jmetal.runner.singleobjective.GenerationalGeneticAlgorithmBinaryEncodingRunner
   */
  public static void main(String[] args) throws Exception {
    IntegerBiclustering problem;
    Algorithm<IntegerCompositeSolution> algorithm;
    IntegerBiclusterCrossover crossover;
    IntegerBiclusterMutation mutation;
    SelectionOperator<List<BinarySolution>, BinarySolution> selection;

    double[][] matrix = GeneDataLoader.loadGeneExpressionMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x1000.csv");
    matrix = NormalizeUtils.normalize(matrix);

    problem = new IntegerBiclustering(matrix) ;

    crossover = new IntegerBiclusterCrossover(0.9, new RandomGenerator<Double>(), new RandomGenerator<Integer>()) ;

    double mutationProbability = 0.4 ;
    mutation = new BitFlipMutation(mutationProbability) ;

    selection = new BinaryTournamentSelection<>();

    algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
            .setPopulationSize(100)
            .setMaxEvaluations(25000)
            .setSelectionOperator(selection)
            .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
            .execute() ;

    BinarySolution solution = algorithm.result() ;
    List<BinarySolution> population = new ArrayList<>(1) ;
    population.add(solution) ;

    long computingTime = algorithmRunner.getComputingTime() ;

    new SolutionListOutput(population)
            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
            .print();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

    JMetalLogger.logger.info("Fitness: " + solution.objectives()[0]) ;
    JMetalLogger.logger.info("Solution: " + solution.variables().get(0)) ;
  }
}