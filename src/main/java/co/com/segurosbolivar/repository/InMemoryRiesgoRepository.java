package co.com.segurosbolivar.repository;
import co.com.segurosbolivar.model.*; import org.springframework.stereotype.Repository; import java.util.*; import java.util.concurrent.atomic.AtomicLong;
@Repository public class InMemoryRiesgoRepository implements RiesgoRepository {
 private final PolizaRepository polizas;
 private final AtomicLong secuencia;
 public InMemoryRiesgoRepository(PolizaRepository p){polizas=p;secuencia=new AtomicLong(p.findAll().stream().flatMap(pol->pol.getRiesgos().stream()).mapToLong(Riesgo::getId).max().orElse(0L));}
 public Optional<Riesgo> findById(Long id){return polizas.findAll().stream().flatMap(p->p.getRiesgos().stream()).filter(r->r.getId().equals(id)).findFirst();}
 public long nextId(){return secuencia.incrementAndGet();}
 public void save(Riesgo riesgo){ }
}
