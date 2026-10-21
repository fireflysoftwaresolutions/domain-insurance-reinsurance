/*
 * Copyright 2026 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fireflysoftware.domain.insurance.reinsurance.web.controllers;

import io.fireflysoftware.core.insurance.reinsurance.sdk.model.TreatyDTO;
import io.fireflysoftware.domain.insurance.reinsurance.core.services.ReinsuranceService;
import io.fireflysoftware.domain.insurance.reinsurance.interfaces.dtos.ReasonRequest;
import io.fireflysoftware.domain.insurance.reinsurance.interfaces.dtos.StatusChangeRequest;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Web slice tests for the treaty domain API.
 */
@WebFluxTest(TreatiesController.class)
class TreatiesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReinsuranceService service;

    @MockitoBean
    private ExceptionConverterService exceptionConverterService;

    @MockitoBean
    private ErrorHandlingProperties errorHandlingProperties;

    @MockitoBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    @Test
    void create_returns201() {
        when(service.create(any())).thenReturn(Mono.just(new TreatyDTO()));

        webTestClient.post()
                .uri("/api/v1/treaties")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TreatyDTO())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TreatyDTO.class);
    }

    @Test
    void get_returns200WhenFound() {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenReturn(Mono.just(new TreatyDTO(id)));

        webTestClient.get()
                .uri("/api/v1/treaties/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TreatyDTO.class);
    }

    @Test
    void get_returns404WhenMissing() {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/treaties/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateStatus_returns200() {
        UUID id = UUID.randomUUID();
        when(service.updateStatus(any())).thenReturn(Mono.just(new TreatyDTO(id)));
        StatusChangeRequest request = new StatusChangeRequest();
        request.setStatus("ACTIVE");
        request.setStatusReason("Caller request");

        webTestClient.put()
                .uri("/api/v1/treaties/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TreatyDTO.class);
    }

    @Test
    void update_returns200() {
        UUID id = UUID.randomUUID();
        when(service.update(any())).thenReturn(Mono.just(new TreatyDTO(id)));

        webTestClient.put()
                .uri("/api/v1/treaties/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new TreatyDTO())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TreatyDTO.class);
    }

    @Test
    void expire_returns200() {
        UUID id = UUID.randomUUID();
        when(service.expire(any(), any())).thenReturn(Mono.just(new TreatyDTO(id)));
        ReasonRequest request = new ReasonRequest();
        request.setStatusReason("Caller request");

        webTestClient.post()
                .uri("/api/v1/treaties/{id}/expire", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TreatyDTO.class);
    }

    @Test
    void remove_returns204() {
        UUID id = UUID.randomUUID();
        when(service.remove(id)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/treaties/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }
}
