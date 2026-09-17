package co.com.segurosbolivar.service;
import co.com.segurosbolivar.model.*; import co.com.segurosbolivar.repository.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import java.math.*; import java.util.*;
@Service public class PolizaService {
 private final PolizaRepository polizas; private final RiesgoRepository riesgos; private final BigDecimal ipc;
 public PolizaService(PolizaRepository p,RiesgoRepository r,@Value("${app.ipc}") BigDecimal ipc){polizas=p;riesgos=r;this.ipc=ipc;}
 public List<Poliza> listar(TipoPoliza tipo,Estado estado){return polizas.findAll().stream().filter(p->tipo==null||p.getTipo()==tipo).filter(p->estado==null||p.getEstado()==estado).toList();}
 public Poliza obtener(Long id){return polizas.findById(id).orElseThrow(()->new BusinessException("Póliza no encontrada",404));}
 public List<Riesgo> riesgos(Long id){return obtener(id).getRiesgos();}
 public Poliza renovar(Long id){var p=obtener(id);if(p.getEstado()==Estado.CANCELADA)throw new BusinessException("No se puede renovar una póliza cancelada",409);p.renovar(ipc);polizas.save(p);return p;}
 public Poliza cancelar(Long id){var p=obtener(id);p.cancelar();polizas.save(p);return p;}
 public Riesgo agregarRiesgo(Long id){var p=obtener(id);if(p.getEstado()==Estado.CANCELADA)throw new BusinessException("No se puede agregar riesgos a una póliza cancelada",409);if(p.getTipo()!=TipoPoliza.COLECTIVA)throw new BusinessException("Solo las pólizas colectivas permiten riesgos",409);long nuevo=riesgos.findById(1L).map(x->p.getRiesgos().stream().mapToLong(Riesgo::getId).max().orElse(0L)+1).orElse(1L);var r=new Riesgo(nuevo,p.getId());p.agregarRiesgo(r);riesgos.save(r);polizas.save(p);return r;}
 public Riesgo cancelarRiesgo(Long id){var r=riesgos.findById(id).orElseThrow(()->new BusinessException("Riesgo no encontrado",404));r.cancelar();riesgos.save(r);return r;}
}
