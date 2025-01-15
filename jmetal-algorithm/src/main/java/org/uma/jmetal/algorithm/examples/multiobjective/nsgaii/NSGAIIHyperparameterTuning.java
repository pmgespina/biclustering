package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultiThreadedSolutionListEvaluator;
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
        String outputFile = encoding.equals("binary") ? "HyperparameterTuningBin.txt" : "HyperparameterTuningInt.txt";

        try(FileWriter writer = new FileWriter(outputFile)) {
            if (encoding.equals("binary")) {
                double [][] referenceFront = CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/fabia100x100/NSGAIIReferenceFrontBinaryExperiment/NSGAIIComputingReferenceParetoFrontsStudy/referenceFronts/Multi Objective. Binary Encoding Biclustering.csv");
                for (double crossoverProbability : crossoverProbabilities) {
                    for (double mutationProbability : mutationProbabilities) {
                        for (int maxEvaluations : maxEvaluationsList) {
                            // Configure the operators
                            CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(crossoverProbability);
                            MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationProbability);
                            SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<>();

                            SolutionListEvaluator<BinarySolution> evaluator = new MultiThreadedSolutionListEvaluator<BinarySolution>(8);
        
                            // Build and run the algorithm
                            Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(binaryProblem, crossover, mutation, populationSize)
                                    .setSelectionOperator(selection)
                                    .setMaxEvaluations(maxEvaluations)
                                    .setSolutionListEvaluator(evaluator)
                                    .build();
        
                            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                            List<BinarySolution> population = SolutionListUtils.getNonDominatedSolutions(algorithm.result());

                            evaluator.shutdown();
        
                            // Compute hypervolume
                            PISAHypervolume hypervolume = new PISAHypervolume(referenceFront);
                            double[][] front = population.stream()
                                .map(solution -> IntStream.range(0, solution.objectives().length)
                                                        .mapToDouble(i -> solution.objectives()[i])
                                                        .toArray())
                                .toArray(double[][]::new);
                            double hv = hypervolume.compute(front);
        
                            String result = String.format("Configuration: {maxEvaluations=%d, crossoverProbability=%.1f, hypervolume=%.10f, mutationProbability=%.3f}",
                                        maxEvaluations, crossoverProbability, hv, mutationProbability);
                            writer.write(result + "\n");

                            System.out.println("Line written to file: " + result);
                        }
                    }
                }
            } else if (encoding.equals("integer")) {
                double [][] referenceFront = CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/fabia100x100/NSGAIIReferenceFrontIntegerExperiment/NSGAIIComputingReferenceParetoFrontsIntegerStudy/referenceFronts/Multi Objective Integer Encoding Biclustering.csv");
                for (double crossoverProbability : crossoverProbabilities) {
                    for (double replicatesProbability : replicatesProbabilities) {
                        for (double mutationProbability : mutationProbabilities) {
                            for (int maxEvaluations : maxEvaluationsList) {
                                // Configure the operators
                                IntegerBiclusterCrossover crossover = new IntegerBiclusterCrossover(crossoverProbability, replicatesProbability);
                                IntegerBiclusterMutation mutation = new IntegerBiclusterMutation(mutationProbability);
                                SelectionOperator<List<CompositeSolution>, CompositeSolution> selection = new RandomSelection<>();

                                SolutionListEvaluator<CompositeSolution> evaluator = new MultiThreadedSolutionListEvaluator<CompositeSolution>(8);
            
                                // Build and run the algorithm
                                Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<>(integerProblem, crossover, mutation, populationSize)
                                        .setSelectionOperator(selection)
                                        .setMaxEvaluations(maxEvaluations)
                                        .setSolutionListEvaluator(evaluator)
                                        .build();
            
                                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                                List<CompositeSolution> population = SolutionListUtils.getNonDominatedSolutions(algorithm.result());

                                evaluator.shutdown();
            
                                // Compute hypervolume
                                PISAHypervolume hypervolume = new PISAHypervolume(referenceFront);
                                double[][] front = population.stream()
                                    .map(solution -> IntStream.range(0, solution.objectives().length)
                                                            .mapToDouble(i -> solution.objectives()[i])
                                                            .toArray())
                                    .toArray(double[][]::new);
                                double hv = hypervolume.compute(front);
            
                                String result = String.format("Configuration: {maxEvaluations=%d, crossoverProbability=%.1f, hypervolume=%.10f, mutationProbability=%.3f}",
                                maxEvaluations, crossoverProbability, hv, mutationProbability);
                                writer.write(result + "\n");

                                System.out.println("Line written to file: " + result);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Configurations written to file " + outputFile);
    }
}
