package br.ucsal.vitalis.leitos.leito;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeitoRepository extends JpaRepository<Leito, Long> {

    List<Leito> findByStatus(StatusLeito status);

    List<Leito> findByAlaIgnoreCase(String ala);

    List<Leito> findByStatusAndAlaIgnoreCase(StatusLeito status, String ala);

    boolean existsByCodigo(String codigo);
}
