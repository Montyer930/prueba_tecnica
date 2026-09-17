package co.com.segurosbolivar.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Poliza {
    private Long id; private TipoPoliza tipo; private Estado estado;
    private BigDecimal canonMensual; private BigDecimal prima;
    private final List<Riesgo> riesgos = new ArrayList<>();
    public Poliza(Long id, TipoPoliza tipo, BigDecimal canonMensual, BigDecimal prima) {
        this.id=id; this.tipo=tipo; this.estado=Estado.ACTIVA; this.canonMensual=canonMensual; this.prima=prima;
    }
    public Long getId(){return id;} public TipoPoliza getTipo(){return tipo;} public Estado getEstado(){return estado;}
    public BigDecimal getCanonMensual(){return canonMensual;} public BigDecimal getPrima(){return prima;}
    public List<Riesgo> getRiesgos(){return riesgos;}
    public void renovar(BigDecimal ipc){ canonMensual=canonMensual.multiply(BigDecimal.ONE.add(ipc)); prima=prima.multiply(BigDecimal.ONE.add(ipc)); estado=Estado.RENOVADA; }
    public void cancelar(){estado=Estado.CANCELADA; riesgos.forEach(Riesgo::cancelar);}
    public void agregarRiesgo(Riesgo riesgo){riesgos.add(riesgo);}
}
