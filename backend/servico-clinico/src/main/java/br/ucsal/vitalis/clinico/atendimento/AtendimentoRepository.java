package br.ucsal.vitalis.clinico.atendimento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByStatusIn(Collection<StatusAtendimento> statuses);

    List<Atendimento> findByPacienteIdOrderByIniciadoEmDesc(Long pacienteId);
}
