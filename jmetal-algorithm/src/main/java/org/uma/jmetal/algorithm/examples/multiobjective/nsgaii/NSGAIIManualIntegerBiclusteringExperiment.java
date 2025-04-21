package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.crossover.impl.IntegerBiclusterCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerBiclusterMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.RandomSelection;
import org.uma.jmetal.problem.multiobjective.MultiIntegerBiclustering;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import static org.uma.jmetal.util.genedataloader.DataLoader.CSVtoDoubleMatrix;

public class NSGAIIManualIntegerBiclusteringExperiment extends AbstractAlgorithmRunner {
    public static void main(String[] args) throws IOException {
        int numRuns = 25;  // Number of times to run the algorithm
        List<List<CompositeSolution>> paretoFronts = new ArrayList<>();

        double[][] matrix = CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x100.csv");
        matrix = NormalizeUtils.normalize(matrix);

        // Problem configuration
        MultiIntegerBiclustering problem = new MultiIntegerBiclustering(matrix);
        double crossoverProbability = 0.9;
        double mutationProbability = 0.5;

        IntegerBiclusterCrossover crossover = new IntegerBiclusterCrossover(crossoverProbability, 0.15);
        IntegerBiclusterMutation mutation = new IntegerBiclusterMutation(mutationProbability);
        SelectionOperator<List<CompositeSolution>, CompositeSolution> selection = new RandomSelection<>();

        // Define the reference point for hypervolume calculation
        double[] referencePoint = {0.005, -0.05, -0.005};

        int populationSize = 100;
        double bestHypervolume = Double.NEGATIVE_INFINITY;
        List<CompositeSolution> bestParetoFront = null;

        // Run the NSGA-II algorithm numRuns times
        for (int run = 0; run < numRuns; run++) {
            Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<>(problem, crossover, mutation, populationSize)
                    .setMaxEvaluations(25000)
                    .setSelectionOperator(selection)
                    .build();

            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

            List<CompositeSolution> result = algorithm.result();

            // Filter all the solutions in result to store the non-dominated solutions (Pareto front) from each run
            NonDominatedSolutionListArchive<CompositeSolution> archive = new NonDominatedSolutionListArchive<>();
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
            String funFileName = "/home/khaosdev/jMetalJava/fabia100x100/NSGAIIManualIntegerExperiment/FUN_run_" + (run + 1) + ".csv";
            String varFileName = "/home/khaosdev/jMetalJava/fabia100x100/NSGAIIManualIntegerExperiment/VAR_run_" + (run + 1) + ".csv";

            new SolutionListOutput(result)
                .setFunFileOutputContext(new DefaultFileOutputContext(funFileName))
                .setVarFileOutputContext(new DefaultFileOutputContext(varFileName))
                .print();

            JMetalLogger.logger.info("Run " + (run + 1) + " completed. Hypervolume: " + currentHypervolume);
        }

        // Output the best Pareto front based on hypervolume
        if (bestParetoFront != null) {
            new SolutionListOutput(bestParetoFront)
                .setFunFileOutputContext(new DefaultFileOutputContext("/home/khaosdev/jMetalJava/fabia100x100/NSGAIIManualIntegerExperiment/BEST_FUN.csv"))
                .setVarFileOutputContext(new DefaultFileOutputContext("/home/khaosdev/jMetalJava/fabia100x100/NSGAIIManualIntegerExperiment/BEST_VAR.csv"))
                .print();

            JMetalLogger.logger.info("Best hypervolume: " + bestHypervolume);
            JMetalLogger.logger.info("Best Pareto front has been saved to BEST_FUN.csv and BEST_VAR.csv");
        }
    }
}
