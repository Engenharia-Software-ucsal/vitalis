package br.ucsal.vitalis.clinico.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SeedDemoPasswordTest {

    private static final String SEED_BCRYPT_HASH =
            "$2b$10$q2gZksCOWCOLWF3W4qH04.N/cqQMAjuD9HxCJEyV8WbQlzrmz7ws2";

    @Test
    void hashDoSeedDemoDeveCorresponderASenhaPadrao() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches("Vitalis@2026", SEED_BCRYPT_HASH)).isTrue();
    }
}
