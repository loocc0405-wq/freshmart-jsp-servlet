-- COMPLETE FIX: Phân phối product lots và xóa Default Supplier
-- Copy toàn bộ script này và paste vào SQL Server Management Studio, sau đó Execute

USE freshmart;
GO

PRINT '=== STEP 1: Current Status ===';
SELECT s.name, COUNT(l.id) as lots, COUNT(DISTINCT l.product_id) as products
FROM suppliers s
LEFT JOIN product_lots l ON l.supplier_id = s.id
GROUP BY s.name
ORDER BY lots DESC;

PRINT '';
PRINT '=== STEP 2: Redistributing lots ===';

-- Lấy Default Supplier ID (chỉ 1 supplier)
DECLARE @defaultId BIGINT;
SELECT TOP 1 @defaultId = id 
FROM suppliers 
WHERE email = 'unknown_1@example.com' OR name = 'Default Supplier'
ORDER BY id;

IF @defaultId IS NULL
BEGIN
    PRINT 'Default Supplier not found. Script completed.';
    RETURN;
END

PRINT 'Default Supplier ID: ' + CAST(@defaultId AS VARCHAR);

-- Phân phối cho các suppliers chính
UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'ABC Supplier')
WHERE supplier_id = @defaultId;
PRINT 'ABC Supplier: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = N'Nguyên Bảo Lộc')
WHERE supplier_id = @defaultId;
PRINT 'Nguyên Bảo Lộc: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'ABC Fresh Produce')
WHERE supplier_id = @defaultId;
PRINT 'ABC Fresh Produce: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'XYZ Organic Farm')
WHERE supplier_id = @defaultId;
PRINT 'XYZ Organic Farm: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Green Valley Co.')
WHERE supplier_id = @defaultId;
PRINT 'Green Valley Co.: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Fresh Direct Ltd')
WHERE supplier_id = @defaultId;
PRINT 'Fresh Direct Ltd: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Quality Foods')
WHERE supplier_id = @defaultId;
PRINT 'Quality Foods: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Valid Supplier')
WHERE supplier_id = @defaultId;
PRINT 'Valid Supplier: 10 lots';

UPDATE TOP (10) product_lots
SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Another Valid')
WHERE supplier_id = @defaultId;
PRINT 'Another Valid: 10 lots';

-- Phân phối cho các suppliers khác (nếu có)
IF EXISTS (SELECT 1 FROM suppliers WHERE name = 'Company, Inc')
BEGIN
    UPDATE TOP (10) product_lots
    SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Company, Inc')
    WHERE supplier_id = @defaultId;
    PRINT 'Company, Inc: 10 lots';
END

IF EXISTS (SELECT 1 FROM suppliers WHERE name = 'ABC Fresh')
BEGIN
    UPDATE TOP (10) product_lots
    SET supplier_id = (SELECT id FROM suppliers WHERE name = 'ABC Fresh')
    WHERE supplier_id = @defaultId;
    PRINT 'ABC Fresh: 10 lots';
END

IF EXISTS (SELECT 1 FROM suppliers WHERE name = 'XYZ Organic')
BEGIN
    UPDATE TOP (10) product_lots
    SET supplier_id = (SELECT id FROM suppliers WHERE name = 'XYZ Organic')
    WHERE supplier_id = @defaultId;
    PRINT 'XYZ Organic: 10 lots';
END

IF EXISTS (SELECT 1 FROM suppliers WHERE name = 'Minimal Supplier')
BEGIN
    UPDATE TOP (10) product_lots
    SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Minimal Supplier')
    WHERE supplier_id = @defaultId;
    PRINT 'Minimal Supplier: 10 lots';
END

-- Phân phối từ suppliers có nhiều lots nếu Default Supplier hết
DECLARE @remainingSuppliers TABLE (SupplierName NVARCHAR(255));
INSERT INTO @remainingSuppliers 
SELECT name FROM suppliers 
WHERE name IN (N'Nguyên Bảo Lộc', 'Another Valid', 'Company, Inc', 'ABC Fresh', 'XYZ Organic', 'Minimal Supplier')
AND NOT EXISTS (SELECT 1 FROM product_lots WHERE supplier_id = suppliers.id);

IF EXISTS (SELECT 1 FROM @remainingSuppliers)
BEGIN
    PRINT '';
    PRINT '=== Redistributing from suppliers with most lots ===';
    
    -- Phân phối từ ABC Supplier (nếu có nhiều lots)
    DECLARE @abcSupplierId BIGINT = (SELECT id FROM suppliers WHERE name = 'ABC Supplier');
    DECLARE @abcLots INT = (SELECT COUNT(*) FROM product_lots WHERE supplier_id = @abcSupplierId);
    
    IF @abcLots > 10
    BEGIN
        -- Nguyên Bảo Lộc
        IF EXISTS (SELECT 1 FROM @remainingSuppliers WHERE SupplierName = N'Nguyên Bảo Lộc')
        BEGIN
            UPDATE TOP (5) product_lots
            SET supplier_id = (SELECT id FROM suppliers WHERE name = N'Nguyên Bảo Lộc')
            WHERE supplier_id = @abcSupplierId;
            PRINT 'Nguyên Bảo Lộc: 5 lots (from ABC Supplier)';
        END
        
        -- Another Valid
        IF EXISTS (SELECT 1 FROM @remainingSuppliers WHERE SupplierName = 'Another Valid')
        BEGIN
            UPDATE TOP (5) product_lots
            SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Another Valid')
            WHERE supplier_id = @abcSupplierId;
            PRINT 'Another Valid: 5 lots (from ABC Supplier)';
        END
    END
    
    -- Phân phối từ Quality Foods
    DECLARE @qualityId BIGINT = (SELECT id FROM suppliers WHERE name = 'Quality Foods');
    DECLARE @qualityLots INT = (SELECT COUNT(*) FROM product_lots WHERE supplier_id = @qualityId);
    
    IF @qualityLots > 10
    BEGIN
        IF EXISTS (SELECT 1 FROM @remainingSuppliers WHERE SupplierName = 'ABC Fresh')
        BEGIN
            UPDATE TOP (5) product_lots
            SET supplier_id = (SELECT id FROM suppliers WHERE name = 'ABC Fresh')
            WHERE supplier_id = @qualityId;
            PRINT 'ABC Fresh: 5 lots (from Quality Foods)';
        END
        
        IF EXISTS (SELECT 1 FROM @remainingSuppliers WHERE SupplierName = 'Minimal Supplier')
        BEGIN
            UPDATE TOP (5) product_lots
            SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Minimal Supplier')
            WHERE supplier_id = @qualityId;
            PRINT 'Minimal Supplier: 5 lots (from Quality Foods)';
        END
    END
    
    -- Phân phối từ Fresh Direct Ltd
    DECLARE @freshDirectId BIGINT = (SELECT id FROM suppliers WHERE name = 'Fresh Direct Ltd');
    DECLARE @freshDirectLots INT = (SELECT COUNT(*) FROM product_lots WHERE supplier_id = @freshDirectId);
    
    IF @freshDirectLots > 10
    BEGIN
        IF EXISTS (SELECT 1 FROM @remainingSuppliers WHERE SupplierName = 'Company, Inc')
        BEGIN
            UPDATE TOP (5) product_lots
            SET supplier_id = (SELECT id FROM suppliers WHERE name = 'Company, Inc')
            WHERE supplier_id = @freshDirectId;
            PRINT 'Company, Inc: 5 lots (from Fresh Direct Ltd)';
        END
        
        IF EXISTS (SELECT 1 FROM @remainingSuppliers WHERE SupplierName = 'XYZ Organic')
        BEGIN
            UPDATE TOP (5) product_lots
            SET supplier_id = (SELECT id FROM suppliers WHERE name = 'XYZ Organic')
            WHERE supplier_id = @freshDirectId;
            PRINT 'XYZ Organic: 5 lots (from Fresh Direct Ltd)';
        END
    END
END

-- Xóa Default Supplier
PRINT '';
PRINT '=== STEP 3: Deleting Default Supplier ===';

DECLARE @remainingLots INT;
SELECT @remainingLots = COUNT(*) FROM product_lots WHERE supplier_id = @defaultId;

IF @remainingLots = 0
BEGIN
    DELETE FROM suppliers WHERE id = @defaultId;
    PRINT 'Default Supplier deleted successfully!';
END
ELSE
BEGIN
    PRINT 'WARNING: Default Supplier still has ' + CAST(@remainingLots AS VARCHAR) + ' lots.';
    PRINT 'Run the script again to distribute remaining lots.';
END

-- Kết quả cuối cùng
PRINT '';
PRINT '=== FINAL RESULT ===';
SELECT 
    s.name,
    COUNT(l.id) as lots,
    SUM(l.qty_in) as total_qty,
    CAST(SUM(l.qty_in * l.import_price) AS DECIMAL(18,2)) as total_value,
    COUNT(DISTINCT l.product_id) as products,
    COUNT(CASE WHEN l.expiry_date <= GETDATE() THEN 1 END) as expired,
    COUNT(CASE WHEN l.expiry_date > GETDATE() AND l.expiry_date <= DATEADD(day, 7, GETDATE()) THEN 1 END) as near_expiry
FROM suppliers s
LEFT JOIN product_lots l ON l.supplier_id = s.id
GROUP BY s.name
ORDER BY lots DESC;

PRINT '';
PRINT 'Done! Refresh your browser to see changes.';
