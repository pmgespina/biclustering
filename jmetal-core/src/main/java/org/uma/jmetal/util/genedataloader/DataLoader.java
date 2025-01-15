package org.uma.jmetal.util.genedataloader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static double[][] CSVtoDoubleMatrix(String filePath) throws IOException {
        List<double[]> data = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Split by comma
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                data.add(row);
            }
        }

        // Convert List<Double[]> to Double[][]
        double[][] geneExpressionMatrix = new double[data.size()][];
        geneExpressionMatrix = data.toArray(geneExpressionMatrix);

        return geneExpressionMatrix;
    }

}
