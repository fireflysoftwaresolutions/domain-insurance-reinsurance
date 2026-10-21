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

package io.fireflysoftware.domain.insurance.reinsurance.core.services;

import io.fireflysoftware.core.insurance.reinsurance.sdk.model.TreatyDTO;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.CreateTreatyCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.GetTreatyQuery;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.UpdateTreatyStatusCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.constants.TreatySagaConstants;
import org.fireflyframework.cqrs.query.QueryBus;
import org.fireflyframework.orchestration.saga.engine.SagaEngine;
import org.fireflyframework.orchestration.saga.engine.SagaResult;
import org.fireflyframework.orchestration.saga.engine.StepInputs;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Runs the treaty sagas and reads through the query bus.
 */
@Service
public class ReinsuranceServiceImpl implements ReinsuranceService {

    private final SagaEngine sagaEngine;
    private final QueryBus queryBus;

    public ReinsuranceServiceImpl(SagaEngine sagaEngine, QueryBus queryBus) {
        this.sagaEngine = sagaEngine;
        this.queryBus = queryBus;
    }

    @Override
    public Mono<TreatyDTO> create(CreateTreatyCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(TreatySagaConstants.STEP_REGISTER, command)
                .build();
        return sagaEngine.execute(TreatySagaConstants.SAGA_REGISTER, inputs)
                .flatMap(result -> outcome(result, TreatySagaConstants.STEP_REGISTER, TreatyDTO.class));
    }

    @Override
    public Mono<TreatyDTO> updateStatus(UpdateTreatyStatusCommand command) {
        StepInputs inputs = StepInputs.builder()
                .forStepId(TreatySagaConstants.STEP_RETRIEVE, new GetTreatyQuery(command.treatyId()))
                .forStepId(TreatySagaConstants.STEP_UPDATE_STATUS, command)
                .build();
        return sagaEngine.execute(TreatySagaConstants.SAGA_UPDATE_STATUS, inputs)
                .flatMap(result -> outcome(result, TreatySagaConstants.STEP_UPDATE_STATUS, TreatyDTO.class));
    }

    @Override
    public Mono<TreatyDTO> get(UUID treatyId) {
        return queryBus.query(new GetTreatyQuery(treatyId));
    }

    private static <T> Mono<T> outcome(SagaResult result, String stepId, Class<T> type) {
        if (result.isFailed()) {
            Throwable error = result.error().orElseGet(() ->
                    new IllegalStateException("Saga " + result.sagaName() + " failed"));
            return Mono.error(error);
        }
        return Mono.justOrEmpty(result.resultOf(stepId, type))
                .switchIfEmpty(Mono.error(new IllegalStateException("Saga step " + stepId + " produced no result")));
    }
}
