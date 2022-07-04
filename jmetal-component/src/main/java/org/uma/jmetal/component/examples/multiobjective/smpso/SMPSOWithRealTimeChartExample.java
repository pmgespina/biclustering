package org.uma.jmetal.component.examples.multiobjective.smpso;

import java.util.List;
import org.uma.jmetal.component.algorithm.ParticleSwarmOptimizationAlgorithm;
import org.uma.jmetal.component.algorithm.multiobjective.SMPSOBuilder;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.qualityindicator.QualityIndicatorUtils;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.ProblemUtils;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.VectorUtils;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.observer.impl.EvaluationObserver;
import org.uma.jmetal.util.observer.impl.RunTimeChartObserver;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.termination.Termination;
import org.uma.jmetal.util.termination.impl.TerminationByEvaluations;

/**
 * Class for configuring and running the SMPSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMPSOWithRealTimeChartExample extends AbstractAlgorithmRunner {
  public static void main(String[] args) throws Exception {
    String problemName = "org.uma.jmetal.problem.multiobjective.zdt.ZDT4";
    String referenceParetoFront = "resources/referenceFrontsCSV/ZDT4.csv";

    Problem<DoubleSolution> problem = ProblemUtils.<DoubleSolution>loadProblem(problemName);

    int swarmSize = 100 ;
    Termination termination = new TerminationByEvaluations(25000);

    ParticleSwarmOptimizationAlgorithm smpso = new SMPSOBuilder(
        (DoubleProblem) problem,
        swarmSize)
        .setTermination(termination)
        .build();

    smpso.getObservable().register(new RunTimeChartObserver<>("SMPSO", 80, 100, referenceParetoFront));

    smpso.run();

    List<DoubleSolution> population = smpso.getResult();
    JMetalLogger.logger.info("Total execution time : " + smpso.getTotalComputingTime() + "ms");
    JMetalLogger.logger.info("Number of evaluations: " + smpso.getEvaluation());

    new SolutionListOutput(population)
        .setVarFileOutputContext(new DefaultFileOutputContext("VAR.csv", ","))
        .setFunFileOutputContext(new DefaultFileOutputContext("FUN.csv", ","))
        .print();

    JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
    JMetalLogger.logger.info("Objectives values have been written to file FUN.csv");
    JMetalLogger.logger.info("Variables values have been written to file VAR.csv");

    QualityIndicatorUtils.printQualityIndicators(
        SolutionListUtils.getMatrixWithObjectiveValues(population),
        VectorUtils.readVectors(referenceParetoFront, ","));
  }
}