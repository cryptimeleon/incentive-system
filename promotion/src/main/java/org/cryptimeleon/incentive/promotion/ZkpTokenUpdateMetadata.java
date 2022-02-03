package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.math.serialization.StandaloneRepresentable;

/**
 * Class for public user input that can be validated and put into the ZKPs as public input, or influence the chosen reward.
 * Must be StandaloneRepresentable for serialization and generic interfaces.
 */
public abstract class ZkpTokenUpdateMetadata implements StandaloneRepresentable {
}

