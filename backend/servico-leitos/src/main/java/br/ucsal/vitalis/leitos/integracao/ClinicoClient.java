package br.ucsal.vitalis.leitos.integracao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ClinicoClient {

    private final RestClient restClient;

    public ClinicoClient(RestClient.Builder builder, @Value("${vitalis.clinico-url}") String clinicoUrl) {
        this.restClient = builder.baseUrl(clinicoUrl).build();
    }

    public ElegibilidadeInternacaoResponse verificarElegibilidadeInternacao(Long atendimentoId, String bearerToken) {
        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri("/atendimentos/{atendimentoId}/elegibilidade-internacao", atendimentoId);

            if (bearerToken != null && !bearerToken.isBlank()) {
                String headerValue = bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken;
                request = request.header(HttpHeaders.AUTHORIZATION, headerValue);
            }

            ElegibilidadeInternacaoResponse response = request.retrieve()
                    .body(ElegibilidadeInternacaoResponse.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "servico clinico indisponivel");
            }
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "atendimento nao encontrado");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "servico clinico indisponivel");
        }
    }
}
