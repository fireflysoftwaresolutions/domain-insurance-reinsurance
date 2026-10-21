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
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.UpdateTreatyStatusCommand;
import io.fireflysoftware.domain.insurance.reinsurance.core.commands.UpdateTreatyCommand;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Application service for treaty orchestration.
 */
public interface ReinsuranceService {

    /**
     * Creates a treaty through the register saga.
     *
     * @param command the create command
     * @return the record returned by the core
     */
    Mono<TreatyDTO> create(CreateTreatyCommand command);

    /**
     * Moves a treaty to a new status through the core.
     *
     * @param command the status change
     * @return the updated record
     */
    Mono<TreatyDTO> updateStatus(UpdateTreatyStatusCommand command);

    /**
     * Reads one treaty.
     *
     * @param treatyId the core identifier
     * @return the record, or empty when the core has none
     */
    Mono<TreatyDTO> get(UUID treatyId);

    /**
     * Updates fields on a treaty and leaves its status unchanged.
     *
     * @param command the update
     * @return the record returned by the core
     */
    Mono<TreatyDTO> update(UpdateTreatyCommand command);

    /**
     * Expires a treaty when the reinsurance core allows EXPIRED.
     *
     * @param treatyId the core identifier
     * @param statusReason optional reason stored by the status saga when the core keeps one
     * @return the updated record
     */
    Mono<TreatyDTO> expire(UUID treatyId, String statusReason);

    /**
     * Deletes a treaty when the core lifecycle allows removal.
     *
     * @param treatyId the core identifier
     * @return completion when the core has deleted the record
     */
    Mono<Void> remove(UUID treatyId);
}
