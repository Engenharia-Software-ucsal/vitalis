package br.ucsal.vitalis.clinico.alergia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlergiaRepository extends JpaRepository<Alergia, Long> {

    List<Alergia> findByPacienteIdOrderByAtivaDescRegistradaEmDesc(Long pacienteId);

    List<Alergia> findByPacienteIdAndAtivaTrue(Long pacienteId);

    Optional<Alergia> findByIdAndPacienteId(Long id, Long pacienteId);
}
