package br.ucsal.vitalis.clinico.prontuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvolucaoClinicaRepository extends JpaRepository<EvolucaoClinica, Long> {

    List<EvolucaoClinica> findByProntuarioIdOrderByCriadaEmDesc(Long prontuarioId);
}
