package org.cryptimeleon.incentive.app.util

import org.cryptimeleon.incentive.crypto.model.Token
import org.cryptimeleon.math.structures.cartesian.Vector
import java.math.BigInteger
import java.util.function.Function

fun Token.toBigIntVector(): Vector<BigInteger> =
    points.map(Function { it.asInteger() })
