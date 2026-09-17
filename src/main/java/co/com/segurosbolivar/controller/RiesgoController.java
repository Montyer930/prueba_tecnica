package co.com.segurosbolivar.controller;
import co.com.segurosbolivar.model.Riesgo; import co.com.segurosbolivar.service.PolizaService; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/riesgos") public class RiesgoController { private final PolizaService service; public RiesgoController(PolizaService s){service=s;} @PostMapping("/{id}/cancelar") public Riesgo cancelar(@PathVariable Long id){return service.cancelarRiesgo(id);} }
