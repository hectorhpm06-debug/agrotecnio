package src;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SimulacioLogistica {
    
    private Escorxador escorxador;
    private List<Granja> granges;
    private List<Transport> flota;
    private StringBuilder jsBuilder = new StringBuilder();
    
    private double totalIngresos = 0;
    private double totalCosteTransporte = 0;
    private double totalCosteComida = 0;
    private int totalCerdosVendidos = 0;

    public SimulacioLogistica(Escorxador escorxador, List<Granja> granges, List<Transport> flota) {
        this.escorxador = escorxador;
        this.granges = granges;
        this.flota = flota;
        jsBuilder.append("const datos_simulacion = [\n");
    }
    
    public void runSimulation() {
        for (int week = 1; week <= 2; week++) {
            // Inicio de semana: Planificar pedidos
            for(Granja g : granges) {
                if(week > 1) g.updateWeeklyGrowth();
                // Establecemos el objetivo de venta de la semana
                int objetivoVenta = g.cerdosConsumo > 0 ? g.cerdosConsumo : (int)(g.inventario * 0.15);
                g.cerdosPendientes = Math.min(g.inventario, objetivoVenta);
            }

            for (int day = 1; day <= 5; day++) {
                int diaGlobal = ((week - 1) * 5) + day;
                runDay(week, diaGlobal);
            }
        }
        guardarDatosJS();
    }

    private void runDay(int week, int diaGlobal) {
        int cupo = escorxador.capacidadDiaria;
        double costeTransDia = 0;
        double ingresosDia = 0;
        int cerdosDia = 0;
        List<Ruta> rutasDelDia = new ArrayList<>();
        List<Transport> libres = new ArrayList<>(flota); // Usamos toda la flota disponible

        // Granjas que todavía tienen cerdos pendientes de recoger esta semana
        List<Granja> candidatos = granges.stream()
            .filter(g -> g.cerdosPendientes > 0) // Seguimos yendo mientras queden pendientes
            .sorted(Comparator.comparingDouble((Granja g) -> {
                double dist = Utiles.calcularDistancia(g.lat, g.lon, escorxador.lat, escorxador.lon);
                double factorPeso = 1.0 - (Math.abs(110 - g.getPesoPromedio()) / 100.0);
                return (factorPeso * 1000) / (dist + 1);
            }).reversed())
            .collect(Collectors.toList());

        while(cupo > 0 && !libres.isEmpty() && !candidatos.isEmpty()) {
            Transport camion = libres.remove(0);
            Ruta ruta = new Ruta(camion, this.escorxador);

            List<Granja> visitadasEnEstaRuta = new ArrayList<>();
            
            // Intentamos llenar el camión
            for(int i=0; i<candidatos.size() && visitadasEnEstaRuta.size() < 3; i++) {
                Granja g = candidatos.get(i);
                if(visitadasEnEstaRuta.contains(g)) continue;

                double peso = g.getPesoPromedio();
                if(peso < 90) continue;

                double espacioKg = ruta.espacioRestanteKg();
                int cabenPorEspacio = (int)(espacioKg / peso);
                
                // Cargamos lo que quepa
                int aCargar = Math.min(Math.min(g.cerdosPendientes, cabenPorEspacio), cupo);

                if(aCargar > 0) {
                    double dist = ruta.paradas.isEmpty() ? 
                        Utiles.calcularDistancia(escorxador.lat, escorxador.lon, g.lat, g.lon) : 
                        Utiles.calcularDistancia(ruta.paradas.get(ruta.paradas.size()-1).lat, ruta.paradas.get(ruta.paradas.size()-1).lon, g.lat, g.lon);
                    
                    ruta.addParada(g, aCargar, dist);
                    
                    cupo -= aCargar;
                    g.cerdosPendientes -= aCargar; // Reducimos lo pendiente
                    visitadasEnEstaRuta.add(g);
                    
                    // Si ya hemos recogido todo lo de la semana, la marcamos como visitada
                    if(g.cerdosPendientes <= 0) {
                        g.ultimEnviamentSetmana = week;
                    }
                }
            }
            
            // Si una granja ha completado su pedido, la quitamos de candidatos para el siguiente camión
            candidatos.removeIf(g -> g.cerdosPendientes <= 0);

            if(!ruta.paradas.isEmpty()) {
                Granja ultima = ruta.paradas.get(ruta.paradas.size()-1);
                ruta.finalizeRouteCost(Utiles.calcularDistancia(ultima.lat, ultima.lon, escorxador.lat, escorxador.lon));
                rutasDelDia.add(ruta);
                costeTransDia += ruta.costTotal;
                ingresosDia += ruta.ingressosTotals;
                cerdosDia += ruta.porcsCarregats;
            }
        }

        // Coste Comida
        double costeComidaDia = 0;
        for(Granja g : granges) if(g.inventario > 0) costeComidaDia += (g.inventario * (consumo.getCosteSemanalPorCerdo(g.edadSemanas)/7.0));

        totalIngresos += ingresosDia;
        totalCosteTransporte += costeTransDia;
        totalCosteComida += costeComidaDia;
        totalCerdosVendidos += cerdosDia;

        addToJs(diaGlobal, cerdosDia, costeTransDia, costeComidaDia, ingresosDia, rutasDelDia);
    }

    private void addToJs(int dia, int cerdos, double cTrans, double cComida, double ingresos, List<Ruta> rutas) {
        double pesoTotalDia = 0;
        for(Ruta r : rutas) for(Granja g : r.paradas) pesoTotalDia += (r.cerdosPorGranja.getOrDefault(g.id, 0) * g.getPesoPromedio());
        
        jsBuilder.append(String.format(Locale.US, "  { \"dia\": %d, \"cerdos\": %d, \"pesoTotal\": %.2f, \"capacidadMataderoPwd\": %.2f, \"cTrans\": %.2f, \"cComida\": %.2f, \"ingresos\": %.2f, \"rutas\": [", dia, cerdos, pesoTotalDia, (cerdos/(double)escorxador.capacidadDiaria)*100, cTrans, cComida, ingresos));
        
        for(Ruta r : rutas) {
            jsBuilder.append(String.format(Locale.US, "{ \"camion_id\": \"%s\", \"carga_actual\": %.2f, \"capacidad_total\": %.0f, \"ocupacion\": 0, \"granjas_visitadas\": %d, \"paradas\": [", r.transporte.id, r.cargaActualKg, r.transporte.capacidadKg, r.paradas.size()));
            for(Granja g : r.paradas) {
                jsBuilder.append(String.format(Locale.US, "{ \"id\": \"%s\", \"lat\": %.4f, \"lon\": %.4f, \"inv_restante\": %d, \"recogidos\": %d, \"peso_medio\": %.2f },", g.id, g.lat, g.lon, g.inventario, r.cerdosPorGranja.getOrDefault(g.id, 0), g.getPesoPromedio()));
            }
            if(!r.paradas.isEmpty()) jsBuilder.setLength(jsBuilder.length() - 1);
            jsBuilder.append("] },");
        }
        if(!rutas.isEmpty()) jsBuilder.setLength(jsBuilder.length() - 1);
        jsBuilder.append("] },\n");
    }

    private void guardarDatosJS() {
        try (FileWriter fw = new FileWriter("web/datos.js")) {
            String content = jsBuilder.toString().trim();
            if (content.endsWith(",")) content = content.substring(0, content.length() - 1);
            
            String resumen = String.format(Locale.US, "];\nconst resumen_global = { \"cerdos\": %d, \"ingresos\": %.2f, \"cTrans\": %.2f, \"cComida\": %.2f, \"beneficio\": %.2f };", totalCerdosVendidos, totalIngresos, totalCosteTransporte, totalCosteComida, (totalIngresos - (totalCosteTransporte + totalCosteComida)));
            
            StringBuilder granjasJs = new StringBuilder("\nconst granjas_todas = [\n");
            for(Granja g : granges) {
                granjasJs.append(String.format(Locale.US, "  { \"id\": \"%s\", \"nombre\": \"%s\", \"lat\": %.4f, \"lon\": %.4f },\n", g.id, (g.nombre != null && !g.nombre.isEmpty() ? g.nombre : g.id), g.lat, g.lon));
            }
            if (!granges.isEmpty()) granjasJs.setLength(granjasJs.length() - 2); 
            granjasJs.append("\n];");

            fw.write(content + resumen + granjasJs.toString());
            System.out.println("✅ JS GENERADO CORRECTAMENTE EN LA CARPETA /WEB.");
        } catch(Exception e) { e.printStackTrace(); }
    }
}