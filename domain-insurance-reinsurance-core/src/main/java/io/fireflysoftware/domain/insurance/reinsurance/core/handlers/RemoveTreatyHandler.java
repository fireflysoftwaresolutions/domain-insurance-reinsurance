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
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.RemoveTreatyCommand;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.cqrs.annotations.CommandHandlerComponent;
import org.fireflyframework.cqrs.command.CommandHandler;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Deletes a treaty through the core during saga compensation.
 */
@Slf4j
@CommandHandlerComponent
public class RemoveTreatyHandler extends CommandHandler<RemoveTreatyCommand, Void> {

    private final TreatiesApi treatiesApi;

    public RemoveTreatyHandler(TreatiesApi treatiesApi) {
        this.treatiesApi = treatiesApi;
    }

    @Override
    protected Mono<Void> doHandle(RemoveTreatyCommand command) {
        log.debug("Removing treaty: treatyId={}", command.treatyId());
        return treatiesApi.deleteTreaty(command.treatyId(), UUID.randomUUID().toString()).then();
    }
}
