package br.ucsal.vitalis.clinico.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaAcessoRepository extends JpaRepository<AuditoriaAcesso, Long> {
}
