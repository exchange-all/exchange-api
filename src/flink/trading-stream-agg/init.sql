CREATE TABLE IF NOT EXISTS "windowed_trades"
(
    window_type     VARCHAR(255)    NOT NULL,
    trading_pair_id VARCHAR(255)    NOT NULL,
    open_price      DECIMAL(64, 18) NOT NULL DEFAULT 0,
    close_price     DECIMAL(64, 18) NOT NULL DEFAULT 0,
    high_price      DECIMAL(64, 18) NOT NULL DEFAULT 0,
    low_price       DECIMAL(64, 18) NOT NULL DEFAULT 0,
    timestamp       BIGINT          NOT NULL,
    PRIMARY KEY (window_type, trading_pair_id, timestamp)
);
