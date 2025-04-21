package org.uma.jmetal.utilities;

public class DimensionExtractor {
    /**
     * Extrae la dimensión de un archivo CSV dado su ruta.
     *
     * @param ruta La ruta del archivo CSV.
     * @return La dimensión extraída como una cadena (ejemplo: "100x100").
     */

    public static String extraerDim(String ruta) {
        // Extraer solo el nombre del archivo
        String nombreArchivo = ruta.substring(ruta.lastIndexOf("/") + 1); // fabia_100x100.csv

        // Quitar la extensión
        String nombreSinExtension = nombreArchivo.substring(0, nombreArchivo.lastIndexOf(".")); // fabia_100x100

        // Obtener lo que está después de la barra baja
        int guionBajoIndex = nombreSinExtension.indexOf("_");
        if (guionBajoIndex != -1 && guionBajoIndex < nombreSinExtension.length() - 1) {
            return nombreSinExtension.substring(guionBajoIndex + 1); // 100x100
        }

        return ""; // En caso de que no exista _
    }

}
