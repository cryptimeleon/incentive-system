package org.cryptimeleon.incentive.app.data

class FakeCryptoRepositoryTest : BaseCryptoRepositoryTest() {
    override fun before() {
        cryptoRepository = FakeCryptoRepository(pp, pkp, incentiveSystem.generateUserKeys())
    }

    override fun after() {}
}