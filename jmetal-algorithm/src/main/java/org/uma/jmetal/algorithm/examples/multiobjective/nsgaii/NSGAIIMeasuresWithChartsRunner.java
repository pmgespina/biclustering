package org.uma.jmetal.algorithm.examples.multiobjective.nsgaii;

import java.io.IOException;
import java.util.List;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.uma.jmetal.algorithm.examples.AlgorithmRunner;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIMeasures;
import org.uma.jmetal.operator.crossover.impl.SBXCrossover;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.ProblemFactory;
import org.uma.jmetal.qualityindicator.QualityIndicatorUtils;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.VectorUtils;
import org.uma.jmetal.util.chartcontainer.ChartContainer;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.legacy.front.impl.ArrayFront;
import org.uma.jmetal.util.measure.MeasureListener;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;

/**
 * Class to configure and run the NSGA-II algorithm (variant with measures)
 */
public class NSGAIIMeasuresWithChartsRunner extends AbstractAlgorithmRunner {

  /**
   * @param args Command line arguments.
   */
  public static void main(String[] args) throws IOException {
    String problemName = "org.uma.jmetal.problem.multiobjective.zdt.ZDT1";
    String referenceParetoFront = "/home/khaosdev/jMetalJava/jMetal/resources/referenceFrontsCSV/ZDT1.csv";

    var problem = ProblemFactory.<DoubleSolution>loadProblem(problemName);

    double crossoverProbability = 0.9;
    double crossoverDistributionIndex = 20.0;
    var crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.numberOfVariables();
    double mutationDistributionIndex = 20.0;
    var mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    var selection = new BinaryTournamentSelection<>(
        new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    int populationSize = 100;
    int maxEvaluations = 25000;
    var algorithm = new NSGAIIBuilder<DoubleSolution>(problem, crossover, mutation, populationSize)
        .setSelectionOperator(selection)
        .setMaxEvaluations(maxEvaluations)
        .setVariant(NSGAIIBuilder.NSGAIIVariant.Measures)
        .build();

    ((NSGAIIMeasures<DoubleSolution>) algorithm).setReferenceFront(
        new ArrayFront(referenceParetoFront));

    MeasureManager measureManager = ((NSGAIIMeasures<DoubleSolution>) algorithm).getMeasureManager();

    /* Measure management */
    BasicMeasure<List<DoubleSolution>> solutionListMeasure = (BasicMeasure<List<DoubleSolution>>) measureManager
        .<List<DoubleSolution>>getPushMeasure("currentPopulation");
    CountingMeasure iterationMeasure = (CountingMeasure) measureManager.<Long>getPushMeasure(
        "currentEvaluation");
    BasicMeasure<Double> hypervolumeMeasure = (BasicMeasure<Double>) measureManager
        .<Double>getPushMeasure("hypervolume");

    ChartContainer chart = new ChartContainer(algorithm.name(), 100);
    chart.setFrontChart(0, 1, referenceParetoFront);
    chart.addIndicatorChart("Hypervolume");
    chart.initChart();

    solutionListMeasure.register(new ChartListener(chart));
    iterationMeasure.register(new IterationListener(chart));
    hypervolumeMeasure.register(new IndicatorListener("Hypervolume", chart));
    /* End of measure management */

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
    chart.saveChart("./chart", BitmapFormat.PNG);

    List<DoubleSolution> population = algorithm.result();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }

  private static class IterationListener implements MeasureListener<Long> {

    ChartContainer chart;

    public IterationListener(ChartContainer chart) {
      this.chart = chart;
      this.chart.getChart("Hypervolume").setTitle("Iteration: " + 0);
    }

    @Override
    synchronized public void measureGenerated(Long iteration) {
      if (this.chart != null) {
        this.chart.getChart("Hypervolume").setTitle("Iteration: " + iteration);
      }
    }
  }

  private static class IndicatorListener implements MeasureListener<Double> {

    ChartContainer chart;
    String indicator;

    public IndicatorListener(String indicator, ChartContainer chart) {
      this.chart = chart;
      this.indicator = indicator;
    }

    @Override
    synchronized public void measureGenerated(Double value) {
      if (this.chart != null) {
        this.chart.updateIndicatorChart(this.indicator, value);
        this.chart.refreshCharts(0);
      }
    }
  }

  private static class ChartListener implements MeasureListener<List<DoubleSolution>> {

    private ChartContainer chart;
    private int iteration = 0;

    public ChartListener(ChartContainer chart) {
      this.chart = chart;
      this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
    }

    private void refreshChart(List<DoubleSolution> solutionList) {
      if (this.chart != null) {
        iteration++;
        this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
        this.chart.updateFrontCharts(solutionList);
        this.chart.refreshCharts();
      }
    }

    @Override
    synchronized public void measureGenerated(List<DoubleSolution> solutions) {
      refreshChart(solutions);
    }
  }

}
