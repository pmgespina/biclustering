package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import static org.uma.jmetal.util.genedataloader.GeneDataLoader.loadGeneExpressionMatrix;

public class NSGAIIManualBinaryBiclusteringExperiment extends AbstractAlgorithmRunner {
    public static void main(String[] args) throws IOException {
        int numRuns = 25;  // Number of times to run the algorithm
        List<List<BinarySolution>> paretoFronts = new ArrayList<>();

        double[][] matrix = loadGeneExpressionMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x1000.csv");
        matrix = NormalizeUtils.normalize(matrix);

        // Problem configuration
        BinaryProblem problem = new MultiBinaryBiclustering(matrix);
        double crossoverProbability = 0.9;
        double mutationProbability = 0.5;

        CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(crossoverProbability);
        MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationProbability);
        SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<>();

        // Define the reference point for hypervolume calculation
        double[] referencePoint = {0.00883, -0.4275, -0.0307};

        int populationSize = 100;
        double bestHypervolume = Double.NEGATIVE_INFINITY;
        List<BinarySolution> bestParetoFront = null;

        // Run the NSGA-II algorithm numRuns times
        for (int run = 0; run < numRuns; run++) {
            Algorithm<List<BinarySolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                    .setMaxEvaluations(25000)
                    .setSelectionOperator(selection)
                    .build();

            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

            List<BinarySolution> result = algorithm.result();

            // Filter all the solutions in result to store the non-dominated solutions (Pareto front) from each run
            NonDominatedSolutionListArchive<BinarySolution> archive = new NonDominatedSolutionListArchive<>();
            result.forEach(archive::add);
            paretoFronts.add(archive.solutions());

            // Calculate hypervolume for the current run by extracting the objectives of my run in order to build the necessary front
            PISAHypervolume hypervolume = new PISAHypervolume(referencePoint);
            double[][] front = archive.solutions().stream()
                .map(solution -> IntStream.range(0, solution.objectives().length)
                                        .mapToDouble(i -> solution.objectives()[i])
                                        .toArray())
                .toArray(double[][]::new);
            double currentHypervolume = hypervolume.compute(front);

            // Update best Pareto front based on hypervolume
            if (currentHypervolume > bestHypervolume) {
                bestHypervolume = currentHypervolume;
                bestParetoFront = new ArrayList<>(archive.solutions());
            }

            // Output the results of each run to a FUN and VAR file
            String funFileName = "/home/khaosdev/jMetalJava/fabia100x1000/NSGAIIManualBinaryExperiment/FUN_run_" + (run + 1) + ".csv";
            String varFileName = "/home/khaosdev/jMetalJava/fabia100x1000/NSGAIIManualBinaryExperiment/VAR_run_" + (run + 1) + ".csv";

            new SolutionListOutput(result)
                .setFunFileOutputContext(new DefaultFileOutputContext(funFileName))
                .setVarFileOutputContext(new DefaultFileOutputContext(varFileName))
                .print();

            JMetalLogger.logger.info("Run " + (run + 1) + " completed. Hypervolume: " + currentHypervolume);
        }

        // Output the best Pareto front based on hypervolume
        if (bestParetoFront != null) {
            new SolutionListOutput(bestParetoFront)
                .setFunFileOutputContext(new DefaultFileOutputContext("/home/khaosdev/jMetalJava/fabia100x1000/NSGAIIManualBinaryExperiment/BEST_FUN.csv"))
                .setVarFileOutputContext(new DefaultFileOutputContext("/home/khaosdev/jMetalJava/fabia100x1000/NSGAIIManualBinaryExperiment/BEST_VAR.csv"))
                .print();

            JMetalLogger.logger.info("Best hypervolume: " + bestHypervolume);
            JMetalLogger.logger.info("Best Pareto front has been saved to BEST_FUN.csv and BEST_VAR.csv");
        }
    }
}
