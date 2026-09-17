package co.com.segurosbolivar.repository;
import co.com.segurosbolivar.model.Riesgo; import java.util.*;
public interface RiesgoRepository { Optional<Riesgo> findById(Long id); void save(Riesgo riesgo); }
