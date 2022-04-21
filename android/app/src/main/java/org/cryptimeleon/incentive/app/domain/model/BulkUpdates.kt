package org.cryptimeleon.incentive.app.domain.model

import java.math.BigInteger
import java.util.*

data class EarnRequestData(
    val promotionId: BigInteger,
    val serializedEarnRequest: String
) : RequestData

data class BulkRequestDto(
    val earnRequestDtoList: List<EarnRequestData>,
    val spendRequestDtoList: List<SpendRequestData>
)

sealed interface RequestData

data class SpendRequestData(
    val promotionId: BigInteger,
    val tokenUpdateId: UUID,
    val serializedSpendRequest: String,
    val serializedMetadata: String
) : RequestData

data class BulkResponseDto(
    val earnTokenUpdateResultDtoList: List<EarnTokenUpdateResultDto>,
    val zkpTokenUpdateResultDtoList: List<ZkpTokenUpdateResultDto>
)

data class EarnTokenUpdateResultDto(val promotionId: BigInteger, val serializedEarnResponse: String)
data class ZkpTokenUpdateResultDto(
    val promotionId: BigInteger,
    val serializedResponse: String,
    val tokenUpdateId: UUID
)
