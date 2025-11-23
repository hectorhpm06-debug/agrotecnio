package src;
public class Escorxador {
    public String id;
    public double lat;
    public double lon;
    public int capacidadDiaria;
    

    public double precioPorKg;
    public double minPeso15, maxPeso15;
    public double minPeso20, maxPeso20;
    public double minPesoOptimo = 105.0;
    public double maxPesoOptimo = 115.0;

    
    public Escorxador(String id, double lat, double lon, int capacidadDiaria,
                      double precioPorKg, double minPeso15, double maxPeso15,
                      double minPeso20, double maxPeso20) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.capacidadDiaria = capacidadDiaria;
        this.precioPorKg = precioPorKg;
        this.minPeso15 = minPeso15;
        this.maxPeso15 = maxPeso15;
        this.minPeso20 = minPeso20;
        this.maxPeso20 = maxPeso20;
    }
}