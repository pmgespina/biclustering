package org.uma.jmetal.problem.singleobjective;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.problem.compositeproblem.impl.AbstractCompositeProblem;
import org.uma.jmetal.problem.permutationproblem.impl.AbstractIntegerPermutationProblem;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution;

public class IntegerBiclustering extends AbstractIntegerPermutationProblem{

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
        return numGenes + numConditions;
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
        return "Single Objective. Integer Encoding Biclustering";
    }

    @Override
    public PermutationSolution<Integer> createSolution() {
        PermutationSolution<Integer> solution = new IntegerPermutationSolution(2,numberOfObjectives(),numberOfConstraints());

        List<Integer> genes = new ArrayList<>();
        List<Integer> conditions = new ArrayList<>();
        
        for (int i = 0; i < numGenes; i++) {
            solution.variables().add(1, genes);
            set(i, genes.variables().get(i));
        }
        for (int j = 0; j < numConditions; j++) {
            solution.variables().set(numGenes + j, conditions.variables().get(j));
        }

        solution.variables().set(0, genes);
        solution.variables().set(1, conditions);
        return solution;
    }


    @Override
    public PermutationSolution<Integer> evaluate(PermutationSolution<Integer> solution) {
        List<Integer> integerBicluster = solution.variables();

        double MSR = fitnessMSR(integerBicluster);
        double MSRNormalized = MSR / 4;

        double BSize = fitnessBSIze(integerBicluster, 0.5);

        double fitness = MSRNormalized * 0.5 + (1 - BSize) * 0.5;

        solution.objectives()[0] = fitness;

        return solution;
    }

    private double fitnessMSR(List<Integer> integerBicluster) {
        List<Integer> selectedGenes = integerBicluster.subList(0, numGenes); // no va a ser que siempre el bicluster contenga numGenes filas porque irá cambiando
        List<Integer> selectedConditions = integerBicluster.subList(numGenes, numGenes + numConditions);

        int sizeBicluster = selectedGenes.size() * selectedConditions.size();

        /*Once we have the indexes of the biclusters we can compute the metric */
        double overallMean = overallMean(selectedGenes, selectedConditions);
        double valueTSR = 0;

        for (Integer gene : selectedGenes) {
            double rowMean = rowMean(gene, selectedConditions);
            for (Integer condition: selectedConditions) {
                double columnMean = columnMean(condition, selectedGenes);
                double element = geneExpressionMatrix[gene][condition];
                valueTSR += Math.pow(element - rowMean - columnMean + overallMean, 2);
            }
        }

        return valueTSR / sizeBicluster;
    }

    private double fitnessBSIze(List<Integer> integerBicluster, double alpha) {
        List<Integer> selectedGenes = integerBicluster.subList(0, numGenes); // no va a ser que siempre el bicluster contenga numGenes filas porque irá cambiando
        List<Integer> selectedConditions = integerBicluster.subList(numGenes, numGenes + numConditions);

        // We already have this metric normalized because we give the rows (same with columns) of the bicluster a certain weight, 
        // but this is already divided by the whole rows of the input matrix
        double BSize = (alpha * (selectedGenes.size() / (double) geneExpressionMatrix.length)) 
            + ((1 - alpha) * (selectedConditions.size() / (double) geneExpressionMatrix[0].length));

        return BSize;
    }

    private double overallMean(List<Integer> selectedGenes, List<Integer> selectedConditions) {
        double sum = 0;
        int numElems = selectedGenes.size() * selectedConditions.size(); // porque estamos calculando medidas del bicluster, no de toda la matriz de expresión génica
        for (Integer gene : selectedGenes) {
            for (Integer condition : selectedConditions) {
                sum += geneExpressionMatrix[gene][condition];
            }
        }
        return numElems > 0 ? sum / numElems : 0;
    }

    private double columnMean(Integer condition, List<Integer> selectedGenes) {
        double sum = 0;
        for (Integer gene : selectedGenes) {
            sum += geneExpressionMatrix[gene][condition];
        }
        return !selectedGenes.isEmpty() ? sum / selectedGenes.size() : 0;
    }

    private double rowMean(Integer gene, List<Integer> selectedConditions) {
        double sum = 0;
        for (Integer condition : selectedConditions) {
            sum += geneExpressionMatrix[gene][condition];
        }
        return !selectedConditions.isEmpty() ? sum / selectedConditions.size() : 0;
    }

}
