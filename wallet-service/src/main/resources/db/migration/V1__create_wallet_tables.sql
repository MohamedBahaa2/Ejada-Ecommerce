CREATE TABLE wallet (
    id          CHAR(36)       NOT NULL,
    user_id     VARCHAR(64)    NOT NULL,
    balance     DECIMAL(19,4)  NOT NULL DEFAULT 0.0000,
    currency    CHAR(3)        NOT NULL DEFAULT 'EGP',
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6)    NOT NULL,
    updated_at  DATETIME(6)    NOT NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT uk_wallet_user_id UNIQUE (user_id),
    CONSTRAINT ck_wallet_balance_non_negative CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wallet_transaction (
    id             CHAR(36)       NOT NULL,
    wallet_id      CHAR(36)       NOT NULL,
    type           VARCHAR(20)    NOT NULL,
    amount         DECIMAL(19,4)  NOT NULL,
    balance_after  DECIMAL(19,4)  NOT NULL,
    reference_id   VARCHAR(120)   NOT NULL,
    description    VARCHAR(255)   NULL,
    created_at     DATETIME(6)    NOT NULL,
    CONSTRAINT pk_wallet_transaction PRIMARY KEY (id),
    CONSTRAINT fk_wallet_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallet (id),
    CONSTRAINT uk_wallet_txn_wallet_reference UNIQUE (wallet_id, reference_id),
    CONSTRAINT ck_wallet_txn_amount_positive CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_wallet_txn_wallet_created
    ON wallet_transaction (wallet_id, created_at);
