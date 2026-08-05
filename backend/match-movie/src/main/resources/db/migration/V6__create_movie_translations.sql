CREATE TABLE movie_translations (
    movie_id BIGINT NOT NULL REFERENCES movies (id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL,
    title TEXT,
    overview TEXT,
    tagline TEXT,
    PRIMARY KEY (movie_id, language_code)
);
