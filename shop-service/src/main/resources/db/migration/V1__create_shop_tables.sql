CREATE TABLE cart (
                      id          CHAR(36)     NOT NULL,
                      user_id     VARCHAR(64)  NOT NULL,
                      created_at  DATETIME(6)  NOT NULL,
                      updated_at  DATETIME(6)  NOT NULL,
                      CONSTRAINT pk_cart PRIMARY KEY (id),
                      CONSTRAINT uk_cart_user_id UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_item (
                           id          CHAR(36)       NOT NULL,
                           cart_id     CHAR(36)       NOT NULL,
                           product_id  CHAR(36)       NOT NULL,
                           quantity    INT            NOT NULL,
                           unit_price  DECIMAL(19,4)  NOT NULL,
                           created_at  DATETIME(6)    NOT NULL,
                           updated_at  DATETIME(6)    NOT NULL,
                           CONSTRAINT pk_cart_item PRIMARY KEY (id),
                           CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
                           CONSTRAINT uk_cart_item_cart_product UNIQUE (cart_id, product_id),
                           CONSTRAINT ck_cart_item_quantity_positive CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `order` (
                         id            CHAR(36)       NOT NULL,
                         user_id       VARCHAR(64)    NOT NULL,
                         status        VARCHAR(20)    NOT NULL,
                         total_amount  DECIMAL(19,4)  NOT NULL,
                         created_at    DATETIME(6)    NOT NULL,
                         updated_at    DATETIME(6)    NOT NULL,
                         CONSTRAINT pk_order PRIMARY KEY (id),
                         CONSTRAINT ck_order_total_non_negative CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_item (
                            id           CHAR(36)       NOT NULL,
                            order_id     CHAR(36)       NOT NULL,
                            product_id   CHAR(36)       NOT NULL,
                            product_name VARCHAR(200)   NOT NULL,
                            quantity     INT            NOT NULL,
                            unit_price   DECIMAL(19,4)  NOT NULL,
                            CONSTRAINT pk_order_item PRIMARY KEY (id),
                            CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES `order` (id),
                            CONSTRAINT ck_order_item_quantity_positive CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_order_user_created ON `order` (user_id, created_at);
CREATE INDEX ix_cart_item_cart ON cart_item (cart_id);