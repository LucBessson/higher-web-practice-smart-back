CREATE TABLE IF NOT EXISTS discussions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    system_prompt TEXT NOT NULL,
    model VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    discussion_id BIGINT NOT NULL REFERENCES discussions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS features (
    id BIGSERIAL PRIMARY KEY,
    discussion_id BIGINT NOT NULL REFERENCES discussions(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    description TEXT,
    code TEXT,
    method VARCHAR(10),
    path VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS feature_settings (
    id BIGSERIAL PRIMARY KEY,
    feature_id BIGINT NOT NULL REFERENCES features(id) ON DELETE CASCADE,
    setting_name VARCHAR(100) NOT NULL,
    setting_value TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_discussion_id ON chat_messages(discussion_id);
CREATE INDEX IF NOT EXISTS idx_features_discussion_id ON features(discussion_id);
CREATE INDEX IF NOT EXISTS idx_feature_settings_feature_id ON feature_settings(feature_id);
