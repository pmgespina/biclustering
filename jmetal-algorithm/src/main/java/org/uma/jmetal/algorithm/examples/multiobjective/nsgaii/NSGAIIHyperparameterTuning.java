package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.IntegerBiclusterCrossover;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.mutation.impl.IntegerBiclusterMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.RandomSelection;
import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.problem.multiobjective.MultiBinaryBiclustering;
import org.uma.jmetal.problem.multiobjective.MultiIntegerBiclustering;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.SolutionListUtils;
import static org.uma.jmetal.util.genedataloader.DataLoader.CSVtoDoubleMatrix;

public class NSGAIIHyperparameterTuning {

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.err.println("Usage: NSGAIIBiclusterRunner <encoding>");
            System.err.println("<encoding> must be 'binary' or 'integer'.");
            System.exit(1);
        }

        double[][] matrix = CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x100.csv");
        matrix = NormalizeUtils.normalize(matrix);

        String encoding = args[0];

        BinaryProblem binaryProblem = null;
        MultiIntegerBiclustering integerProblem = null;

        switch (encoding.toLowerCase()) {
        case "binary":
            binaryProblem = new MultiBinaryBiclustering(matrix);
            break;

        case "integer":
            integerProblem = new MultiIntegerBiclustering(matrix);
            break;

        default:
            System.err.println("Invalid encoding type. Must be 'binary' or 'integer'.");
            System.exit(1);
        }

        // Define ranges of hyperparameters
        double[] crossoverProbabilities = {0.1, 0.5, 0.9};
        double[] replicatesProbabilities = {0.1, 0.5, 0.9};
        double[] mutationProbabilities = {0.001, 0.01};
        int populationSize = 100;
        int[] maxEvaluationsList = {100000, 500000, 1000000};

        // Store results
        List<Map<String, Object>> results = new ArrayList<>();

        if (encoding.equals("binary")) {
            double [][] referenceFront = CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/fabia100x100/NSGAIIReferenceFrontBinaryExperiment/NSGAIIComputingReferenceParetoFrontsStudy/referenceFronts/Multi Objective. Binary Encoding Biclustering.csv");
            for (double crossoverProbability : crossoverProbabilities) {
                for (double mutationProbability : mutationProbabilities) {
                    for (int maxEvaluations : maxEvaluationsList) {
                        // Configure the operators
                        CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(crossoverProbability);
                        MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationProbability);
                        SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<>();
    
                        // Build and run the algorithm
                        Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(binaryProblem, crossover, mutation, populationSize)
                                .setSelectionOperator(selection)
                                .setMaxEvaluations(maxEvaluations)
                                .build();
    
                        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                        List<BinarySolution> population = SolutionListUtils.getNonDominatedSolutions(algorithm.result());
    
                        // Compute hypervolume
                        PISAHypervolume hypervolume = new PISAHypervolume(referenceFront);
                        double[][] front = population.stream()
                            .map(solution -> IntStream.range(0, solution.objectives().length)
                                                    .mapToDouble(i -> solution.objectives()[i])
                                                    .toArray())
                            .toArray(double[][]::new);
                        double hv = hypervolume.compute(front);
    
                        // Store result
                        results.add(Map.of(
                                "crossoverProbability", crossoverProbability,
                                "mutationProbability", mutationProbability,
                                "maxEvaluations", maxEvaluations,
                                "hypervolume", hv
                        ));
    
                        System.out.println("Evaluated config: " + results.get(results.size() - 1));
                    }
                }
            }
        } else if (encoding.equals("integer")) {
            double [][] referenceFront = CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/fabia100x100/NSGAIIReferenceFrontIntegerExperiment/NSGAIIComputingReferenceParetoFrontsStudy/referenceFronts/Multi Objective Integer Encoding Biclustering.csv");
            for (double crossoverProbability : crossoverProbabilities) {
                for (double replicatesProbability : replicatesProbabilities) {
                    for (double mutationProbability : mutationProbabilities) {
                        for (int maxEvaluations : maxEvaluationsList) {
                            // Configure the operators
                            IntegerBiclusterCrossover crossover = new IntegerBiclusterCrossover(crossoverProbability, replicatesProbability);
                            IntegerBiclusterMutation mutation = new IntegerBiclusterMutation(mutationProbability);
                            SelectionOperator<List<CompositeSolution>, CompositeSolution> selection = new RandomSelection<>();
        
                            // Build and run the algorithm
                            Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<>(integerProblem, crossover, mutation, populationSize)
                                    .setSelectionOperator(selection)
                                    .setMaxEvaluations(maxEvaluations)
                                    .build();
        
                            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                            List<CompositeSolution> population = SolutionListUtils.getNonDominatedSolutions(algorithm.result());
        
                            // Compute hypervolume
                            PISAHypervolume hypervolume = new PISAHypervolume(referenceFront);
                            double[][] front = population.stream()
                                .map(solution -> IntStream.range(0, solution.objectives().length)
                                                        .mapToDouble(i -> solution.objectives()[i])
                                                        .toArray())
                                .toArray(double[][]::new);
                            double hv = hypervolume.compute(front);
        
                            // Store result
                            results.add(Map.of(
                                    "crossoverProbability", crossoverProbability,
                                    "replicatesProbability", replicatesProbability,
                                    "mutationProbability", mutationProbability,
                                    "maxEvaluations", maxEvaluations,
                                    "hypervolume", hv
                            ));
        
                            System.out.println("Evaluated config: " + results.get(results.size() - 1));
                        }
                    }
                }
            }
        }

        // Print or save results
        results.forEach(System.out::println);
    }
}
