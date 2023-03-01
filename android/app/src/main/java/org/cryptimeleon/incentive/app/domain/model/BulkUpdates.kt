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

data class BulkRequestStoreDto(
    val basketId: UUID,
    val earnRequestStoreDtoList: List<EarnRequestStoreDto>,
    val spendRequestStoreDtoList: List<SpendRequestStoreDto>,
)

data class EarnRequestStoreDto(
    val promotionId: BigInteger,
    val serializedRequest: String
)

data class SpendRequestStoreDto(
    val serializedRequest: String,
    val promotionId: BigInteger,
    val tokenUpdateId: UUID,
    val serializedTokenUpdateMetadata: String
)

data class BulkResultStoreDto(
    val earnResults: List<EarnResultStoreDto>,
    val spendResults: List<SpendResultStoreDto>
)

data class EarnResultStoreDto(
    val promotionId: BigInteger,
    val serializedEarnCouponSignature: String
)

data class SpendResultStoreDto(
    val promotionId: BigInteger,
    val serializedSpendCouponSignature: String
)


data class BulkRequestProviderDto(
    val spendRequests: List<SpendRequestProviderDto>,
    val earnRequests: List<EarnRequestProviderDto>
)

data class SpendRequestProviderDto(
    val promotionId: BigInteger,
    val serializedSpendRequest: String,
    val serializedTokenUpdateMetadata: String,
    val basketId: UUID,
    val tokenUpdateId: UUID,
    val basketPoints: List<BigInteger>
)

data class EarnRequestProviderDto(
    val promotionId: BigInteger,
    val serializedEarnRequestECDSA: String
)

data class BulkResultsProviderDto(
    val earnResults: List<EarnResultProviderDto>,
    val spendResults: List<SpendResultProviderDto>
)

data class EarnResultProviderDto(
    val promotionId: BigInteger,
    val serializedEarnResponse: String
)

data class SpendResultProviderDto(
    val promotionId: BigInteger,
    val serializedSpendResult: String
)
