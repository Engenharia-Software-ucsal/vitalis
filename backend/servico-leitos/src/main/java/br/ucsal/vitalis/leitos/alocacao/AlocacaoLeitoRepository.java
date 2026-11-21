package br.ucsal.vitalis.leitos.alocacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlocacaoLeitoRepository extends JpaRepository<AlocacaoLeito, Long> {

    Optional<AlocacaoLeito> findByLeitoIdAndLiberadoEmIsNull(Long leitoId);

    Optional<AlocacaoLeito> findByAtendimentoIdAndLiberadoEmIsNull(Long atendimentoId);

    List<AlocacaoLeito> findByLiberadoEmIsNull();
}
