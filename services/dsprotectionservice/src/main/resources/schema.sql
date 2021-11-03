-- creates tables for transactions, tokens (i.e. dsids), double-spending IDs and user info by hand
-- (to avoid overflow errors due to too small cell sizes, serialized representations can get quite large...)

create table transactions (
    id bigint,
    isValid boolean,
    serializedTransactionIDRepr varchar(2000),
    k varchar(255),
    dsTagEntryID bigint,
    producedDsidEntryID bigint,
    consumedDsidEntryID bigint
);

create table dsids (
    id bigint,
    serializedDsidRepr varchar(2000),
    associatedUserInfoId bigint
);

create table dstags (
    id bigint,
    serializedC0Repr varchar(2000),
    serializedC1Repr varchar(2000),
    serializedGammaRepr varchar(2000),
    serializedEskStarProvRepr varchar(2000),
    serializedCTrace0Repr varchar(2000),
    serializedCTrace1Repr varchar(2000)
);

create table userInfo (
    id bigint,
    serializedUpkRepr varchar(2000),
    serializedDsBlameRepr varchar(2000),
    serializedDsTraceRepr varchar(2000)
);