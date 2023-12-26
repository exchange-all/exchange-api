CREATE TABLE IF NOT EXISTS "windowed_trades"
(
    window_type      VARCHAR(255)    NOT NULL,
    trading_pair_id  VARCHAR(255)    NOT NULL,
    open_price       DECIMAL(64, 18) NOT NULL DEFAULT 0,
    close_price      DECIMAL(64, 18) NOT NULL DEFAULT 0,
    high_price       DECIMAL(64, 18) NOT NULL DEFAULT 0,
    low_price        DECIMAL(64, 18) NOT NULL DEFAULT 0,
    window_timestamp BIGINT          NOT NULL,
    created_at       TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6), -- Time when the record was created in Database
    PRIMARY KEY (window_type, trading_pair_id, window_timestamp)
);

CREATE TABLE IF NOT EXISTS "trades_histories"
(
    id               VARCHAR(255) PRIMARY KEY NOT NULL,
    user_id          VARCHAR(255)             NOT NULL,
    trading_pair_id  VARCHAR(255)             NOT NULL,
    order_id         VARCHAR(255)             NOT NULL,
    amount           DECIMAL(64, 18)          NOT NULL DEFAULT 0,
    available_amount DECIMAL(64, 18)          NOT NULL DEFAULT 0,
    price            DECIMAL(64, 18)          NOT NULL DEFAULT 0,
    type             VARCHAR(50)              NOT NULL,
    status           VARCHAR(50)              NOT NULL,
    traded_amount    DECIMAL(64, 18)          NOT NULL DEFAULT 0,
    traded_price     DECIMAL(64, 18)          NOT NULL DEFAULT 0,
    traded_at        BIGINT                   NOT NULL,
    created_at       TIMESTAMP(6)             NOT NULL DEFAULT CURRENT_TIMESTAMP(6) -- Time when the record was created in Database
);
