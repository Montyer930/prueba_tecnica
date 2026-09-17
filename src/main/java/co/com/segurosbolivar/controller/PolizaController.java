package co.com.segurosbolivar.controller;
import co.com.segurosbolivar.model.*; import co.com.segurosbolivar.service.PolizaService; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/polizas") public class PolizaController {
 private final PolizaService service; public PolizaController(PolizaService s){service=s;}
 @GetMapping public List<Poliza> listar(@RequestParam(required=false) TipoPoliza tipo,@RequestParam(required=false) Estado estado){return service.listar(tipo,estado);}
 @GetMapping("/{id}/riesgos") public List<Riesgo> riesgos(@PathVariable Long id){return service.riesgos(id);}
 @PostMapping("/{id}/renovar") public Poliza renovar(@PathVariable Long id){return service.renovar(id);}
 @PostMapping("/{id}/cancelar") public Poliza cancelar(@PathVariable Long id){return service.cancelar(id);}
 @PostMapping("/{id}/riesgos") @ResponseStatus(HttpStatus.CREATED) public Riesgo agregar(@PathVariable Long id){return service.agregarRiesgo(id);}
}
