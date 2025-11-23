package src;
public class Granja {
    public String id;
    public String nombre;
    public double lat;
    public double lon;
    public int inventario;
    public double pesoPromedio;
    public double tasaCrecimiento;
    public int edadSemanas;
    public double precioPorKg;
    public int cerdosConsumo;
    public int capacidadMaxima;
    
    // Control de simulación
    public int ultimEnviamentSetmana = 0;
    public int cerdosPendientes = 0; 
    
    public Granja(String id, String nombre, double lat, double lon, int inventario, 
                  double pesoPromedio, double tasaCrecimiento, int edadSemanas, 
                  double precioPorKg, int cerdosConsumo, int capacidadMaxima) {
        this.id = id;
        this.nombre = nombre;
        this.lat = lat;
        this.lon = lon;
        this.inventario = inventario;
        this.pesoPromedio = pesoPromedio;
        this.tasaCrecimiento = tasaCrecimiento;
        this.edadSemanas = edadSemanas;
        this.precioPorKg = precioPorKg;
        this.cerdosConsumo = cerdosConsumo;
        this.capacidadMaxima = capacidadMaxima;
    }

    public void updateWeeklyGrowth() {
        this.edadSemanas++;
    }
    
    public double getPesoPromedio() {
        return Crecimiento.getPeso(this.edadSemanas);
    }
}