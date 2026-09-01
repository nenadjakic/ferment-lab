package com.github.nenadjakic.fermentlab.common.extension

import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeResponse
import com.github.nenadjakic.fermentlab.common.dto.reference.FermentTypeSimpleResponse
import com.github.nenadjakic.fermentlab.data.entity.reference.FermentType

fun FermentType.toResponse(): FermentTypeResponse =
    FermentTypeResponse(
        id = requireNotNull(id),
        code = code,
        name = name,
        category = category,
        description = description,
        defaultDurationDays = defaultDurationDays,
        targetPhMin = targetPhMin,
        targetPhMax = targetPhMax,
        overFermentationPh = overFermentationPh,
        targetTempMinC = targetTempMinC,
        targetTempMaxC = targetTempMaxC,
        accentColor = accentColor
    )

fun FermentType.toSimpleResponse(): FermentTypeSimpleResponse =
    FermentTypeSimpleResponse(
        id = requireNotNull(id),
        code = code,
        name = name,
        category = category,
        accentColor = accentColor
    )
