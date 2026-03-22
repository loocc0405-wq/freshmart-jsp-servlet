USE freshmart;
GO IF COL_LENGTH('dbo.product_lots', 'qty_reserved') IS NULL BEGIN
ALTER TABLE dbo.product_lots
ADD qty_reserved INT NOT NULL CONSTRAINT df_product_lots_qty_reserved DEFAULT (0);
END
GO IF OBJECT_ID(N'dbo.order_item_lot_reservations', N'U') IS NULL BEGIN CREATE TABLE dbo.order_item_lot_reservations (
        id BIGINT IDENTITY(1, 1) NOT NULL PRIMARY KEY,
        order_item_id BIGINT NOT NULL,
        product_lot_id BIGINT NOT NULL,
        reserved_qty INT NOT NULL,
        created_at DATETIME2(0) NOT NULL CONSTRAINT df_oir_created_at DEFAULT (SYSDATETIME()),
        released_at DATETIME2(0) NULL,
        release_reason NVARCHAR(255) NULL,
        CONSTRAINT fk_oir_order_item FOREIGN KEY (order_item_id) REFERENCES dbo.order_items(id),
        CONSTRAINT fk_oir_product_lot FOREIGN KEY (product_lot_id) REFERENCES dbo.product_lots(id)
    );
CREATE INDEX idx_oir_order_item ON dbo.order_item_lot_reservations(order_item_id);
CREATE INDEX idx_oir_product_lot ON dbo.order_item_lot_reservations(product_lot_id);
END
GO