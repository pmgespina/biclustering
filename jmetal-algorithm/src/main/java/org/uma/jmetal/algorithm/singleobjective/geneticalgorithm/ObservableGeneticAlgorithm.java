package org.uma.jmetal.algorithm.singleobjective.geneticalgorithm;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.observable.impl.DefaultObservable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservableGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {

    private Comparator<S> comparator;
    private final int maxEvaluations;
    private int evaluations;
    private DefaultObservable<Map<String, Object>> observable;

    public ObservableGeneticAlgorithm(Problem<S> problem, 
                                        int maxEvaluations, int populationSize,
                                        CrossoverOperator<S> crossover,MutationOperator<S> mutation, 
                                        SelectionOperator<List<S>, S> selection) {
        super(problem);
        setMaxPopulationSize(populationSize);
        this.maxEvaluations = maxEvaluations;

        this.crossoverOperator = crossover;
        this.mutationOperator = mutation;
        this.selectionOperator = selection;

        // Initialize the observable for this algorithm
        this.observable = new DefaultObservable<>("Observable Genetic Algorithm");
        this.comparator = new ObjectiveComparator<S>(0);
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        population.sort(comparator);
        offspringPopulation.add(population.get(0));
        offspringPopulation.add(population.get(1));
        offspringPopulation.sort(comparator);
        offspringPopulation.remove(offspringPopulation.size() - 1);
        offspringPopulation.remove(offspringPopulation.size() - 1);
    
        return offspringPopulation;
    }

    public DefaultObservable<Map<String, Object>> getObservable() {
        return observable;
    }

    @Override
    protected void initProgress() {
        evaluations = getMaxPopulationSize();
    }

    @Override
    protected void updateProgress() {
        evaluations += getMaxPopulationSize();

        // Notify observers of the current progress
        Map<String, Object> data = new HashMap<>();
        getPopulation().sort(comparator);
        data.put("EVALUATIONS", evaluations);
        data.put("BEST_SOLUTION", getPopulation().get(0));
        observable.setChanged();
        observable.notifyObservers(data);
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        population.forEach(getProblem()::evaluate);
        return population;
    }

    @Override
    public S result() {
        getPopulation().sort(comparator);
        return getPopulation().get(0);
    }

    @Override
    public String name() {
        return "oGA";
    }

    @Override
    public String description() {
        return "Observable Genetic Algorithm";
    }
}
