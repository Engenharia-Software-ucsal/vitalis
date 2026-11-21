package br.ucsal.vitalis.ia.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisaoHumanaIaRepository extends JpaRepository<DecisaoHumanaIa, Long> {

    boolean existsByLogInferenciaId(Long logInferenciaId);

    long countByDecisaoMedica(DecisaoMedica decisaoMedica);
}
