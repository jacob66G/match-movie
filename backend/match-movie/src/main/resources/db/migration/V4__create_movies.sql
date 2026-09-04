CREATE TABLE movies (
    id BIGINT PRIMARY KEY,
    imdb_id VARCHAR(15),
    title TEXT NOT NULL,
    original_title TEXT,
    original_language VARCHAR(10),
    overview TEXT,
    tagline TEXT,
    release_date DATE,
    release_year SMALLINT GENERATED ALWAYS AS (EXTRACT(YEAR FROM release_date)::SMALLINT) STORED,
    runtime INTEGER,
    status VARCHAR(20),
    adult BOOLEAN NOT NULL DEFAULT FALSE,
    video BOOLEAN NOT NULL DEFAULT FALSE,
    popularity DOUBLE PRECISION,
    vote_average NUMERIC(5, 3),
    vote_count INTEGER NOT NULL DEFAULT 0,
    poster_path VARCHAR(255),
    backdrop_path VARCHAR(255),
    collection_id BIGINT,
    collection_name TEXT,
    origin VARCHAR(20) NOT NULL,
    synced_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT movies_runtime_positive CHECK (runtime IS NULL OR runtime > 0),
    CONSTRAINT movies_vote_average_range CHECK (vote_average IS NULL OR vote_average BETWEEN 0 AND 10),
    CONSTRAINT movies_vote_count_non_negative CHECK (vote_count >= 0),
    CONSTRAINT movies_origin_allowed CHECK (origin IN ('SEED', 'ON_DEMAND'))
);

CREATE INDEX idx_movies_popularity ON movies (popularity DESC);
CREATE INDEX idx_movies_release_year ON movies (release_year);