package org.uma.jmetal.algorithm.singleobjective.geneticalgorithm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.observable.Observable;
import org.uma.jmetal.util.observable.impl.DefaultObservable;

/**
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class ObservableGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
  private Comparator<S> comparator;
  private int maxEvaluations;
  private int evaluations;
  private Observable<Map<String, Object>> observable;

  private SolutionListEvaluator<S> evaluator;

  /**
   * Constructor
   */
  public ObservableGeneticAlgorithm(Problem<S> problem, int maxEvaluations, int populationSize,
                                      CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                      SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
    super(problem);
    this.maxEvaluations = maxEvaluations;
    this.setMaxPopulationSize(populationSize);

    this.crossoverOperator = crossoverOperator;
    this.mutationOperator = mutationOperator;
    this.selectionOperator = selectionOperator;

    this.evaluator = evaluator;

    comparator = new ObjectiveComparator<S>(0);

    this.observable = new DefaultObservable<>("Generational Genetic Algorithm");
  }

  @Override protected boolean isStoppingConditionReached() {
    return (evaluations >= maxEvaluations);
  }

  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    population.sort(comparator);
    offspringPopulation.add(population.get(0));
    offspringPopulation.add(population.get(1));
    offspringPopulation.sort(comparator);
    offspringPopulation.remove(offspringPopulation.size() - 1);
    offspringPopulation.remove(offspringPopulation.size() - 1);

    return offspringPopulation;
  }

  @Override protected List<S> evaluatePopulation(List<S> population) {
    population = evaluator.evaluate(population, getProblem());

    return population;
  }

  @Override public S result() {
    getPopulation().sort(comparator);
    return getPopulation().get(0);
  }

  @Override public void initProgress() {
    evaluations = getMaxPopulationSize();
  }

  @Override public void updateProgress() {
    evaluations += getMaxPopulationSize();

    Map<String, Object> data = new HashMap<>();
    getPopulation().sort(comparator);
    data.put("EVALUATIONS", evaluations);
    data.put("BEST_SOLUTION", getPopulation().get(0));
    observable.setChanged();
    observable.notifyObservers(data);
  }

  @Override public String name() {
    return "gGA" ;
  }

  @Override public String description() {
    return "Generational Genetic Algorithm" ;
  }

  public Observable<Map<String, Object>> getObservable() {
    return observable;
  }
}
