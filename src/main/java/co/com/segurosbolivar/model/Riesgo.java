package co.com.segurosbolivar.model;
public class Riesgo {
    private Long id; private Long polizaId; private Estado estado=Estado.ACTIVA;
    public Riesgo(Long id, Long polizaId){this.id=id;this.polizaId=polizaId;}
    public Long getId(){return id;} public Long getPolizaId(){return polizaId;} public Estado getEstado(){return estado;}
    public void cancelar(){estado=Estado.CANCELADA;}
}
