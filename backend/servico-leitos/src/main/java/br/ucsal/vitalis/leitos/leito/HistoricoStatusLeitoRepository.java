package br.ucsal.vitalis.leitos.leito;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoStatusLeitoRepository extends JpaRepository<HistoricoStatusLeito, Long> {

    List<HistoricoStatusLeito> findByLeitoIdOrderByAlteradoEmDesc(Long leitoId);
}
