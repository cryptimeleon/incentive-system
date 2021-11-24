package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;


/**
 * Implements the connectivity to the double-spending protection database.
 * This comprises methods for adding transaction and token nodes to a bipartite graph and both types of directed edges.
 * Methods of this class can be mapped 1:1 to the server-side request mappers in the deduct package.
 * Communication is done via POST requests, data objects are transferred as JSON-marshalled representations (see Representation in math package).
 */
public class DSProtectionClient {
    private Logger logger = LoggerFactory.getLogger(DSProtectionClient.class);

    private WebClient dsProtectionClient; // the underlying web client making the requests

    public DSProtectionClient(String dsProtectionServiceURL) {
        logger.info("Creating a client that sends queries to " + dsProtectionServiceURL);
        this.dsProtectionClient = WebClientHelper.buildWebClient(dsProtectionServiceURL);
    }
}
