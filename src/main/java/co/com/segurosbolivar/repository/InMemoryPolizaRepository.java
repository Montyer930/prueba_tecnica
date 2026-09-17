package co.com.segurosbolivar.repository;
import co.com.segurosbolivar.model.*; import org.springframework.stereotype.Repository; import java.math.BigDecimal; import java.util.*; import java.util.concurrent.ConcurrentHashMap;
@Repository public class InMemoryPolizaRepository implements PolizaRepository {
 private final Map<Long,Poliza> data=new ConcurrentHashMap<>();
 public InMemoryPolizaRepository(){var colectiva=new Poliza(555L,TipoPoliza.COLECTIVA,new BigDecimal("1000000"),new BigDecimal("500000")); colectiva.agregarRiesgo(new Riesgo(1L,555L)); data.put(555L,colectiva); var individual=new Poliza(556L,TipoPoliza.INDIVIDUAL,new BigDecimal("800000"),new BigDecimal("300000")); individual.agregarRiesgo(new Riesgo(2L,556L)); data.put(556L,individual);}
 public List<Poliza> findAll(){return new ArrayList<>(data.values());} public Optional<Poliza> findById(Long id){return Optional.ofNullable(data.get(id));} public void save(Poliza p){data.put(p.getId(),p);}
}
