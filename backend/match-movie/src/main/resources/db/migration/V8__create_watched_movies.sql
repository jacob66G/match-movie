CREATE TABLE watched_movies (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL REFERENCES movies (id),
    rating INTEGER,
    review VARCHAR(2000),
    watched_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, movie_id),
    CONSTRAINT watched_movies_rating_range CHECK (rating IS NULL OR rating BETWEEN 1 AND 10)
);

CREATE INDEX idx_watched_movies_user_id_watched_at ON watched_movies (user_id, watched_at DESC);
CREATE INDEX idx_watched_movies_movie_id ON watched_movies (movie_id);
