package proyecto1.mscredit.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mscredit.dto.CustomerDTO;
import proyecto1.mscredit.dto.CustomerResponse;
import reactor.core.publisher.Mono;

@Component
public class CustomerClient {

    private final WebClient webClient;

    public CustomerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    public Mono<CustomerDTO> getCustomerById(String customerId) {
        return webClient.get()
                .uri("/v1.0/customers/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Cliente no encontrado")))
                .onStatus(HttpStatus::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Error en el servicio ms-customer")))
                .bodyToMono(CustomerResponse.class)  // Captura la estructura completa
                .flatMap(response -> {
                    if (response.getData() == null || response.getData().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
                    }
                    return Mono.just(response.getData().get(0)); // Retorna el primer cliente
                });
    }
}
