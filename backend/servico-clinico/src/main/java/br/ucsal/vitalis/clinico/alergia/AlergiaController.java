package br.ucsal.vitalis.clinico.alergia;

import br.ucsal.vitalis.clinico.auditoria.AcaoAuditoria;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.paciente.PacienteRepository;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pacientes/{pacienteId}/alergias")
public class AlergiaController {

    private final AlergiaRepository alergiaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final AuditoriaService auditoriaService;

    public AlergiaController(AlergiaRepository alergiaRepository,
                             PacienteRepository pacienteRepository,
                             UsuarioAutenticado usuarioAutenticado,
                             AuditoriaService auditoriaService) {
        this.alergiaRepository = alergiaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Alergia> listar(@PathVariable Long pacienteId) {
        garantirPacienteExiste(pacienteId);
        return alergiaRepository.findByPacienteIdOrderByAtivaDescRegistradaEmDesc(pacienteId);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<Alergia> registrar(@PathVariable Long pacienteId,
                                             @Valid @RequestBody NovaAlergiaRequest req) {
        garantirPacienteExiste(pacienteId);
        Alergia salva = alergiaRepository.save(new Alergia(
                pacienteId,
                req.substancia().trim(),
                req.reacao(),
                req.gravidade(),
                usuarioAutenticado.id()
        ));
        auditoriaService.registrar(AcaoAuditoria.CRIACAO, "alergia", salva.getId(), Map.of(
                "pacienteId", pacienteId,
                "substancia", salva.getSubstancia(),
                "gravidade", salva.getGravidade().name()
        ));
        return ResponseEntity.created(URI.create("/pacientes/" + pacienteId + "/alergias/" + salva.getId()))
                .body(salva);
    }

    @PatchMapping("/{alergiaId}/inativar")
    @Transactional
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMINISTRADOR')")
    public Alergia inativar(@PathVariable Long pacienteId, @PathVariable Long alergiaId) {
        Alergia alergia = alergiaRepository.findByIdAndPacienteId(alergiaId, pacienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alergia nao encontrada"));
        alergia.inativar();
        Alergia salva = alergiaRepository.save(alergia);
        auditoriaService.registrar(AcaoAuditoria.ALTERACAO, "alergia", salva.getId(), Map.of(
                "pacienteId", pacienteId,
                "ativa", false
        ));
        return salva;
    }

    private void garantirPacienteExiste(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente nao encontrado");
        }
    }
}
