-- Create keyspace with NetworkTopologyStrategy for production use
-- For development, SimpleStrategy is fine
CREATE KEYSPACE IF NOT EXISTS action_history
    WITH REPLICATION = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

USE action_history;

-- Primary table for action history by entity
-- Uses compound primary key with entity_type+entity_id for partition and timestamp for clustering
CREATE TABLE IF NOT EXISTS action_history_by_entity (
    entity_type text,
    entity_id text,
    action_timestamp timestamp,
    action_type text,
    user_id text,
    details map<text, text>,
    source_ip text,
    user_agent text,
    PRIMARY KEY ((entity_type, entity_id), action_timestamp)
) WITH CLUSTERING ORDER BY (action_timestamp DESC)
   AND default_time_to_live = 31536000; -- 1 year default TTL

-- Secondary index for searching by user
CREATE TABLE IF NOT EXISTS action_history_by_user (
    user_id text,
    action_timestamp timestamp,
    entity_type text,
    entity_id text,
    action_type text,
    details map<text, text>,
    source_ip text,
    user_agent text,
    PRIMARY KEY ((user_id), action_timestamp)
) WITH CLUSTERING ORDER BY (action_timestamp DESC)
   AND default_time_to_live = 31536000; -- 1 year default TTL

-- Secondary index for searching by action type
CREATE TABLE IF NOT EXISTS action_history_by_action (
    action_type text,
    action_timestamp timestamp,
    entity_type text,
    entity_id text,
    user_id text,
    details map<text, text>,
    source_ip text,
    user_agent text,
    PRIMARY KEY ((action_type), action_timestamp)
) WITH CLUSTERING ORDER BY (action_timestamp DESC)
   AND default_time_to_live = 31536000; -- 1 year default TTL

-- Create a monthly partitioned table for time-based queries
-- This prevents partitions from growing too large
CREATE TABLE IF NOT EXISTS action_history_by_time (
    year_month text,  -- Format: YYYY-MM
    action_timestamp timestamp,
    entity_type text,
    entity_id text,
    action_type text,
    user_id text,
    details map<text, text>,
    source_ip text,
    user_agent text,
    PRIMARY KEY ((year_month), action_timestamp)
) WITH CLUSTERING ORDER BY (action_timestamp DESC)
   AND default_time_to_live = 31536000; -- 1 year default TTL

-- Counter table for statistics
CREATE TABLE IF NOT EXISTS action_history_stats (
    entity_type text,
    action_type text,
    year_month text,
    count counter,
    PRIMARY KEY ((entity_type, action_type), year_month)
) WITH CLUSTERING ORDER BY (year_month DESC);