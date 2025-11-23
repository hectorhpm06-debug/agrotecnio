package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class consumo{ 
    private static Map<Integer, Double> tablaConsumo = new HashMap<>();
    
    private static final double PRECIO_PIENSO_KG = 0.35; 

    public static void cargarDatos() {
        // Nombre del archivo de consumo 
        try (BufferedReader br = new BufferedReader(new FileReader("data/consumo.csv"))) {
            // Saltamos las dos líneas de cabecera
            br.readLine(); 
            br.readLine(); 
            
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split("[,;]");
                if (d.length >= 2) {
                    int semana = (int) Double.parseDouble(d[0].replace(",", "."));
                    double consumoAcum = Double.parseDouble(d[1].replace(",", "."));
                    tablaConsumo.put(semana, consumoAcum);
                }
            }
            System.out.println("✅ Datos de consumo cargados.");
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo leer 'consumo.csv'. El coste de comida será 0. Error: " + e.getMessage());
        }
    }

    // Calculamos cuánto come un cerdo en una semana
    public static double getCosteSemanalPorCerdo(int semana) {
        if (semana <= 1) return 0;
        double consumoActual = tablaConsumo.getOrDefault(semana, 0.0);
        double consumoAnterior = tablaConsumo.getOrDefault(semana - 1, 0.0);
        
        // El consumo de esta semana es la diferencia en el acumulado
        double consumoSemanaKg = consumoActual - consumoAnterior;
        
        // Coste = Kg comidos * Precio del pienso
        if (consumoSemanaKg < 0) consumoSemanaKg = 0; // Por si acaso el dato es raro
        return consumoSemanaKg * PRECIO_PIENSO_KG;
    }
}