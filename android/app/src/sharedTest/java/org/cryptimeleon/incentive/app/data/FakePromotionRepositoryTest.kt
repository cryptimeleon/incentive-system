package org.cryptimeleon.incentive.app.data

class FakePromotionRepositoryTest : BasePromotionRepositoryTest() {
    override fun before() {
        promotionRepository = FakePromotionRepository(promotions)
    }

    override fun after() {
    }
}
