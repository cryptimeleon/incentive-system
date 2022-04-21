package org.cryptimeleon.incentive.app.util

import java.text.DecimalFormat

private val formatter = DecimalFormat("##.00€")

fun formatCents(cents: Int) = formatter.format(cents)
