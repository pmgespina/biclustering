package org.uma.jmetal.lab.experiment.studies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import org.uma.jmetal.lab.experiment.component.impl.GenerateBoxplotsWithR;
import org.uma.jmetal.lab.experiment.component.impl.GenerateFriedmanHolmTestTables;
import org.uma.jmetal.lab.experiment.component.impl.GenerateHtmlPages;
import org.uma.jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.lab.experiment.component.impl.GenerateReferenceParetoFront;
import org.uma.jmetal.lab.experiment.component.impl.GenerateWilcoxonTestTablesWithR;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.operator.crossover.impl.IntegerBiclusterCrossover;
import org.uma.jmetal.operator.mutation.impl.IntegerBiclusterMutation;
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
 * This experiment assumes that the reference Pareto front are not known, so the names of files
 * containing them and the directory where they are located must be specified.
 * <p>
 * Five quality indicators are used for performance assessment.
 * <p>
 * The steps to carry out the experiment are:
 * 1. Configure the experiment
 * 2. Execute the algorithms
 * 3. Generate the reference Pareto fronts
 * 4. Compute the quality indicators
 * 5. Generate Latex tables reporting means and medians
 * 6. Generate Latex tables with the result of applying the Wilcoxon Rank Sum Test
 * 7. Generate Latex tables with the ranking obtained by applying the Friedman test 8. Generate R scripts to obtain boxplots
 * 8. Generate HTML pages
 *
 * @author Antonio J. Nebro
 */
public class NSGAIIComputingReferenceParetoFrontsIntegerStudy {

  private static final int INDEPENDENT_RUNS = 25;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      throw new JMetalException("Needed arguments: experimentBaseDirectory");
    }
    String experimentBaseDirectory = args[0];

    double[][] matrix = DataLoader.CSVtoDoubleMatrix("/home/khaosdev/jMetalJava/jMetal/resources/fabia_100x1000.csv");
    matrix = NormalizeUtils.normalize(matrix);

    List<ExperimentProblem<CompositeSolution>> problemList = new ArrayList<>();
    problemList.add(new ExperimentProblem<>(new MultiIntegerBiclustering(matrix)));

    List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> algorithmList =
            configureAlgorithmList(problemList);

    Experiment<CompositeSolution, List<CompositeSolution>> experiment =
            new ExperimentBuilder<CompositeSolution, List<CompositeSolution>>("NSGAIIComputingReferenceParetoFrontsIntegerStudy")
                    .setAlgorithmList(algorithmList)
                    .setProblemList(problemList)
                    .setExperimentBaseDirectory(experimentBaseDirectory)
                    .setOutputParetoFrontFileName("FUN")
                    .setOutputParetoSetFileName("VAR")
                    .setReferenceFrontDirectory(experimentBaseDirectory + "/NSGAIIComputingReferenceParetoFrontsIntegerStudy/referenceFronts")
                    .setIndicatorList(Arrays.asList(
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

    new ExecuteAlgorithms<>(experiment).run();
    new GenerateReferenceParetoFront(experiment).run(); // this changes from BinarySolution to CompositeSolution because the solution representation is different
    new ComputeQualityIndicators<>(experiment).run();
    new GenerateLatexTablesWithStatistics(experiment).run();
    new GenerateFriedmanHolmTestTables<>(experiment).run();
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(3).setColumns(2).run();
    new GenerateHtmlPages<>(experiment).run() ;
  }


  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}. The {@link
   * ExperimentAlgorithm} has an optional tag component, that can be set as it is shown in this
   * example, where four variants of a same algorithm are defined.
   */
  static List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> configureAlgorithmList(
          List<ExperimentProblem<CompositeSolution>> problemList) {
    List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> algorithms = new ArrayList<>();
    for (int run = 0; run < INDEPENDENT_RUNS; run++) {
      for (ExperimentProblem<CompositeSolution> experimentProblem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<CompositeSolution>(
                experimentProblem.getProblem(),
                new IntegerBiclusterCrossover(1.0),
                new IntegerBiclusterMutation(1.0 / experimentProblem.getProblem().numberOfVariables()),
                100)
                .setMaxEvaluations(25000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAIIa", experimentProblem, run));
      }


      for (ExperimentProblem<CompositeSolution> experimentProblem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<CompositeSolution>(
                experimentProblem.getProblem(),
                new IntegerBiclusterCrossover(1.0),
                new IntegerBiclusterMutation(1.0 / experimentProblem.getProblem().numberOfVariables()),
                100)
                .setMaxEvaluations(25000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAIIb", experimentProblem, run));
      }


      for (ExperimentProblem<CompositeSolution> experimentProblem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<CompositeSolution>(
                experimentProblem.getProblem(), 
                new IntegerBiclusterCrossover(1.0),
                new IntegerBiclusterMutation(1.0 / experimentProblem.getProblem().numberOfVariables()),
                100)
                .setMaxEvaluations(25000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAIIc", experimentProblem, run));
      }


      for (ExperimentProblem<CompositeSolution> experimentProblem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<CompositeSolution>(
                experimentProblem.getProblem(), 
                new IntegerBiclusterCrossover(1.0),
                new IntegerBiclusterMutation(1.0 / experimentProblem.getProblem().numberOfVariables()),
                100)
                .setMaxEvaluations(25000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, "NSGAIId", experimentProblem, run));
      }
    }
    return algorithms;
  }
}