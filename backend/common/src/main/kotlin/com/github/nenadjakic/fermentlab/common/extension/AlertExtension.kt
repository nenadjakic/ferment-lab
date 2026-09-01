package com.github.nenadjakic.fermentlab.common.extension

import com.github.nenadjakic.fermentlab.common.dto.alert.AlertResponse
import com.github.nenadjakic.fermentlab.data.entity.alert.Alert

fun Alert.toResponse(): AlertResponse =
    AlertResponse(
        id = requireNotNull(id),
        batchId = batch?.id,
        batchCode = batch?.code,
        batchName = batch?.name,
        cultureId = culture?.id,
        cultureName = culture?.name,
        alertType = alertType,
        severity = severity,
        message = message,
        dueAt = dueAt,
        acknowledgedAt = acknowledgedAt,
        createdAt = createdAt
    )
