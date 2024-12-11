package org.uma.jmetal.problem.singleobjective;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

public class IntegerBiclustering implements Problem<CompositeSolution> {

    private final int numGenes; // numGenes or number of rows involved in the bicluster
    private final int numConditions; // numConditions or number of conditions involved in the bicluster
    private final double[][] geneExpressionMatrix;

    public IntegerBiclustering(double[][] matrix) {
        geneExpressionMatrix = matrix;
        numGenes = matrix.length;
        numConditions = matrix[0].length;
    }

    @Override
    public int numberOfVariables() {
        return 2;
    }

    @Override
    public int numberOfObjectives() {
        return 1;
    }

    @Override
    public int numberOfConstraints() {
        return 0;
    }

    @Override
    public String name() {
        return "Single Objective Integer Encoding Biclustering";
    }

    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {
        List<Integer> selectedGenes = ((IntegerSolution) solution.variables().get(0)).variables();
        List<Integer> selectedConditions = ((IntegerSolution) solution.variables().get(1)).variables();

        double MSR = fitnessMSR(selectedGenes, selectedConditions);
        double MSRNormalized = MSR / 4;

        double BSize = fitnessBSize(selectedGenes, selectedConditions, 0.5);

        double fitness = 0.5 * MSRNormalized + (1 - BSize) * 0.5;

        solution.objectives()[0] = fitness;

        return solution;
    }

    @Override
    public CompositeSolution createSolution() {
        IntegerSolution selectedGenes = new DefaultIntegerSolution(rowsBounds(), numberOfObjectives(), numberOfConstraints());
        IntegerSolution selectedConditions = new DefaultIntegerSolution(columnBounds(), numberOfObjectives(), numberOfConstraints());
        /* The IntegerSolutions are already initialised taking into account the bounds restriction
         * If rowsBounds() returns a list with 10 bounds, the variables list will have 10 elements
         */

        List<Solution<?>> solutions = new ArrayList<>();
        solutions.add(selectedGenes);
        solutions.add(selectedConditions);

        CompositeSolution solution = new CompositeSolution(solutions);
        
        return solution;
    }

    private List<Bounds<Integer>> rowsBounds() {
        List<Bounds<Integer>> rowsBounds = new ArrayList<>();
        // To apply the bounds restriction to all the elements of the list
        for (int i = 0; i < numGenes; i++) {
            // Supposing that the upper bound cannot be numGenes because the index starts at 0
            rowsBounds.add(Bounds.create(-1, numGenes - 1));
        }
        return rowsBounds;
    }

    private List<Bounds<Integer>> columnBounds() {
        List<Bounds<Integer>> columnBounds = new ArrayList<>();
        // To apply the bounds restriction to all the elements of the list
        for (int i = 0; i < numConditions; i++) {
            // Supposing that the upper bound cannot be numConditions because the index starts at 0
            columnBounds.add(Bounds.create(-1, numConditions - 1));
        }
        return columnBounds;
    }

    private double fitnessMSR(List<Integer> selectedGenes, List<Integer> selectedConditions) {
        int sizeBicluster = selectedGenes.size() * selectedConditions.size();

        /*Once we have the indexes of the biclusters we can compute the metric */
        double overallMean = overallMean(selectedGenes, selectedConditions);
        double valueTSR = 0;

        for (Integer gene : selectedGenes) {
            if (gene != -1) {
                double rowMean = rowMean(gene, selectedConditions);
                for (Integer condition: selectedConditions) {
                    if (condition != -1) {
                        double columnMean = columnMean(condition, selectedGenes);
                        double element = geneExpressionMatrix[gene][condition];
                        valueTSR += Math.pow(element - rowMean - columnMean + overallMean, 2);
                    }
                }
            }
        }

        return valueTSR / sizeBicluster;
    }

    private double fitnessBSize(List<Integer> selectedGenes, List<Integer> selectedConditions, double alpha) {
        /* We already have this metric normalized because we give the rows (same with columns) of the bicluster a certain weight, 
        but this is already divided by the whole rows of the input matrix */
        List<Integer> filteredGenes = selectedGenes.stream()
                .filter(value -> value != -1) // Keep values that actually belong to the bicluster
                .collect(Collectors.toList());
        List<Integer> filteredConditions = selectedConditions.stream()
                .filter(value -> value != -1) // Keep values that actually belong to the bicluster
                .collect(Collectors.toList());
        double BSize = (alpha * (filteredGenes.size() / (double) geneExpressionMatrix.length)) 
            + ((1 - alpha) * (filteredConditions.size() / (double) geneExpressionMatrix[0].length));

        return BSize;
    }

    private double overallMean(List<Integer> selectedGenes, List<Integer> selectedConditions) {
        double sum = 0;
        int numElems = selectedGenes.size() * selectedConditions.size();
        for (Integer gene : selectedGenes) {
            if (gene != -1) {
                for (Integer condition : selectedConditions) {
                    if (condition != -1) {
                        sum += geneExpressionMatrix[gene][condition];
                    }
                }
            }
        }
        return numElems > 0 ? sum / numElems : 0;
    }

    private double columnMean(Integer condition, List<Integer> selectedGenes) {
        double sum = 0;
        // We already know that the condition index is not 0, as it is controlled in fitnessMSR method
        for (Integer gene : selectedGenes) {
            if (gene != -1) {
                sum += geneExpressionMatrix[gene][condition];
            }
        }
        return !selectedGenes.isEmpty() ? sum / selectedGenes.size() : 0;
    }

    private double rowMean(Integer gene, List<Integer> selectedConditions) {
        double sum = 0;
        // We already know that the gene index is not 0, as this is controlled in fitnessMSR method
        for (Integer condition : selectedConditions) {
            if (condition != -1) {
                sum += geneExpressionMatrix[gene][condition];
            }
        }
        return !selectedConditions.isEmpty() ? sum / selectedConditions.size() : 0;
    }

}
