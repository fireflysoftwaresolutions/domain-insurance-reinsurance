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

package io.fireflysoftware.domain.insurance.reinsurance.core.lifecycle;

import io.fireflysoftware.core.insurance.reinsurance.sdk.model.TreatyDTO;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Moves copied from the treaty lifecycle on {@code core-insurance-reinsurance}.
 * A missing status is treated as DRAFT, which is the only status the core will delete.
 */
public final class TreatyRecordLifecycle {

    private static final Map<TreatyDTO.TreatyStatusEnum, Set<TreatyDTO.TreatyStatusEnum>> ALLOWED = Map.of(
            TreatyDTO.TreatyStatusEnum.DRAFT, EnumSet.of(TreatyDTO.TreatyStatusEnum.ACTIVE),
            TreatyDTO.TreatyStatusEnum.ACTIVE, EnumSet.of(TreatyDTO.TreatyStatusEnum.RUN_OFF, TreatyDTO.TreatyStatusEnum.EXPIRED),
            TreatyDTO.TreatyStatusEnum.RUN_OFF, EnumSet.of(TreatyDTO.TreatyStatusEnum.EXPIRED)
    );

    private TreatyRecordLifecycle() {
    }

    public static boolean canRemove(TreatyDTO.TreatyStatusEnum status) {
        return status == null || status == TreatyDTO.TreatyStatusEnum.DRAFT;
    }

    public static boolean canTransition(TreatyDTO.TreatyStatusEnum from, TreatyDTO.TreatyStatusEnum to) {
        if (from == null || to == null) {
            return false;
        }
        if (from == to) {
            return true;
        }
        Set<TreatyDTO.TreatyStatusEnum> next = ALLOWED.get(from);
        return next != null && next.contains(to);
    }
}
