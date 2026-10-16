package co.com.segurosbolivar.repository;
import co.com.segurosbolivar.model.Riesgo; import java.util.*;
public interface RiesgoRepository { Optional<Riesgo> findById(Long id); long nextId(); void save(Riesgo riesgo); }
