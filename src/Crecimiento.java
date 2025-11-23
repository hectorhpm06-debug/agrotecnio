package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Crecimiento {
    private static Map<Integer, Double> tablaPesos = new HashMap<>();

    public static void cargarDatos() {
        // Ruta actualizada a carpeta data/
        try (BufferedReader br = new BufferedReader(new FileReader("data/pesos.csv"))) {
            br.readLine();
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] d = linea.split("[,;]");
                if (d.length >= 2) {
                    int semana = (int) Double.parseDouble(d[0].replace(",", "."));
                    double peso = Double.parseDouble(d[1].replace(",", "."));
                    tablaPesos.put(semana, peso);
                }
            }
            System.out.println("✅ Datos de crecimiento cargados.");
        } catch (Exception e) { 
            System.out.println("⚠️ No se pudo leer 'pesos.csv'. Usando fórmula estándar.");
        }
    }

    public static double getPeso(int semana) {
        return tablaPesos.getOrDefault(semana, 20.0 + (semana * 4.5));
    }
}