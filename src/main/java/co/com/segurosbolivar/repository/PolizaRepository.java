package co.com.segurosbolivar.repository;
import co.com.segurosbolivar.model.Poliza; import java.util.*;
public interface PolizaRepository { List<Poliza> findAll(); Optional<Poliza> findById(Long id); void save(Poliza poliza); }
