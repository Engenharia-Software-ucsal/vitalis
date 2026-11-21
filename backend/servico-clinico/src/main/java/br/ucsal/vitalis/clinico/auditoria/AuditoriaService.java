package br.ucsal.vitalis.clinico.auditoria;

import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditoriaService {

    private final AuditoriaAcessoRepository repository;
    private final UsuarioAutenticado usuarioAutenticado;

    public AuditoriaService(AuditoriaAcessoRepository repository, UsuarioAutenticado usuarioAutenticado) {
        this.repository = repository;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public void registrar(AcaoAuditoria acao, String recurso, Object recursoId, Map<String, Object> metadados) {
        registrar(acao, recurso, recursoId, null, metadados);
    }

    public void registrar(AcaoAuditoria acao,
                          String recurso,
                          Object recursoId,
                          String justificativa,
                          Map<String, Object> metadados) {
        AuditoriaAcesso registro = new AuditoriaAcesso(
                usuarioAutenticado.idOuNull(),
                acao,
                recurso,
                recursoId == null ? null : String.valueOf(recursoId),
                justificativa,
                metadados
        );
        repository.save(registro);
    }
}
