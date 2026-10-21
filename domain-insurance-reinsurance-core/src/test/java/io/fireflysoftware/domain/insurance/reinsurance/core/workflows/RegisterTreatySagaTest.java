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

package io.fireflysoftware.domain.insurance.reinsurance.core.workflows;

import io.fireflysoftware.core.insurance.reinsurance.sdk.model.TreatyDTO;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.CreateTreatyCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.RemoveTreatyCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants;

import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the register saga stores the core result and can compensate by deleting it.
 */
@ExtendWith(MockitoExtension.class)
class RegisterTreatySagaTest {

    @Mock
    private CommandBus commandBus;

    private RegisterTreatySaga saga;

    @BeforeEach
    void setUp() {
        saga = new RegisterTreatySaga(commandBus);
    }

    @Test
    void register_storesTheRecordReturnedByTheCore() {
        UUID id = UUID.randomUUID();
        TreatyDTO created = new TreatyDTO(id);
        created.setTreatyStatus(TreatyDTO.TreatyStatusEnum.DRAFT);
        when(commandBus.send(any(CreateTreatyCommand.class))).thenReturn(Mono.just(created));

        ExecutionContext ctx = ExecutionContext.forSaga(TreatySagaConstants.SAGA_REGISTER, "test");
        TreatyDTO request = new TreatyDTO();
        request.setTreatyStatus(TreatyDTO.TreatyStatusEnum.DRAFT);

        StepVerifier.create(saga.registerTreaty(new CreateTreatyCommand(request), ctx))
                .expectNext(created)
                .verifyComplete();

        assertThat(ctx.getVariableAs(TreatySagaConstants.CTX_RECORD, TreatyDTO.class).getTreatyId()).isEqualTo(id);
    }

    @Test
    void remove_sendsDeleteForTheCreatedRecord() {
        UUID id = UUID.randomUUID();
        when(commandBus.send(any(RemoveTreatyCommand.class))).thenReturn(Mono.empty());

        StepVerifier.create(saga.removeTreaty(new TreatyDTO(id)))
                .verifyComplete();

        ArgumentCaptor<RemoveTreatyCommand> captor = ArgumentCaptor.forClass(RemoveTreatyCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().treatyId()).isEqualTo(id);
    }

}
