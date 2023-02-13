package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.hash.impl.SHAHashAccumulator;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.util.UUID;

public class ContextManager {
    public static UniqueByteRepresentable computeContext(UUID tokenUpdateId, ZkpTokenUpdateMetadata metadata) {
        SHA256HashFunction sha256HashFunction = new SHA256HashFunction();
        JSONConverter jsonConverter = new JSONConverter();

        SHAHashAccumulator shaHashAccumulator = new SHAHashAccumulator("SHA-256");
        shaHashAccumulator.append(tokenUpdateId.toString());
        shaHashAccumulator.append(jsonConverter.serialize(metadata.getRepresentation()));
        return new ByteArrayImplementation(sha256HashFunction.hash(shaHashAccumulator.extractBytes()));
    }
}
