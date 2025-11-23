package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== INICIO SIMULACIÓN (MODO BLINDADO) ===");
        
        consumo.cargarDatos(); 
        Escorxador escorxador = cargarMatadero("data/matadero.csv"); 
        List<Granja> granjas = cargarGranjas("data/granjas.csv");     
        List<Transport> flota = cargarCamiones("data/camiones.csv");  

        System.out.println("\n -> RESUMEN DE DATOS:");
        System.out.println("    - Granjas cargadas: " + granjas.size());
        System.out.println("    - Camiones cargados: " + flota.size());
        System.out.println("    - Matadero: " + escorxador.id + " (" + escorxador.capacidadDiaria + " cerdos/día)");

        SimulacioLogistica simulacion = new SimulacioLogistica(escorxador, granjas, flota);
        simulacion.runSimulation();
        
        System.out.println("\n=== FIN SIMULACIÓN ===");
    }

    // Helper para separar CSV (detecta ; o ,)
    private static String[] splitCsvLine(String line) {
        if (line.contains(";")) return line.split(";");
        return line.split(",");
    }

    // Helper para limpiar números (quita espacios y gestiona decimales)
    private static double parseCleanDouble(String val) {
        try {
            return Double.parseDouble(val.trim().replace(",", "."));
        } catch (Exception e) { return 0.0; }
    }

    public static Escorxador cargarMatadero(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            br.readLine(); 
            String l = br.readLine();
            if (l != null) {
                String[] d = splitCsvLine(l);
                return new Escorxador(d[0], parseCleanDouble(d[2]), parseCleanDouble(d[3]), 
                    (int)parseCleanDouble(d[4]), parseCleanDouble(d[5]), parseCleanDouble(d[6]), 
                    parseCleanDouble(d[7]), parseCleanDouble(d[8]), parseCleanDouble(d[9]));
            }
        } catch (Exception e) { System.out.println("! Error Matadero: " + e.getMessage()); }
        return new Escorxador("MAT_DEF", 41.63, 0.52, 1500, 1.56, 100, 120, 90, 130);
    }

    public static List<Granja> cargarGranjas(String archivo) {
        List<Granja> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            br.readLine();
            String l;
            while ((l = br.readLine()) != null) {
                String[] d = splitCsvLine(l);
                if(d.length >= 11) {
                    lista.add(new Granja(d[0], d[1], parseCleanDouble(d[2]), parseCleanDouble(d[3]), 
                        (int)parseCleanDouble(d[4]), parseCleanDouble(d[5]), parseCleanDouble(d[6]), 
                        (int)parseCleanDouble(d[7]), parseCleanDouble(d[8]), (int)parseCleanDouble(d[9]), 
                        (int)parseCleanDouble(d[10])));
                }
            }
        } catch (Exception e) { System.out.println("! Error Granjas: " + e.getMessage()); }
        
        if (lista.isEmpty()) {
            System.out.println("! Usando Granjas de Respaldo...");
            lista.add(new Granja("GRJ_001", "Granja A", 41.3138, 0.8416, 2000, 101.0, 4.5, 18, 1.4, 200, 2500));
        }
        return lista;
    }

    public static List<Transport> cargarCamiones(String archivo) {
        List<Transport> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            br.readLine();
            String l;
            while ((l = br.readLine()) != null) {
                String[] d = splitCsvLine(l);
                if(d.length >= 6) {
                    double rawCap = parseCleanDouble(d[2]);
                    double capacidadKg;

                    
                    // Un camión de cerdos lleva entre 5.000kg y 40.000kg.
                    
                    if (rawCap > 40000) {
                        // Si el valor es gigante (ej. 100000), lo reducimos
                        capacidadKg = rawCap / 1000.0; 
                        if(capacidadKg > 40000) capacidadKg = 20000; // Si sigue mal, forzamos 20T
                    } else if (rawCap < 1000) {
                        // Si es pequeño (ej. 10, 20, 25), son Toneladas -> Pasar a Kg
                        capacidadKg = rawCap * 1000.0;
                    } else {
                        // Si está entre 1000 y 40000, el valor es correcto
                        capacidadKg = rawCap;
                    }
                    

                    lista.add(new Transport(d[0], capacidadKg, parseCleanDouble(d[3]), 
                        (int)parseCleanDouble(d[4]), parseCleanDouble(d[5])));
                }
            }
        } catch (Exception e) { System.out.println("! Error Camiones: " + e.getMessage()); }

        if (lista.isEmpty()) {
            lista.add(new Transport("TRP_S1", 10000, 1.15, 40, 1000));
            lista.add(new Transport("TRP_L1", 20000, 1.25, 40, 1500));
        }
        return lista;
    }
}