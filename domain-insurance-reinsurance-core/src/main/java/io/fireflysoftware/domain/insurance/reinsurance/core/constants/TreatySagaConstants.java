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

package io.fireflysoftware.domain.insurance.reinsurance.core.constants;

/**
 * Names shared by the treaty sagas, their steps, and the saga engine.
 */
public final class TreatySagaConstants {

    public static final String SAGA_REGISTER = "RegisterTreatySaga";
    public static final String SAGA_UPDATE_STATUS = "UpdateTreatyStatusSaga";

    public static final String STEP_REGISTER = "registerTreaty";
    public static final String STEP_RECORD_INITIAL_STATUS = "recordInitialStatus";
    public static final String STEP_RETRIEVE = "retrieveTreaty";
    public static final String STEP_UPDATE_STATUS = "updateTreatyStatus";
    public static final String STEP_RECORD_STATUS_HISTORY = "recordStatusHistory";

    public static final String COMPENSATE_REMOVE = "removeTreaty";
    public static final String COMPENSATE_REMOVE_INITIAL_STATUS = "removeInitialStatus";
    public static final String COMPENSATE_RESTORE = "restoreTreatyStatus";
    public static final String COMPENSATE_REMOVE_STATUS_HISTORY = "removeStatusHistory";

    public static final String EVENT_REGISTERED = "treaties.registered";
    public static final String EVENT_INITIAL_STATUS_RECORDED = "treaties.status.recorded";
    public static final String EVENT_RETRIEVED = "treaties.retrieved";
    public static final String EVENT_STATUS_UPDATED = "treaties.status.updated";
    public static final String EVENT_STATUS_HISTORY_RECORDED = "treaties.status.history.recorded";

    public static final String CTX_RECORD = "treatyIdRecord";
    public static final String CTX_PREVIOUS_STATUS = "previousTreatyStatus";

    private TreatySagaConstants() {
    }
}
