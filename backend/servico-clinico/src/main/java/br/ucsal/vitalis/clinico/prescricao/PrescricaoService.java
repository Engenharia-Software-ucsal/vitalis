package br.ucsal.vitalis.clinico.prescricao;

import br.ucsal.vitalis.clinico.alergia.Alergia;
import br.ucsal.vitalis.clinico.alergia.AlergiaRepository;
import br.ucsal.vitalis.clinico.atendimento.Atendimento;
import br.ucsal.vitalis.clinico.atendimento.AtendimentoRepository;
import br.ucsal.vitalis.clinico.auditoria.AcaoAuditoria;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PrescricaoService {

    private final PrescricaoRepository prescricaoRepository;
    private final ItemPrescricaoRepository itemPrescricaoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final AlergiaRepository alergiaRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final AuditoriaService auditoriaService;

    public PrescricaoService(PrescricaoRepository prescricaoRepository,
                             ItemPrescricaoRepository itemPrescricaoRepository,
                             MedicamentoRepository medicamentoRepository,
                             AtendimentoRepository atendimentoRepository,
                             AlergiaRepository alergiaRepository,
                             UsuarioAutenticado usuarioAutenticado,
                             AuditoriaService auditoriaService) {
        this.prescricaoRepository = prescricaoRepository;
        this.itemPrescricaoRepository = itemPrescricaoRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.alergiaRepository = alergiaRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PrescricaoResponse criar(Long atendimentoId, NovaPrescricaoRequest req) {
        Atendimento atendimento = atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento nao encontrado"));

        if (atendimento.getStatus().isEncerrado()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "RN-12: Nao e permitido prescrever em atendimento ja encerrado"
            );
        }

        List<Alergia> alergiasAtivas = alergiaRepository.findByPacienteIdAndAtivaTrue(atendimento.getPacienteId());
        if (!alergiasAtivas.isEmpty() && !req.alergiasReconhecidas()) {
            throw new AlergiaNaoReconhecidaException(alergiasAtivas);
        }

        List<Long> medIds = req.itens().stream()
                .map(NovaPrescricaoRequest.NovoItemPrescricaoRequest::medicamentoId)
                .distinct()
                .toList();
        Map<Long, Medicamento> medicamentos = medicamentoRepository.findAllById(medIds).stream()
                .collect(Collectors.toMap(Medicamento::getId, Function.identity()));
        for (Long medId : medIds) {
            if (!medicamentos.containsKey(medId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicamento nao encontrado: " + medId);
            }
        }

        Prescricao prescricao = prescricaoRepository.save(
                new Prescricao(atendimentoId, usuarioAutenticado.id(), req.alergiasReconhecidas())
        );

        List<ItemPrescricao> itensSalvos = req.itens().stream()
                .map(item -> itemPrescricaoRepository.save(new ItemPrescricao(
                        prescricao.getId(),
                        item.medicamentoId(),
                        item.dose().trim(),
                        item.via().trim(),
                        item.frequencia().trim(),
                        item.duracao() == null ? null : item.duracao().trim(),
                        item.quantidade(),
                        item.instrucoes() == null ? null : item.instrucoes().trim()
                )))
                .toList();

        auditoriaService.registrar(AcaoAuditoria.CRIACAO, "prescricao", prescricao.getId(), Map.of(
                "atendimentoId", atendimentoId,
                "pacienteId", atendimento.getPacienteId(),
                "alergiasReconhecidas", req.alergiasReconhecidas(),
                "totalItens", itensSalvos.size()
        ));

        return toResponse(prescricao, itensSalvos, medicamentos);
    }

    public List<PrescricaoResponse> listarPorAtendimento(Long atendimentoId) {
        List<Prescricao> prescricoes = prescricaoRepository.findByAtendimentoIdOrderByCriadaEmDesc(atendimentoId);
        return montarRespostas(prescricoes);
    }

    public List<PrescricaoResponse> listarPorAtendimentos(List<Long> atendimentoIds) {
        if (atendimentoIds.isEmpty()) {
            return List.of();
        }
        List<Prescricao> prescricoes = prescricaoRepository.findByAtendimentoIdInOrderByCriadaEmDesc(atendimentoIds);
        return montarRespostas(prescricoes);
    }

    private List<PrescricaoResponse> montarRespostas(List<Prescricao> prescricoes) {
        if (prescricoes.isEmpty()) {
            return List.of();
        }
        List<Long> prescricaoIds = prescricoes.stream().map(Prescricao::getId).toList();
        List<ItemPrescricao> todosItens = itemPrescricaoRepository.findByPrescricaoIdIn(prescricaoIds);

        List<Long> medIds = todosItens.stream().map(ItemPrescricao::getMedicamentoId).distinct().toList();
        Map<Long, Medicamento> medicamentos = medicamentoRepository.findAllById(medIds).stream()
                .collect(Collectors.toMap(Medicamento::getId, Function.identity()));

        Map<Long, List<ItemPrescricao>> itensPorPrescricao = todosItens.stream()
                .collect(Collectors.groupingBy(ItemPrescricao::getPrescricaoId));

        return prescricoes.stream()
                .map(p -> toResponse(p, itensPorPrescricao.getOrDefault(p.getId(), List.of()), medicamentos))
                .toList();
    }

    private static PrescricaoResponse toResponse(Prescricao p,
                                                 List<ItemPrescricao> itens,
                                                 Map<Long, Medicamento> medicamentos) {
        List<PrescricaoResponse.ItemPrescricaoResponse> itensResp = itens.stream()
                .map(i -> {
                    Medicamento med = medicamentos.get(i.getMedicamentoId());
                    return new PrescricaoResponse.ItemPrescricaoResponse(
                            i.getId(),
                            i.getMedicamentoId(),
                            med != null ? med.getNome() : "Medicamento #" + i.getMedicamentoId(),
                            i.getDose(),
                            i.getVia(),
                            i.getFrequencia(),
                            i.getDuracao(),
                            i.getQuantidade(),
                            i.getInstrucoes()
                    );
                })
                .toList();

        return new PrescricaoResponse(
                p.getId(),
                p.getAtendimentoId(),
                p.getPrescritorId(),
                p.getStatus(),
                p.isAlergiasReconhecidas(),
                p.getCriadaEm(),
                itensResp
        );
    }
}
