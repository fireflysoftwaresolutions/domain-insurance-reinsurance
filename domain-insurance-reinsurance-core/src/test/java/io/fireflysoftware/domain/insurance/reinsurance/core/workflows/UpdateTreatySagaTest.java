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
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.ApplyTreatyStatusCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.UpdateTreatyCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
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
 * Verifies a field update keeps the current status and can be restored.
 */
@ExtendWith(MockitoExtension.class)
class UpdateTreatySagaTest {

    @Mock
    private CommandBus commandBus;

    @Mock
    private QueryBus queryBus;

    private UpdateTreatySaga saga;

    @BeforeEach
    void setUp() {
        saga = new UpdateTreatySaga(commandBus, queryBus);
    }

    @Test
    void update_keepsTheCurrentStatus() {
        UUID id = UUID.randomUUID();
        TreatyDTO current = new TreatyDTO(id);
        current.setTreatyStatus(TreatyDTO.TreatyStatusEnum.DRAFT);
        TreatyDTO incoming = new TreatyDTO();
        incoming.setTreatyStatus(TreatyDTO.TreatyStatusEnum.ACTIVE);
        when(commandBus.send(any(ApplyTreatyStatusCommand.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0, ApplyTreatyStatusCommand.class).record()));

        ExecutionContext ctx = ExecutionContext.forSaga(TreatySagaConstants.SAGA_UPDATE_RECORD, "test");
        ctx.putVariable(TreatySagaConstants.CTX_RECORD, current);

        StepVerifier.create(saga.updateTreaty(new UpdateTreatyCommand(id, incoming), ctx))
                .assertNext(saved -> assertThat(saved.getTreatyStatus()).isEqualTo(TreatyDTO.TreatyStatusEnum.DRAFT))
                .verifyComplete();

        ArgumentCaptor<ApplyTreatyStatusCommand> captor = ArgumentCaptor.forClass(ApplyTreatyStatusCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().treatyId()).isEqualTo(id);
        assertThat(captor.getValue().record().getTreatyStatus()).isEqualTo(TreatyDTO.TreatyStatusEnum.DRAFT);
        assertThat(ctx.getVariableAs(TreatySagaConstants.CTX_PREVIOUS_RECORD, TreatyDTO.class)).isSameAs(current);
    }

    @Test
    void restore_writesThePreviousRecordBack() {
        UUID id = UUID.randomUUID();
        TreatyDTO previous = new TreatyDTO(id);
        previous.setTreatyStatus(TreatyDTO.TreatyStatusEnum.DRAFT);
        TreatyDTO updated = new TreatyDTO(id);
        updated.setTreatyStatus(TreatyDTO.TreatyStatusEnum.ACTIVE);
        when(commandBus.send(any(ApplyTreatyStatusCommand.class))).thenReturn(Mono.just(previous));

        ExecutionContext ctx = ExecutionContext.forSaga(TreatySagaConstants.SAGA_UPDATE_RECORD, "test");
        ctx.putVariable(TreatySagaConstants.CTX_PREVIOUS_RECORD, previous);

        StepVerifier.create(saga.restoreTreaty(updated, ctx))
                .verifyComplete();

        ArgumentCaptor<ApplyTreatyStatusCommand> captor = ArgumentCaptor.forClass(ApplyTreatyStatusCommand.class);
        verify(commandBus).send(captor.capture());
        assertThat(captor.getValue().record()).isSameAs(previous);
    }
}
