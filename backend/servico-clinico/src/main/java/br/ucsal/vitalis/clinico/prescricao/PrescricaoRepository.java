package br.ucsal.vitalis.clinico.prescricao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PrescricaoRepository extends JpaRepository<Prescricao, Long> {

    List<Prescricao> findByAtendimentoIdOrderByCriadaEmDesc(Long atendimentoId);

    List<Prescricao> findByAtendimentoIdInOrderByCriadaEmDesc(Collection<Long> atendimentoIds);
}
