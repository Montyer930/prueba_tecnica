package co.com.segurosbolivar.repository;
import co.com.segurosbolivar.model.*; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public class InMemoryRiesgoRepository implements RiesgoRepository {
 private final PolizaRepository polizas; public InMemoryRiesgoRepository(PolizaRepository p){polizas=p;}
 public Optional<Riesgo> findById(Long id){return polizas.findAll().stream().flatMap(p->p.getRiesgos().stream()).filter(r->r.getId().equals(id)).findFirst();}
 public void save(Riesgo riesgo){ }
}
