CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies (id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres (id),
    PRIMARY KEY (movie_id, genre_id)
);

CREATE INDEX idx_movie_genres_genre_id ON movie_genres (genre_id);
