package src;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ruta {
    public Transport transporte;
    public double cargaActualKg;
    public List<Granja> paradas;
    public double costTotal;
    public double ingressosTotals;
    public int porcsCarregats;
    public double distanciaTotal;
    public Escorxador matadero;
    
    public Map<String, Integer> cerdosPorGranja; 

    public Ruta(Transport transporte, Escorxador matadero) {
        this.transporte = transporte;
        this.matadero = matadero;
        this.cargaActualKg = 0.0;
        this.paradas = new ArrayList<>();
        this.cerdosPorGranja = new HashMap<>(); 
        this.costTotal = 0;
        this.ingressosTotals = 0;
        this.porcsCarregats = 0;
        this.distanciaTotal = 0;
    }

    public void addParada(Granja granja, int numPorcs, double distKm) {
        this.paradas.add(granja);
        this.porcsCarregats += numPorcs;
        this.distanciaTotal += distKm;
        this.cerdosPorGranja.put(granja.id, numPorcs);

        double peso = granja.getPesoPromedio();
        
        
        // Sumamos (Cerdos * Peso) a la carga actual. 
        this.cargaActualKg += (numPorcs * peso);

        // Cálculo de Penalizaciones 
        double factorPenalizacion = 0.0;
        if (peso < matadero.minPeso20 || peso > matadero.maxPeso20) { 
             factorPenalizacion = 0.20;
        } else if (peso < matadero.minPeso15 || peso > matadero.maxPeso15) { 
             factorPenalizacion = 0.15;
        } else if (peso > 120) { 
             factorPenalizacion = 0.20;
        }

        double factorIngreso = 1.0 - factorPenalizacion;
        
        // Ingresos
        double ingresosParada = numPorcs * peso * matadero.precioPorKg * factorIngreso;
        this.ingressosTotals += ingresosParada;
        
        granja.inventario -= numPorcs;
    }

    public void finalizeRouteCost(double distRetorno) {
        this.distanciaTotal += distRetorno;
        // Calcula coste variable según distancia
        this.costTotal = this.distanciaTotal * this.transporte.costePorKm;
    }

    public double espacioRestanteKg() {
        // Devuelve el espacio libre 
        return this.transporte.capacidadKg - this.cargaActualKg;
    }
}