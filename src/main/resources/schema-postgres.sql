CREATE SCHEMA IF NOT EXISTS virtualnetwork AUTHORIZATION postgres;

/*
 * We have to explicitly create this sequence in the schema because there may be a bug
 * where if another schema exists with a sequence of the same name, the auto-ddl appears
 * to find the sequence in the other schema and won't create it in the new schema.
 */
CREATE SEQUENCE IF NOT EXISTS virtualnetwork.base_entity_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE virtualnetwork.base_entity_seq
    OWNER TO postgres;