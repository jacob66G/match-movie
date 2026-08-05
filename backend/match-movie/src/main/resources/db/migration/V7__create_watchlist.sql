CREATE TABLE watchlist (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL REFERENCES movies (id),
    added_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, movie_id)
);

CREATE INDEX idx_watchlist_user_id_added_at ON watchlist (user_id, added_at DESC);
CREATE INDEX idx_watchlist_movie_id ON watchlist (movie_id);
