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
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.GetTreatyQuery;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.UpdateTreatyCommand;
import org.fireflyframework.cqrs.command.CommandBus;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.core.context.ExecutionContext;
import org.fireflyframework.orchestration.saga.annotation.Saga;
import org.fireflyframework.orchestration.saga.annotation.SagaStep;
import org.fireflyframework.orchestration.saga.annotation.StepEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.COMPENSATE_RESTORE_RECORD;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.CTX_PREVIOUS_RECORD;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.CTX_RECORD;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.EVENT_RECORD_UPDATED;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.EVENT_RETRIEVED;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.SAGA_UPDATE_RECORD;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.STEP_RETRIEVE_FOR_UPDATE;
import static io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants.STEP_UPDATE_RECORD;

/**
 * Updates a treaty on {@code core-insurance-reinsurance} and compensates by writing the previous record back.
 * Status stays on the status saga.
 */
@Saga(name = SAGA_UPDATE_RECORD)
@Service
public class UpdateTreatySaga {

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @Autowired
    public UpdateTreatySaga(CommandBus commandBus, QueryBus queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @SagaStep(id = STEP_RETRIEVE_FOR_UPDATE)
    @StepEvent(type = EVENT_RETRIEVED)
    public Mono<TreatyDTO> retrieveTreatyForUpdate(GetTreatyQuery query, ExecutionContext ctx) {
        return queryBus.query(query)
                .doOnNext(record -> ctx.putVariable(CTX_RECORD, record));
    }

    @SagaStep(id = STEP_UPDATE_RECORD, dependsOn = STEP_RETRIEVE_FOR_UPDATE, compensate = COMPENSATE_RESTORE_RECORD)
    @StepEvent(type = EVENT_RECORD_UPDATED)
    public Mono<TreatyDTO> updateTreaty(UpdateTreatyCommand command, ExecutionContext ctx) {
        TreatyDTO current = ctx.getVariableAs(CTX_RECORD, TreatyDTO.class);
        if (current == null || current.getTreatyId() == null) {
            return Mono.error(new IllegalStateException("Treaty was not found"));
        }
        TreatyDTO incoming = command.record();
        if (incoming == null) {
            return Mono.error(new IllegalArgumentException("Treaty is required"));
        }
        if (current.getTreatyStatus() != null) {
            incoming.setTreatyStatus(current.getTreatyStatus());
        }
        ctx.putVariable(CTX_PREVIOUS_RECORD, current);
        return commandBus.send(new ApplyTreatyStatusCommand(current.getTreatyId(), incoming))
                .doOnNext(updated -> ctx.putVariable(CTX_RECORD, updated));
    }

    public Mono<Void> restoreTreaty(TreatyDTO updated, ExecutionContext ctx) {
        TreatyDTO previous = ctx.getVariableAs(CTX_PREVIOUS_RECORD, TreatyDTO.class);
        if (previous == null || previous.getTreatyId() == null) {
            return Mono.empty();
        }
        return commandBus.send(new ApplyTreatyStatusCommand(previous.getTreatyId(), previous)).then();
    }
}
