package br.ucsal.vitalis.ia.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogInferenciaIaRepository extends JpaRepository<LogInferenciaIa, Long> {

    long countByModoContingenciaAtivoTrue();
}
