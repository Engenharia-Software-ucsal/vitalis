package br.ucsal.vitalis.clinico.triagem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    List<Triagem> findByAtendimentoIdOrderByRealizadaEmDesc(Long atendimentoId);

    Optional<Triagem> findFirstByAtendimentoIdOrderByRealizadaEmDesc(Long atendimentoId);

    boolean existsByAtendimentoId(Long atendimentoId);

    List<Triagem> findByAtendimentoIdInOrderByRealizadaEmDesc(Collection<Long> atendimentoIds);
}
