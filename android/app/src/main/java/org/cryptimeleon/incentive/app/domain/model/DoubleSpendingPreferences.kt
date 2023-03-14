package org.cryptimeleon.incentive.app.domain.model

data class DoubleSpendingPreferences(val discardUpdatedToken: Boolean, val stopAfterPay: Boolean)
