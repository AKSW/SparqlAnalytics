DROP TABLE IF EXISTS "rdf_term";
DROP TABLE IF EXISTS "user";
DROP TABLE IF EXISTS "query";
DROP TABLE IF EXISTS "request";

-- TODO Add check constraints for valid field combinations
CREATE TABLE "rdf_term" (
	"id" text PRIMARY KEY,
	"term_type" int NOT NULL, -- Actually this should be an enum
	"lexical_form" text NOT NULL,
	
	-- TODO Allow NULL or only empty string?
	"language_tag" text,
	"datatype" text
);


CREATE TABLE "user" (
	"id" text PRIMARY KEY,
	-- For now just use text - later user IPv4/6 fields
	"name" text NOT NULL
);

CREATE TABLE "query" (
	"id" text PRIMARY KEY,
	-- query string may be normalized
	"query_string" text NOT NULL
);


CREATE TABLE "request" (
	--"id" text NOT NULL,
	"id" text PRIMARY KEY,
	"ts_start" timestamp NOT NULL,
	"user_id" text NOT NULL, --REFERENCES "user"("id"),
	"service_id" text NOT NULL,
	"query_id" text NOT NULL,  --REFERENCES "query"("id") NOT NULL,
    "item_count" text NOT NULL -- TODO clarify semantics
);

CREATE INDEX "idx_request_id" ON "request"("id");
CREATE INDEX "idx_request_ts_start" ON "request"("ts_start");
CREATE INDEX "idx_reuest_user_id" ON "request"("user_id");
CREATE INDEX "idx_request_service_id" ON "request"("service_id");
CREATE INDEX "idx_request_query_id" ON "request"("query_id");
CREATE INDEX "idx_request_item_count" ON "request"("item_count");
