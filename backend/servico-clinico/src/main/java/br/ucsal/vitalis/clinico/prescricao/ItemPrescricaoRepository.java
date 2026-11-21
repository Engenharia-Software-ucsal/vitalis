package br.ucsal.vitalis.clinico.prescricao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ItemPrescricaoRepository extends JpaRepository<ItemPrescricao, Long> {

    List<ItemPrescricao> findByPrescricaoId(Long prescricaoId);

    List<ItemPrescricao> findByPrescricaoIdIn(Collection<Long> prescricaoIds);
}
