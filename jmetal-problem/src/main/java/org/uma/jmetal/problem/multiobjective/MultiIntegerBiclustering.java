package org.uma.jmetal.problem.multiobjective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.compositesolution.CompositeSolution;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution;
import org.uma.jmetal.util.bounds.Bounds;

public class MultiIntegerBiclustering implements Problem<CompositeSolution> {

    private final int numGenes; // numGenes or number of rows involved in the bicluster
    private final int numConditions; // numConditions or number of conditions involved in the bicluster
    private final double[][] geneExpressionMatrix;

    public MultiIntegerBiclustering(double[][] matrix) {
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
        return 3;
    }

    @Override
    public int numberOfConstraints() {
        return 0;
    }

    @Override
    public String name() {
        return "Multi Objective Integer Encoding Biclustering";
    }

    @Override
    public CompositeSolution evaluate(CompositeSolution solution) {
        List<Integer> selectedGenes = ((IntegerSolution) solution.variables().get(0)).variables();
        List<Integer> selectedConditions = ((IntegerSolution) solution.variables().get(1)).variables();

        double MSR = fitnessMSR(selectedGenes, selectedConditions);
        double MSRNormalized = MSR / 4;

        double BSize = fitnessBSize(selectedGenes, selectedConditions, 0.5);

        double rVar = fitnessrVAR(selectedGenes, selectedConditions);

        solution.objectives()[0] = MSRNormalized;
        solution.objectives()[1] = - BSize;
        solution.objectives()[2] = - rVar;

        return solution;
    }

    @Override
    public CompositeSolution createSolution() {
        // IntegerSolutions are initialised randomly with repeated elements possibly
        IntegerSolution selectedGenes = new DefaultIntegerSolution(rowsBounds(), numberOfObjectives(), numberOfConstraints());
        IntegerSolution selectedConditions = new DefaultIntegerSolution(columnBounds(), numberOfObjectives(), numberOfConstraints());
    
        // Ensure no duplicates in selectedGenes
        List<Integer> uniqueGenes = new ArrayList<>();
        for (int i = 0; i < selectedGenes.variables().size(); i++) {
            int value = selectedGenes.variables().get(i);
            if (uniqueGenes.contains(value)) {
                selectedGenes.variables().set(i, -1);
            } else {
                uniqueGenes.add(value);
            }
        }
    
        // Ensure no duplicates in selectedConditions
        List<Integer> uniqueConditions = new ArrayList<>();
        for (int i = 0; i < selectedConditions.variables().size(); i++) {
            int value = selectedConditions.variables().get(i);
            if (uniqueConditions.contains(value)) {
                selectedConditions.variables().set(i, -1);
            } else {
                uniqueConditions.add(value);
            }
        }

        Collections.sort(selectedGenes.variables());
        Collections.sort(selectedConditions.variables());
    
        List<Solution<?>> solutions = new ArrayList<>();
        solutions.add(selectedGenes);
        solutions.add(selectedConditions);
    
        return new CompositeSolution(solutions);
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

        // Once we have the indexes of the biclusters we can compute the metric
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
        // We already have this metric normalized because we give the rows (same with columns) of the bicluster a certain weight, 
        // but this is already divided by the whole rows of the input matrix
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

    private double fitnessrVAR(List<Integer> selectedGenes, List<Integer> selectedConditions) {
        int sizeBicluster = selectedGenes.size() * selectedConditions.size();

        double value = 0;
        for (Integer gene : selectedGenes) {
            if (gene != -1) {
                double rowMean = rowMean(gene, selectedConditions);
                for (Integer condition: selectedConditions) {
                    if (condition != -1) {
                        double element = geneExpressionMatrix[gene][condition];
                        value += Math.pow(element - rowMean, 2);
                    }
                }
            }
        }

        return value / sizeBicluster;
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
