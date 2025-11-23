package src;
public class Transport {
    public String id;
    public double capacidadKg;
    public double costePorKm;
    public int maxHorasSemana;
    public double costeFijoSemanal; 

    public Transport(String id, double capacidadTons, double costePorKm, int maxHorasSemana, double costeFijoSemanal) {
        this.id = id;
        this.capacidadKg = capacidadTons;
        this.costePorKm = costePorKm;
        this.maxHorasSemana = maxHorasSemana;
        this.costeFijoSemanal = costeFijoSemanal;
    }
}