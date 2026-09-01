CREATE TABLE category (
    id          CHAR(36)      NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    slug        VARCHAR(100)  NOT NULL,
    created_at  DATETIME(6)   NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT uk_category_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product (
    id              CHAR(36)       NOT NULL,
    category_id     CHAR(36)       NULL,
    sku             VARCHAR(64)    NOT NULL,
    name            VARCHAR(200)   NOT NULL,
    description     TEXT           NULL,
    price           DECIMAL(19,4)  NOT NULL,
    stock_quantity  INT            NOT NULL DEFAULT 0,
    image_path      VARCHAR(255)   NULL,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      DATETIME(6)    NOT NULL,
    updated_at      DATETIME(6)    NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT uk_product_sku UNIQUE (sku),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT ck_product_price_positive CHECK (price > 0),
    CONSTRAINT ck_product_stock_non_negative CHECK (stock_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_product_category ON product (category_id);
CREATE INDEX ix_product_active ON product (active);

INSERT INTO category (id, name, slug, created_at) VALUES
    (UUID(), 'Electronics', 'electronics', NOW(6)),
    (UUID(), 'Books', 'books', NOW(6)),
    (UUID(), 'Clothing', 'clothing', NOW(6)),
    (UUID(), 'Home & Kitchen', 'home-kitchen', NOW(6));
