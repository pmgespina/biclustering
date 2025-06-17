//

//

package org.uma.jmetal.lab.experiment.studies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.mosa.MOSA;
import org.uma.jmetal.algorithm.multiobjective.mosa.cooling.impl.Exponential;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.ExecuteAlgorithms;
import org.uma.jmetal.lab.experiment.component.impl.GenerateBoxplotsWithR;
import org.uma.jmetal.lab.experiment.component.impl.GenerateFriedmanTestTables;
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
import org.uma.jmetal.util.archive.impl.GenericBoundedArchive;
import org.uma.jmetal.util.densityestimator.impl.CrowdingDistanceDensityEstimator;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.genedataloader.DataLoader;

/**
 * Example of experimental study based on solving the multiobjective integer encoding biclustering problem with three algorithms: NSGAII,
 * SPEA2 and MOCell
 *
 * This org.uma.jmetal.experiment assumes that the reference Pareto front are not known, so the must be produced.
 *
 * Six quality indicators are used for performance assessment.
 *
 * The steps to carry out the org.uma.jmetal.experiment are: 1. Configure the org.uma.jmetal.experiment 2. Execute the algorithms
 * 3. Generate the reference Pareto fronts 4. Compute que quality indicators 5. Generate Latex
 * tables reporting means and medians 6. Generate Latex tables with the result of applying the
 * Wilcoxon Rank Sum Test 7. Generate Latex tables with the ranking obtained by applying the
 * Friedman test 8. Generate R scripts to obtain boxplots
 *
 * @author Antonio J. Nebro
 */
public class IntegerProblemsStudy {

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

    Experiment<CompositeSolution, List<CompositeSolution>> experiment;
    experiment = new ExperimentBuilder<CompositeSolution, List<CompositeSolution>>("IntegerProblemsStudy")
        .setAlgorithmList(algorithmList)
        .setProblemList(problemList)
        .setExperimentBaseDirectory(experimentBaseDirectory)
        .setOutputParetoFrontFileName("FUN")
        .setOutputParetoSetFileName("VAR")
        .setReferenceFrontDirectory("/home/khaosdev/jMetalJava/fabia100x1000/NSGAIIReferenceFrontIntegerExperiment/NSGAIIComputingReferenceParetoFrontsIntegerStudy/referenceFronts")
        .setIndicatorList(Arrays.asList(
            new Epsilon(),
            new Spread(),
            new GenerationalDistance(),
            new PISAHypervolume(),
                new NormalizedHypervolume(),
                new InvertedGenerationalDistance(),
            new InvertedGenerationalDistancePlus())
        )
        .setIndependentRuns(INDEPENDENT_RUNS)
        .setNumberOfCores(12)
        .build();

    new ExecuteAlgorithms<>(experiment).run();
    new GenerateReferenceParetoFront(experiment).run();
    new ComputeQualityIndicators<>(experiment).run();
    new GenerateLatexTablesWithStatistics(experiment).run();
    new GenerateWilcoxonTestTablesWithR<>(experiment).run();
    new GenerateFriedmanTestTables<>(experiment).run();
    new GenerateBoxplotsWithR<>(experiment).setRows(1).setColumns(2).setDisplayNotch().run();
    new GenerateHtmlPages<>(experiment).run();
  }

  /**
   * The algorithm list is composed of pairs {@link Algorithm} + {@link Problem} which form part of
   * a {@link ExperimentAlgorithm}, which is a decorator for class {@link Algorithm}.
   */

  static List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> configureAlgorithmList(
      List<ExperimentProblem<CompositeSolution>> problemList) {
    List<ExperimentAlgorithm<CompositeSolution, List<CompositeSolution>>> algorithms = new ArrayList<>();
    for (int run = 0; run < INDEPENDENT_RUNS; run++) {

      for (ExperimentProblem<CompositeSolution> problem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new NSGAIIBuilder<>(
                problem.getProblem(),
                new IntegerBiclusterCrossover(0.5, 0.20),
                new IntegerBiclusterMutation(0.010),
                200)
                .setMaxEvaluations(40000)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<CompositeSolution> problem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new SPEA2Builder<>(
                problem.getProblem(),
                new IntegerBiclusterCrossover(0.5, 0.2),
                new IntegerBiclusterMutation(0.010))
                .setMaxIterations(200)
                .setPopulationSize(200)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<CompositeSolution> problem : problemList) {
        Algorithm<List<CompositeSolution>> algorithm = new MOCellBuilder<>(
                problem.getProblem(),
                new IntegerBiclusterCrossover(0.5, 0.2),
                new IntegerBiclusterMutation(0.010))
                .setMaxEvaluations(40000)
                .setPopulationSize(200)
                .build();
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

      for (ExperimentProblem<CompositeSolution> problem : problemList) {
        CompositeSolution initialSolution = problem.getProblem().createSolution();
        problem.getProblem().evaluate(initialSolution);

        Algorithm<List<CompositeSolution>> algorithm = new MOSA<>(
            initialSolution,
            problem.getProblem(),
            40000,
            new GenericBoundedArchive<>(200, new CrowdingDistanceDensityEstimator<>()),
            new IntegerBiclusterMutation(0.010),
            1.0,
            new Exponential(0.95)
        );
        algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
      }

    }
    return algorithms;
  }
}
