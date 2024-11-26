package org.uma.jmetal.problem.multiobjective;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.binarySet.BinarySet;

public class MultiBinaryBiclustering extends AbstractBinaryProblem{

    private final int numGenes;
    private final int numConditions;

    private final double[][] geneExpressionMatrix;

    public MultiBinaryBiclustering(double[][] matrix) {
        geneExpressionMatrix = matrix;
        numGenes = matrix.length;
        numConditions = matrix[0].length;
    }

    @Override
    public List<Integer> numberOfBitsPerVariable() {
        List<Integer> bitsPerVariable = new ArrayList<>();
        bitsPerVariable.add(numGenes + numConditions);
        return bitsPerVariable;
    }

    @Override
    public int numberOfVariables() {
        return 1;
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
        return "Multi Objective Binary Encoding Biclustering";
    }

    @Override
    public BinarySolution createSolution() {
        DefaultBinarySolution solution = new DefaultBinarySolution(numberOfBitsPerVariable(), numberOfObjectives());

        Random r = new Random();
        Integer bitsPerVariable = numberOfBitsPerVariable().get(0);
        BinarySet bicluster = new BinarySet(bitsPerVariable); // Creates a bit set with initial size 0 to bitsPerVariable-1.
        for (int i = 0; i < bitsPerVariable; i++) {
            bicluster.set(i, r.nextBoolean());
        }

        solution.variables().set(0, bicluster);

        return solution;
    }

    @Override
    public BinarySolution evaluate(BinarySolution solution) {

        BinarySet binaryBicluster = solution.variables().get(0);

        double MSR = fitnessMSR(binaryBicluster);
        double MSRNormalized = MSR / 4;

        double BSize = fitnessBSize(binaryBicluster, 0.5);

        double rVAR = fitnessrVAR(binaryBicluster);

        solution.objectives()[0] = MSRNormalized;
        solution.objectives()[1] = - BSize;
        solution.objectives()[2] = - rVAR;

        return solution;
    }

    private double fitnessMSR(BinarySet bicluster) {
        /*We firstly fill the indexes of the bicluster into the selectedGenes list and selectedConditions list */
        List<Integer> selectedGenes = new ArrayList<>();
        List<Integer> selectedConditions = new ArrayList<>();

        // Va desde cero hasta el numero de genes - 1 rellenando según el array solución diga que el gen en el indice i está o no en el bicluster
        // Recorre toda la representación binaria del bicluster para almacenar índices en selectedGenes de los que pertenecen al bicluster 
        for (int i = 0; i < numGenes; i++) {
            if (bicluster.get(i)) {
                selectedGenes.add(i);
            }
        }
        
        for (int j = numGenes; j < numGenes + numConditions; j++) {
            if (bicluster.get(j)) {
                selectedConditions.add(j - numGenes); // Tenemos que restarle el número de genes porque sino el índice de la columna no representaría a la columna
            }
        }

        int sizeBicluster = selectedGenes.size() * selectedConditions.size();
        if (sizeBicluster == 0) return Double.POSITIVE_INFINITY;

        /*Once we have the indexes of the biclusters we can compute the metric */
        double overallMean = overallMean(selectedGenes, selectedConditions);
        double valueTSR = 0;

        for (Integer gene : selectedGenes) {
            double rowMean = rowMean(gene, selectedConditions);
            for (Integer condition: selectedConditions) {
                double columnMean = columnMean(condition, selectedGenes);
                double element = geneExpressionMatrix[gene][condition];
                double squaredResidue = Math.pow(element - rowMean - columnMean + overallMean, 2);
                valueTSR += squaredResidue;
            }
        }

        return valueTSR / sizeBicluster;
    }

    private double fitnessBSize(BinarySet bicluster, double alpha) {
        /*We firstly fill the indexes of the bicluster into the selectedGenes list and selectedConditions list */
        List<Integer> selectedGenes = new ArrayList<>();
        List<Integer> selectedConditions = new ArrayList<>();

        for (int i = 0; i < numGenes; i++) {
            if (bicluster.get(i)) {
                selectedGenes.add(i);
            }
        }

        for (int j = numGenes; j < numGenes + numConditions; j++) {
            if (bicluster.get(j)) {
                selectedConditions.add(j - numGenes); // Tenemos que restarle el número de genes porque sino el índice de la columna no representaría a la columna
            }
        }

        double valueBSize = (alpha * (selectedGenes.size() / (double) geneExpressionMatrix.length)) 
            + ((1 - alpha) * (selectedConditions.size() / (double) geneExpressionMatrix[0].length));

        return valueBSize;
    }

    private double fitnessrVAR(BinarySet bicluster) {
        /*We firstly fill the indexes of the bicluster into the selectedGenes list and selectedConditions list */
        List<Integer> selectedGenes = new ArrayList<>();
        List<Integer> selectedConditions = new ArrayList<>();

        for (int i = 0; i < numGenes; i++) {
            if (bicluster.get(i)) {
                selectedGenes.add(i);
            }
        }

        for (int j = numGenes; j < numGenes + numConditions; j++) {
            if (bicluster.get(j)) {
                selectedConditions.add(j - numGenes); // Tenemos que restarle el número de genes porque sino el índice de la columna no representaría a la columna
            }
        }

        int sizeBicluster = selectedGenes.size() * selectedConditions.size();
        if (sizeBicluster == 0) return Double.POSITIVE_INFINITY;

        double value = 0;
        for (Integer gene : selectedGenes) {
            double rowMean = rowMean(gene, selectedConditions);
            for (Integer condition: selectedConditions) {
                double element = geneExpressionMatrix[gene][condition];
                value += Math.pow(element - rowMean, 2);
            }
        }

        return value / sizeBicluster;
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
