package org.uma.jmetal.lab.experiment.studies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import org.uma.jmetal.lab.experiment.component.impl.GenerateBoxplotsWithR;
import org.uma.jmetal.lab.experiment.component.impl.GenerateFriedmanTestTables;
import org.uma.jmetal.lab.experiment.component.impl.GenerateHtmlPages;
import org.uma.jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.lab.experiment.component.impl.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.MultiIntegerBiclustering;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.NormalizedHypervolume;
import org.uma.jmetal.qualityindicator.impl.Spread;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.util.NormalizeUtils;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.genedataloader.DataLoader;

/**
 * Example of experimental study based on solving the ZDT problems with four versions of NSGA-II,
 * each of them applying a different crossover probability (from 0.7 to 1.0).
 * <p>
 * This org.uma.jmetal.experiment assumes that the reference Pareto front are known and that, given
 * a problem named P, there is a corresponding file called P.pf containing its corresponding Pareto
 * front. If this is not the case, please refer to class {@link DTLZStudy} to see an example of how
 * to explicitly indicate the name of those files.
 * <p>
 * Six quality indicators are used for performance assessment.
 * <p>
 * The steps to carry out the org.uma.jmetal.experiment are: 1. Configure experiment 2. Execute
 * algorithms 3. Compute quality indicators 4. Generate Latex tables reporting means and medians 5.
 * Generate Latex tables with the result of applying the Wilcoxon Rank Sum Test 6. Generate Latex
 * tables with the ranking obtained by applying the Friedman test 7. Generate R scripts to obtain
 * boxplots 8. Generate HTML pages with including the above data
 *
 * @author Antonio J. Nebro (ajnebro@uma.es)
 */
public class NSGAIIStudyBiclusteringInteger {

  private static final int INDEPENDENT_RUNS = 25;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new JMetalException("Missing argument: experimentBaseDirectory");
    }
    String experimentBaseDirectory = args[0];

    double[][] matrix = DataLoader.CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x1000.csv");
    matrix = NormalizeUtils.normalize(matrix);

    List<ExperimentProblem<CompositeSolution>> problemList = new ArrayList<>();
    problemList.add(new ExperimentProblem<>(new MultiIntegerBiclustering(matrix)));

    List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> algorithmList =
        configureAlgorithmList(problemList);

    Experiment<CompositeSolution, List<CompositeSolution>> experiment =
        new ExperimentBuilder<CompositeSolution, List<CompositeSolution>>("NSGAIIIntegerExperiment")
            .setAlgorithmList(algorithmList)
            .setProblemList(problemList)
            .setExperimentBaseDirectory(experimentBaseDirectory)
            .setOutputParetoFrontFileName("FUN")
            .setOutputParetoSetFileName("VAR")
            .setReferenceFrontDirectory("/home/khaosdev/jMetalJava/fabia100x1000/NSGAIIReferenceFrontIntegerExperiment2D/NSGAIIComputingReferenceParetoFrontsIntegerStudy/referenceFronts")
            .setIndicatorList(
                List.of(
                    new Epsilon(),
                    new Spread(),
                    new GenerationalDistance(),
                    new PISAHypervolume(),
                    new NormalizedHypervolume(),
                    new InvertedGenerationalDistance(),
                    new InvertedGenerationalDistancePlus()))
            .setIndependentRuns(INDEPENDENT_RUNS)
            .setNumberOfCores(8)
            .build();

    new ExecuteAlgorithms<>(experiment).run(); // First step: Execution of all the configurations
    new ComputeQualityIndicators<>(experiment).run(); // Second step: Apply quality indicators to the obtained fronts. We need a Pareto front per problem.
    new GenerateLatexTablesWithStatistics(experiment).run(); // Third step: Performance assessment by statistical tests and summarizing results
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateFriedmanTestTables<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(2).setColumns(3).run();
    new GenerateHtmlPages<>(experiment).run();
  }

  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}. The
   * {@link ExperimentAlgorithm} has an optional tag component, that can be set as it is shown in
   * this example, where four variants of a same algorithm are defined.
   */
  static List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> configureAlgorithmList(
      List<ExperimentProblem<CompositeSolution>> problemList) {

    List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> algorithms = new ArrayList<>();

    int[] populationSizes = {50, 100, 200};
    double[] crossoverProbabilities = {0.1, 0.5, 0.9};
    double[] mutationProbabilities = {0.001, 0.01, 0.1};
    int[] iterations = {50, 100, 200};

    for (int run = 0; run < INDEPENDENT_RUNS; run++) {
      for (ExperimentProblem<CompositeSolution> problem : problemList) {
        for (int popSize : populationSizes) {
          for (double crossoverProb : crossoverProbabilities) {
            for (double mutationProb : mutationProbabilities) {
              for (int iterationCount : iterations) {

                int maxEvaluations = popSize * iterationCount;

                Algorithm<List<CompositeSolution>> algorithm =
                    new NSGAIIBuilder<>(
                        problem.getProblem(),
                        new SinglePointCrossover(crossoverProb),
                        new BitFlipMutation(mutationProb),
                        popSize)
                        .setMaxEvaluations(maxEvaluations)
                        .build();

                String tag = String.format("NSGAII_P%d_C%.2f_M%.3f_I%d",
                    popSize, crossoverProb, mutationProb, iterationCount);

                algorithms.add(new ExperimentAlgorithm<>(algorithm, tag, problem, run));
              }
            }
          }
        }
      }
    }
    return algorithms;
  }
}
