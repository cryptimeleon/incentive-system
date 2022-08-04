package org.cryptimeleon.incentive.app.data

class FakeCryptoRepositoryTest : BaseCryptoRepositoryTest() {
    override fun before() {
        cryptoRepository = FakeCryptoRepository(pp, pkp, ukp)
    }

    override fun after() {}
}
