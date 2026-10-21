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

package io.fireflysoftware.domain.insurance.reinsurance.core.handlers;

import io.fireflysoftware.core.insurance.reinsurance.sdk.api.TreatiesApi;
import io.fireflysoftware.core.insurance.reinsurance.sdk.model.TreatyDTO;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.GetTreatyQuery;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.QueryHandlerComponent;
import org.fireflyframework.cqrs.query.QueryHandler;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Loads one treaty from the core.
 */
@Slf4j
@QueryHandlerComponent
public class GetTreatyHandler extends QueryHandler<GetTreatyQuery, TreatyDTO> {

    private final TreatiesApi treatiesApi;

    public GetTreatyHandler(TreatiesApi treatiesApi) {
        this.treatiesApi = treatiesApi;
    }

    @Override
    protected Mono<TreatyDTO> doHandle(GetTreatyQuery query) {
        log.debug("Fetching treaty: treatyId={}", query.treatyId());
        return treatiesApi.getTreatyById(query.treatyId(), UUID.randomUUID().toString());
    }
}
