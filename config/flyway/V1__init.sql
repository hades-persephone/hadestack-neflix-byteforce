-- Bảng audit_logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')),
    action_by BIGINT REFERENCES users(id),
    action_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255)
);

-- Bảng users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(20),
    country VARCHAR(50),
    phone_number VARCHAR(20),
    profile_picture_url VARCHAR(255),
    subscription_plan VARCHAR(50),
    subscription_start_date DATE,
    subscription_end_date DATE,
    last_login TIMESTAMP,
    account_status VARCHAR(20) DEFAULT 'ACTIVE',
    preferred_language VARCHAR(10),
    notification_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng movies
CREATE TABLE movies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT NOT NULL,
    release_date DATE,
    rating_score DECIMAL(3,1),
    imdb_rating DECIMAL(3,1),
    rotten_tomatoes_score INT,
    production_company VARCHAR(100),
    budget BIGINT,
    box_office BIGINT,
    trailer_url VARCHAR(255),
    poster_url VARCHAR(255),
    thumbnail_url VARCHAR(255),
    video_quality VARCHAR(20),
    age_rating VARCHAR(10),
    country_of_origin VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    stream_url VARCHAR(255),
    file_size BIGINT,
    runtime_seconds INT,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng series
CREATE TABLE series (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE, -- Ngày phát hành series (có thể là ngày mùa đầu tiên)
    rating_score DOUBLE PRECISION CHECK (rating_score BETWEEN 0 AND 10),
    imdb_rating DOUBLE PRECISION CHECK (imdb_rating BETWEEN 0 AND 10),
    rotten_tomatoes_score INT CHECK (rotten_tomatoes_score BETWEEN 0 AND 100),
    production_company VARCHAR(100),
    trailer_url VARCHAR(255),
    poster_url VARCHAR(255),
    thumbnail_url VARCHAR(255),
    age_rating VARCHAR(10),
    country_of_origin VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE,
    view_count BIGINT DEFAULT 0 CHECK (view_count >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

CREATE TABLE seasons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id UUID NOT NULL REFERENCES series(id) ON DELETE CASCADE,
    season_number INT NOT NULL CHECK (season_number >= 1),
    title VARCHAR(255),
    description TEXT,
    release_date DATE,
    poster_url VARCHAR(255),
    trailer_url VARCHAR(255),
    is_available BOOLEAN DEFAULT TRUE,
    view_count BIGINT DEFAULT 0 CHECK (view_count >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    CONSTRAINT unique_series_season UNIQUE (series_id, season_number)
);

-- Bảng episodes
CREATE TABLE episodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    season_number INT NOT NULL,
    episode_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INT NOT NULL,
    release_date DATE,
    rating_score DECIMAL(3,1),
    view_count BIGINT DEFAULT 0,
    stream_url VARCHAR(255),
    thumbnail_url VARCHAR(255),
    file_size BIGINT,
    video_quality VARCHAR(20),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    air_date DATE,
    runtime_seconds INT,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng categories
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    parent_category_id INT REFERENCES categories(id),
    display_order INT,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng actors
CREATE TABLE actors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    nationality VARCHAR(50),
    biography TEXT,
    profile_picture_url VARCHAR(255),
    height_cm INT,
    gender VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    imdb_profile_url VARCHAR(255),
    awards TEXT,
    known_for VARCHAR(255),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng directors
CREATE TABLE directors (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   full_name VARCHAR(100) NOT NULL,
   date_of_birth DATE,
   nationality VARCHAR(50),
   biography TEXT,
   profile_picture_url VARCHAR(255),
   is_active BOOLEAN DEFAULT TRUE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP,
   deleted_at TIMESTAMP,
   imdb_profile_url VARCHAR(255),
   awards TEXT,
   known_for VARCHAR(255),
   years_active VARCHAR(50),
   style VARCHAR(100),
   created_by BIGINT REFERENCES users(id),
   updated_by BIGINT REFERENCES users(id)
);

-- Bảng languages
CREATE TABLE languages (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   name VARCHAR(50) UNIQUE NOT NULL,
   code VARCHAR(10) NOT NULL,
   is_active BOOLEAN DEFAULT TRUE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP,
   deleted_at TIMESTAMP,
   region VARCHAR(50),
   native_name VARCHAR(50),
   popularity_score INT,
   created_by BIGINT REFERENCES users(id),
   updated_by BIGINT REFERENCES users(id)
);

-- Bảng subtitles
CREATE TABLE subtitles (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   language_code VARCHAR(10) NOT NULL,
   file_url VARCHAR(255) NOT NULL,
   format VARCHAR(20),
   is_active BOOLEAN DEFAULT TRUE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP,
   deleted_at TIMESTAMP,
   quality_score INT,
   contributor VARCHAR(100),
   created_by BIGINT REFERENCES users(id),
   updated_by BIGINT REFERENCES users(id)
);

-- Bảng trung gian movies_categories
CREATE TABLE movies_categories (
   movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
   category_id INT REFERENCES categories(id) ON DELETE CASCADE,
   PRIMARY KEY (movie_id, category_id)
);

-- Bảng trung gian series_categories
CREATE TABLE series_categories (
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    category_id INT REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, category_id)
);

-- Bảng trung gian movies_actors
CREATE TABLE movies_actors (
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
    actor_id INT REFERENCES actors(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, actor_id)
);

-- Bảng trung gian series_actors
CREATE TABLE series_actors (
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    actor_id INT REFERENCES actors(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, actor_id)
);

-- Bảng trung gian movies_directors
CREATE TABLE movies_directors (
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
    director_id INT REFERENCES directors(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, director_id)
);

-- Bảng trung gian series_directors
CREATE TABLE series_directors (
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    director_id INT REFERENCES directors(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, director_id)
);

-- Bảng trung gian movies_languages
CREATE TABLE movies_languages (
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
    language_id INT REFERENCES languages(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, language_id)
);

-- Bảng trung gian series_languages
CREATE TABLE series_languages (
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    language_id INT REFERENCES languages(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, language_id)
);

-- Bảng trung gian movies_subtitles
CREATE TABLE movies_subtitles (
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
    subtitle_id INT REFERENCES subtitles(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, subtitle_id)
);

-- Bảng trung gian series_subtitles
CREATE TABLE series_subtitles (
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    subtitle_id INT REFERENCES subtitles(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, subtitle_id)
);

-- Bảng trung gian series_seasons
CREATE TABLE series_seasons (
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    season_id INT REFERENCES seasons(id) ON DELETE CASCADE,
    PRIMARY KEY (series_id, season_id)
);

-- Bảng trung gian seasons_episodes
CREATE TABLE seasons_episodes (
    season_id INT REFERENCES seasons(id) ON DELETE CASCADE,
    episode_id INT REFERENCES episode(id) ON DELETE CASCADE,
    PRIMARY KEY (episode_id, season_id)
);

-- Bảng watch_history
CREATE TABLE watch_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    movie_id INT REFERENCES movies(id) ON DELETE SET NULL,
    episode_id INT REFERENCES episodes(id) ON DELETE SET NULL,
    watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    watch_duration_seconds INT,
    completed BOOLEAN DEFAULT FALSE,
    last_position_seconds INT,
    device_type VARCHAR(50),
    ip_address VARCHAR(45),
    quality_watched VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    watch_count INT DEFAULT 1,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng ratings
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    score INT CHECK (score >= 1 AND score <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    source VARCHAR(50),
    weight INT DEFAULT 1,
    is_verified BOOLEAN DEFAULT FALSE,
    comment_short VARCHAR(255),
    rating_date DATE,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng reviews
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    movie_id INT REFERENCES movies(id) ON DELETE SET NULL,
    series_id INT REFERENCES series(id) ON DELETE SET NULL,
    rating_id INT UNIQUE REFERENCES ratings(id) ON DELETE SET NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    likes_count INT DEFAULT 0,
    dislikes_count INT DEFAULT 0,
    is_spoiler BOOLEAN DEFAULT FALSE,
    review_title VARCHAR(255),
    language VARCHAR(10),
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng playlists
CREATE TABLE playlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    cover_image_url VARCHAR(255),
    total_items INT DEFAULT 0,
    last_updated TIMESTAMP,
    visibility VARCHAR(20) DEFAULT 'PRIVATE',
    share_url VARCHAR(255),
    playlist_type VARCHAR(50),
    order_number INT,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Bảng trung gian playlists_movies
CREATE TABLE playlists_movies (
    playlist_id INT REFERENCES playlists(id) ON DELETE CASCADE,
    movie_id INT REFERENCES movies(id) ON DELETE CASCADE,
    PRIMARY KEY (playlist_id, movie_id)
);

-- Bảng trung gian playlists_series
CREATE TABLE playlists_series (
    playlist_id INT REFERENCES playlists(id) ON DELETE CASCADE,
    series_id INT REFERENCES series(id) ON DELETE CASCADE,
    PRIMARY KEY (playlist_id, series_id)
);

