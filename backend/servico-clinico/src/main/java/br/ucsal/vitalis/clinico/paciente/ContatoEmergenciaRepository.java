package br.ucsal.vitalis.clinico.paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, Long> {

    List<ContatoEmergencia> findByPacienteIdOrderByPrincipalDescIdAsc(Long pacienteId);

    Optional<ContatoEmergencia> findByIdAndPacienteId(Long id, Long pacienteId);
}
