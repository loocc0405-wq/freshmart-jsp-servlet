USE freshmart;
SET NOCOUNT ON;
SET XACT_ABORT ON;
SET DATEFIRST 1;

BEGIN TRY
    BEGIN TRAN;

    DECLARE @Today DATE = CAST(GETDATE() AS DATE);
    DECLARE @Now   DATETIME2(0) = CAST(GETDATE() AS DATETIME2(0));
    DECLARE @SeedHash NVARCHAR(200) = N'$2a$10$FreshMartDevSeedHashPlaceholder9Bv3u5nG7Qk1LmP8sD2UOe';

    /* =========================================================
       1. seed users
       ========================================================= */
    IF OBJECT_ID('tempdb..#SeedUsers') IS NOT NULL DROP TABLE #SeedUsers;
    CREATE TABLE #SeedUsers (
        username        NVARCHAR(100) NOT NULL,
        password_hash   NVARCHAR(255) NOT NULL,
        role            NVARCHAR(20)  NOT NULL,
        tier            NVARCHAR(10)  NOT NULL,
        expired_date    DATE          NULL,
        full_name       NVARCHAR(150) NOT NULL,
        gender          NVARCHAR(10)  NOT NULL,
        dob             DATE          NULL,
        phone           NVARCHAR(20)  NULL,
        address         NVARCHAR(255) NULL,
        active          BIT           NOT NULL,
        created_days_ago INT          NOT NULL
    );

    INSERT INTO #SeedUsers (
        username, password_hash, role, tier, expired_date, full_name, gender, dob, phone, address, active, created_days_ago
    )
    VALUES
    -- staff
    (N'staff_lan',   @SeedHash, N'STAFF',   N'FREE', NULL, N'Trần Mỹ Lan',        N'FEMALE', '1992-05-18', N'0909123001', N'Quận 3, TP. Hồ Chí Minh', 1, 260),
    (N'staff_phuc',  @SeedHash, N'STAFF',   N'FREE', NULL, N'Nguyễn Hữu Phúc',    N'MALE',   '1989-11-09', N'0909123002', N'Quận Bình Thạnh, TP. Hồ Chí Minh', 1, 210),

    -- sellers
    (N'seller_minh', @SeedHash, N'SELLER',  N'FREE', NULL, N'Nguyễn Đức Minh',    N'MALE',   '1995-02-14', N'0909123101', N'Thủ Đức, TP. Hồ Chí Minh', 1, 320),
    (N'seller_thao', @SeedHash, N'SELLER',  N'FREE', NULL, N'Lê Thanh Thảo',      N'FEMALE', '1997-08-22', N'0909123102', N'Quận 7, TP. Hồ Chí Minh', 1, 295),
    (N'seller_quan', @SeedHash, N'SELLER',  N'FREE', NULL, N'Phạm Anh Quân',      N'MALE',   '1994-12-03', N'0909123103', N'Quận Tân Bình, TP. Hồ Chí Minh', 1, 250),

    -- customers
    (N'cust_01', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Nguyễn Minh Anh',      N'FEMALE', '1998-01-11', N'0909123201', N'Quận 1, TP. Hồ Chí Minh', 1, 190),
    (N'cust_02', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Trần Quốc Bảo',        N'MALE',   '1993-04-27', N'0909123202', N'Quận 4, TP. Hồ Chí Minh', 1, 160),
    (N'cust_03', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Lê Thu Hà',            N'FEMALE', '1996-09-14', N'0909123203', N'Quận Phú Nhuận, TP. Hồ Chí Minh', 1, 175),
    (N'cust_04', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Phạm Gia Huy',         N'MALE',   '1999-06-02', N'0909123204', N'Thủ Đức, TP. Hồ Chí Minh', 1, 150),
    (N'cust_05', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Võ Ngọc Mai',          N'FEMALE', '1997-07-19', N'0909123205', N'Quận 10, TP. Hồ Chí Minh', 1, 145),
    (N'cust_06', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Đặng Khánh Linh',      N'FEMALE', '2000-02-06', N'0909123206', N'Quận Bình Thạnh, TP. Hồ Chí Minh', 1, 138),
    (N'cust_07', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Bùi Tiến Dũng',        N'MALE',   '1991-10-20', N'0909123207', N'Quận Gò Vấp, TP. Hồ Chí Minh', 1, 205),
    (N'cust_08', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Hồ Thanh Trúc',        N'FEMALE', '1995-03-08', N'0909123208', N'Quận 6, TP. Hồ Chí Minh', 1, 155),
    (N'cust_09', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Nguyễn Hoàng Nam',     N'MALE',   '1988-12-30', N'0909123209', N'Quận 7, TP. Hồ Chí Minh', 1, 220),
    (N'cust_10', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Trương Mỹ Duyên',      N'FEMALE', '1994-05-15', N'0909123210', N'Quận Tân Phú, TP. Hồ Chí Minh', 1, 118),
    (N'cust_11', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Phan Đức Anh',         N'MALE',   '1990-11-12', N'0909123211', N'Quận 11, TP. Hồ Chí Minh', 1, 240),
    (N'cust_12', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Đoàn Quỳnh Như',       N'FEMALE', '1998-08-25', N'0909123212', N'Quận 5, TP. Hồ Chí Minh', 1, 130),
    (N'cust_13', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Lý Hải Đăng',          N'MALE',   '1992-01-05', N'0909123213', N'Quận 3, TP. Hồ Chí Minh', 1, 165),
    (N'cust_14', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Vũ Ngọc Diệp',         N'FEMALE', '1996-06-29', N'0909123214', N'Quận Bình Tân, TP. Hồ Chí Minh', 1, 142),
    (N'cust_15', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Huỳnh Gia Bảo',        N'MALE',   '1997-09-07', N'0909123215', N'Quận 8, TP. Hồ Chí Minh', 1, 170),
    (N'cust_16', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Nguyễn Thảo Vy',       N'FEMALE', '2001-04-03', N'0909123216', N'Quận 12, TP. Hồ Chí Minh', 1, 124),
    (N'cust_17', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Trần Nhật Minh',       N'MALE',   '1993-02-17', N'0909123217', N'Quận 2, TP. Hồ Chí Minh', 1, 200),
    (N'cust_18', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Phạm Bảo Trân',        N'FEMALE', '1999-11-01', N'0909123218', N'Quận 9, TP. Hồ Chí Minh', 1, 111),
    (N'cust_19', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Lâm Quốc Khánh',       N'MALE',   '1990-07-21', N'0909123219', N'Quận 4, TP. Hồ Chí Minh', 1, 215),
    (N'cust_20', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Mai Thiên Hương',      N'FEMALE', '1995-12-09', N'0909123220', N'Quận Tân Bình, TP. Hồ Chí Minh', 1, 150),
    (N'cust_21', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Dương Anh Khoa',       N'MALE',   '1998-03-27', N'0909123221', N'Quận 7, TP. Hồ Chí Minh', 1, 136),
    (N'cust_22', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Tạ Ngọc Hân',          N'OTHER',  '1997-10-13', N'0909123222', N'Quận Phú Nhuận, TP. Hồ Chí Minh', 1, 108),
    (N'cust_23', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Nguyễn Tường Vi',      N'FEMALE', '2000-01-24', N'0909123223', N'Thủ Đức, TP. Hồ Chí Minh', 1, 98),
    (N'cust_24', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Lê Thành Công',        N'MALE',   '1989-06-18', N'0909123224', N'Quận Gò Vấp, TP. Hồ Chí Minh', 1, 188),
    (N'cust_25', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Phạm Ngọc Thảo',       N'FEMALE', '1994-09-16', N'0909123225', N'Quận 1, TP. Hồ Chí Minh', 1, 140),
    (N'cust_26', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Đinh Minh Quân',       N'MALE',   '1991-08-08', N'0909123226', N'Quận 11, TP. Hồ Chí Minh', 1, 232),
    (N'cust_27', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Bùi Thu Uyên',         N'FEMALE', '1998-12-11', N'0909123227', N'Quận 6, TP. Hồ Chí Minh', 1, 122),
    (N'cust_28', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Cao Hoàng Phúc',       N'MALE',   '1996-05-01', N'0909123228', N'Quận Bình Tân, TP. Hồ Chí Minh', 1, 157),
    (N'cust_29', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Võ Khả Ngân',          N'FEMALE', '1999-07-05', N'0909123229', N'Quận 3, TP. Hồ Chí Minh', 1, 105),
    (N'cust_30', @SeedHash, N'CUSTOMER', N'FREE', NULL, N'Đỗ Gia Hân',           N'FEMALE', '2001-09-22', N'0909123230', N'Quận 8, TP. Hồ Chí Minh', 1, 92);

    MERGE users AS tgt
    USING (
        SELECT
            username,
            LOWER(username) + N'@freshmart.local' AS email,
            password_hash,
            role,
            tier,
            expired_date,
            full_name,
            gender,
            dob,
            phone,
            address,
            active,
            DATEADD(DAY, -created_days_ago, @Now) AS created_at
        FROM #SeedUsers
    ) AS src
    ON tgt.username = src.username
    WHEN MATCHED THEN
        UPDATE SET
            tgt.email         = ISNULL(tgt.email, src.email),
            tgt.password_hash = src.password_hash,
            tgt.role          = src.role,
            tgt.tier          = src.tier,
            tgt.expired_date  = src.expired_date,
            tgt.full_name     = src.full_name,
            tgt.gender        = src.gender,
            tgt.dob           = src.dob,
            tgt.phone         = src.phone,
            tgt.address       = src.address,
            tgt.active        = src.active
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (
            username, email, password_hash, role, tier, expired_date,
            full_name, gender, dob, phone, address, active, created_at
        )
        VALUES (
            src.username, src.email, src.password_hash, src.role, src.tier, src.expired_date,
            src.full_name, src.gender, src.dob, src.phone, src.address, src.active, src.created_at
        );

    /* =========================================================
       2. seed suppliers
       ========================================================= */
    IF OBJECT_ID('tempdb..#SeedSuppliers') IS NOT NULL DROP TABLE #SeedSuppliers;
    CREATE TABLE #SeedSuppliers (
        name            NVARCHAR(150) NOT NULL,
        phone           NVARCHAR(20)  NULL,
        address         NVARCHAR(255) NULL,
        certificate     NVARCHAR(100) NULL,
        lead_time_days  INT           NOT NULL,
        note            NVARCHAR(255) NULL,
        email           NVARCHAR(150) NULL,
        created_days_ago INT          NOT NULL,
        updated_days_ago INT          NOT NULL
    );

    INSERT INTO #SeedSuppliers (
        name, phone, address, certificate, lead_time_days, note, email, created_days_ago, updated_days_ago
    )
    VALUES
    (N'HTX Rau Củ Đà Lạt Xanh',              N'02873001001', N'Đà Lạt, Lâm Đồng',                N'VietGAP', 2,  N'Chuyên rau lá và củ quả mát, giao sáng sớm.',                          N'dalatxanh@freshsup.vn', 420, 3),
    (N'Nông Trại VietGAP Củ Chi',            N'02873001002', N'Củ Chi, TP. Hồ Chí Minh',         N'VietGAP', 1,  N'Rau ăn lá, rau gia vị canh tác theo quy trình VietGAP.',                 N'cuchi.vietgap@freshsup.vn', 390, 4),
    (N'Trang Trại Hữu Cơ Mộc Châu',          N'02873001003', N'Mộc Châu, Sơn La',                N'ISO22000', 4, N'Trái cây và rau củ hữu cơ, ổn định theo mùa vụ.',                        N'mocchau.organic@freshsup.vn', 370, 7),
    (N'Công ty Thực phẩm Heo Sạch An Phú',   N'02873001004', N'Long An',                          N'HACCP', 2,    N'Thịt heo mát đóng khay, có hồ sơ truy xuất theo lô.',                    N'anphu.pork@freshsup.vn', 360, 5),
    (N'Trang trại Gà Tươi Bình Phước',       N'02873001005', N'Bình Phước',                       N'VietGAP', 2,  N'Cung ứng gà tươi, trứng và thực phẩm sơ chế lạnh.',                      N'ga.binphuoc@freshsup.vn', 350, 6),
    (N'Công ty Thịt Bò Tây Nguyên Fresh',    N'02873001006', N'Buôn Ma Thuột, Đắk Lắk',          N'ISO22000', 3, N'Thịt bò mát, bò xay, ba rọi bò cắt theo quy cách.',                      N'beef.tn@freshsup.vn', 340, 8),
    (N'Hải sản Phú Quốc SeaMart',            N'02873001007', N'Phú Quốc, Kiên Giang',            N'HACCP', 8,    N'Lead time cao do gom chuyến lạnh định kỳ từ đảo.',                       N'phuquoc.sea@freshsup.vn', 300, 10),
    (N'Vựa Hải sản Vũng Tàu Fresh',          N'02873001008', N'Vũng Tàu, Bà Rịa - Vũng Tàu',     N'HACCP', 7,    N'Hải sản tươi giao lạnh trong ngày, mạnh nhóm cá và mực.',               N'vungtau.fresh@freshsup.vn', 290, 9),
    (N'Công ty Thủy sản Miền Trung HACCP',   N'02873001009', N'Nha Trang, Khánh Hòa',            N'HACCP', 10,   N'Nguồn cung tôm cá biển và sơ chế theo tiêu chuẩn HACCP.',               N'mientrung.sea@freshsup.vn', 280, 12),
    (N'Vườn Trái Cây Miền Tây Select',       N'02873001010', N'Cái Bè, Tiền Giang',              N'VietGAP', 8,  N'Lead time cao vào mùa cao điểm trái cây và các đợt gom hàng.',          N'mientay.select@freshsup.vn', 260, 7),
    (N'Hợp tác xã Trái cây Đồng Nai',        N'02873001011', N'Long Khánh, Đồng Nai',            N'VietGAP', 3,  N'Chuối, bơ, thanh long, cam sành ổn định quanh năm.',                     N'dongnai.fruit@freshsup.vn', 255, 5),
    (N'Xưởng Thực phẩm Tươi Sạch Sài Gòn',   N'02873001012', N'Bình Tân, TP. Hồ Chí Minh',       N'ISO22000', 9, N'Đồ chế biến sẵn, kim chi, salad và suất tươi đóng hộp.',                 N'saigon.readyfood@freshsup.vn', 240, 6);

    MERGE suppliers AS tgt
    USING (
        SELECT
            name,
            phone,
            address,
            certificate,
            lead_time_days,
            note,
            email,
            DATEADD(DAY, -created_days_ago, @Now) AS created_at,
            DATEADD(DAY, -updated_days_ago, @Now) AS updated_at
        FROM #SeedSuppliers
    ) AS src
    ON tgt.name = src.name
    WHEN MATCHED THEN
        UPDATE SET
            tgt.phone          = src.phone,
            tgt.address        = src.address,
            tgt.certificate    = src.certificate,
            tgt.lead_time_days = src.lead_time_days,
            tgt.note           = src.note,
            tgt.email          = src.email,
            tgt.updated_at     = src.updated_at
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (
            name, phone, address, certificate, lead_time_days, note, email, created_at, updated_at
        )
        VALUES (
            src.name, src.phone, src.address, src.certificate, src.lead_time_days, src.note, src.email, src.created_at, src.updated_at
        );

    /* =========================================================
       3. seed products
       ========================================================= */
    IF OBJECT_ID('tempdb..#SeedProducts') IS NOT NULL DROP TABLE #SeedProducts;
    CREATE TABLE #SeedProducts (
        product_no   INT           NOT NULL,
        name         NVARCHAR(150) NOT NULL,
        category     NVARCHAR(100) NOT NULL,
        unit         NVARCHAR(20)  NOT NULL,
        sell_price   DECIMAL(18,2) NOT NULL,
        image_url    NVARCHAR(255) NULL,
        description  NVARCHAR(500) NULL,
        active       BIT           NOT NULL
    );

    INSERT INTO #SeedProducts (
        product_no, name, category, unit, sell_price, image_url, description, active
    )
    VALUES
    (1,  N'Cải xanh',              N'Rau củ',                    N'bó',  18000,  N'/images/products/cai-xanh.jpg',           N'Rau cải xanh tươi, phù hợp nấu canh và xào, nhập mới hằng ngày.', 1),
    (2,  N'Rau muống',             N'Rau củ',                    N'bó',  16000,  N'/images/products/rau-muong.jpg',          N'Rau muống nước non, bó gọn, phù hợp luộc và xào tỏi.', 1),
    (3,  N'Xà lách romaine',       N'Rau củ',                    N'bó',  29000,  N'/images/products/xa-lach-romaine.jpg',    N'Xà lách romaine giòn ngọt, dùng cho salad và cuốn.', 1),
    (4,  N'Cà chua bi',            N'Rau củ',                    N'hộp', 42000,  N'/images/products/ca-chua-bi.jpg',         N'Cà chua bi hộp nhỏ, quả đều, vị ngọt thanh.', 1),
    (5,  N'Dưa leo baby',          N'Rau củ',                    N'kg',  28000,  N'/images/products/dua-leo-baby.jpg',       N'Dưa leo baby xanh mướt, giòn và ít hạt.', 1),
    (6,  N'Cà rốt Đà Lạt',         N'Rau củ',                    N'kg',  32000,  N'/images/products/ca-rot-da-lat.jpg',      N'Cà rốt Đà Lạt củ đều, màu đẹp, thích hợp ép nước và nấu súp.', 1),
    (7,  N'Khoai tây vàng',        N'Rau củ',                    N'kg',  26000,  N'/images/products/khoai-tay-vang.jpg',     N'Khoai tây vàng ruột chắc, dùng chiên và hầm.', 1),
    (8,  N'Bắp cải tím',           N'Rau củ',                    N'kg',  30000,  N'/images/products/bap-cai-tim.jpg',        N'Bắp cải tím tươi, giòn, phù hợp trộn salad.', 1),
    (9,  N'Hành lá',               N'Rau củ',                    N'bó',  22000,  N'/images/products/hanh-la.jpg',            N'Hành lá tươi xanh, bó sạch, dùng làm gia vị.', 1),
    (10, N'Nấm đùi gà',            N'Rau củ',                    N'hộp', 55000,  N'/images/products/nam-dui-ga.jpg',         N'Nấm đùi gà đóng hộp mát, thịt nấm dày và ngọt.', 1),

    (11, N'Thịt ba chỉ heo',       N'Thịt',                      N'kg',  165000, N'/images/products/thit-ba-chi-heo.jpg',    N'Ba chỉ heo mát, tỷ lệ nạc mỡ cân đối, phù hợp kho và nướng.', 1),
    (12, N'Thịt nạc vai heo',      N'Thịt',                      N'kg',  150000, N'/images/products/thit-nac-vai-heo.jpg',   N'Nạc vai heo mềm, dùng xay, xào hoặc rim.', 1),
    (13, N'Thịt bò thăn',          N'Thịt',                      N'kg',  335000, N'/images/products/thit-bo-than.jpg',       N'Thăn bò mát cắt sẵn, phù hợp áp chảo và lúc lắc.', 1),
    (14, N'Thịt bò xay',           N'Thịt',                      N'kg',  245000, N'/images/products/thit-bo-xay.jpg',        N'Thịt bò xay mát, tỷ lệ nạc cao, tiện chế biến.', 1),
    (15, N'Ức gà phi lê',          N'Thịt',                      N'kg',  118000, N'/images/products/uc-ga-phi-le.jpg',      N'Ức gà phi lê sạch, ít mỡ, phù hợp ăn kiêng và meal prep.', 1),
    (16, N'Đùi gà góc tư',         N'Thịt',                      N'kg',  82000,  N'/images/products/dui-ga-goc-tu.jpg',      N'Đùi gà góc tư tươi, phù hợp chiên và nướng.', 1),
    (17, N'Sườn non heo',          N'Thịt',                      N'kg',  198000, N'/images/products/suon-non-heo.jpg',       N'Sườn non heo cắt miếng vừa, nhiều nạc, ít vụn.', 1),
    (18, N'Ba rọi bò Mỹ',          N'Thịt',                      N'kg',  310000, N'/images/products/ba-roi-bo-my.jpg',      N'Ba rọi bò Mỹ cuộn khay mát, dùng lẩu và nướng.', 1),

    (19, N'Cá hồi phi lê',         N'Hải sản',                   N'kg',  420000, N'/images/products/ca-hoi-phi-le.jpg',      N'Cá hồi phi lê mát, màu tươi, cắt khúc tiện bán lẻ.', 1),
    (20, N'Cá basa phi lê',        N'Hải sản',                   N'kg',  98000,  N'/images/products/ca-basa-phi-le.jpg',     N'Cá basa phi lê trắng, ít xương, phù hợp chiên và kho.', 1),
    (21, N'Tôm thẻ',               N'Hải sản',                   N'kg',  255000, N'/images/products/tom-the.jpg',            N'Tôm thẻ size vừa, tươi lạnh, phù hợp hấp và rang.', 1),
    (22, N'Mực ống',               N'Hải sản',                   N'kg',  230000, N'/images/products/muc-ong.jpg',            N'Mực ống tươi lạnh, thân dày, phù hợp nướng và xào.', 1),
    (23, N'Cá thu cắt khúc',       N'Hải sản',                   N'kg',  185000, N'/images/products/ca-thu-cat-khuc.jpg',   N'Cá thu cắt khúc dày, thịt chắc, phù hợp kho tiêu.', 1),
    (24, N'Nghêu trắng',           N'Hải sản',                   N'kg',  65000,  N'/images/products/ngheu-trang.jpg',        N'Nghêu trắng sống, chọn lọc sạch cát.', 1),
    (25, N'Cua biển',              N'Hải sản',                   N'kg',  360000, N'/images/products/cua-bien.jpg',           N'Cua biển tươi sống, chắc thịt, phù hợp hấp và rang me.', 1),

    (26, N'Chuối già Nam Bộ',      N'Trái cây',                  N'kg',  29000,  N'/images/products/chuoi-gia-nam-bo.jpg',   N'Chuối già chín vừa, ngọt thơm, dễ bán lẻ theo nải.', 1),
    (27, N'Táo Gala',              N'Trái cây',                  N'kg',  72000,  N'/images/products/tao-gala.jpg',           N'Táo Gala giòn ngọt, trái đồng đều.', 1),
    (28, N'Cam sành',              N'Trái cây',                  N'kg',  42000,  N'/images/products/cam-sanh.jpg',           N'Cam sành mọng nước, vị đậm, phù hợp ép và ăn tươi.', 1),
    (29, N'Xoài cát Hòa Lộc',      N'Trái cây',                  N'kg',  88000,  N'/images/products/xoai-cat-hoa-loc.jpg',   N'Xoài cát Hòa Lộc thơm ngọt, chọn size trung bình.', 1),
    (30, N'Dưa hấu',               N'Trái cây',                  N'kg',  22000,  N'/images/products/dua-hau.jpg',            N'Dưa hấu ruột đỏ, bán cắt hoặc nguyên trái.', 1),
    (31, N'Nho đỏ không hạt',      N'Trái cây',                  N'hộp', 155000, N'/images/products/nho-do-khong-hat.jpg',   N'Nho đỏ không hạt đóng hộp mát, phù hợp phân khúc trung cao.', 1),
    (32, N'Bơ sáp',                N'Trái cây',                  N'kg',  76000,  N'/images/products/bo-sap.jpg',             N'Bơ sáp dẻo béo, độ chín vừa phải.', 1),
    (33, N'Thanh long ruột đỏ',    N'Trái cây',                  N'kg',  38000,  N'/images/products/thanh-long-ruot-do.jpg', N'Thanh long ruột đỏ tươi, mẫu mã đẹp.', 1),

    (34, N'Chả lụa',               N'Thực phẩm chế biến sẵn',    N'đòn', 128000, N'/images/products/cha-lua.jpg',           N'Chả lụa đòn 500g, đóng gói mát, bán nhanh theo ngày.', 1),
    (35, N'Xúc xích tươi',         N'Thực phẩm chế biến sẵn',    N'gói', 72000,  N'/images/products/xuc-xich-tuoi.jpg',     N'Xúc xích tươi gói lạnh, tiện chế biến tại nhà.', 1),
    (36, N'Đậu hũ non',            N'Thực phẩm chế biến sẵn',    N'hộp', 18000,  N'/images/products/dau-hu-non.jpg',         N'Đậu hũ non hộp mát, phù hợp nấu canh và ăn chay.', 1),
    (37, N'Sữa chua ăn',           N'Thực phẩm chế biến sẵn',    N'hộp', 32000,  N'/images/products/sua-chua-an.jpg',        N'Sữa chua ăn hộp lạnh, bán theo pack nhỏ.', 1),
    (38, N'Kim chi cải thảo',      N'Thực phẩm chế biến sẵn',    N'hũ',  68000,  N'/images/products/kim-chi-cai-thao.jpg',  N'Kim chi cải thảo hũ mát, vị cay nhẹ, quay vòng tốt.', 1),
    (39, N'Gỏi cuốn tươi',         N'Thực phẩm chế biến sẵn',    N'hộp', 45000,  N'/images/products/goi-cuon-tuoi.jpg',     N'Gỏi cuốn tươi đóng hộp, dùng trong ngày.', 1),
    (40, N'Salad trộn sẵn',        N'Thực phẩm chế biến sẵn',    N'hộp', 52000,  N'/images/products/salad-tron-san.jpg',    N'Salad rau củ trộn sẵn, cần xoay vòng FEFO chặt.', 1);

    MERGE products AS tgt
    USING #SeedProducts AS src
    ON tgt.name = src.name
    WHEN MATCHED THEN
        UPDATE SET
            tgt.category    = src.category,
            tgt.unit        = src.unit,
            tgt.sell_price  = src.sell_price,
            tgt.image_url   = src.image_url,
            tgt.description = src.description,
            tgt.active      = src.active
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (
            name, category, unit, sell_price, image_url, description, active
        )
        VALUES (
            src.name, src.category, src.unit, src.sell_price, src.image_url, src.description, src.active
        );

    IF OBJECT_ID('tempdb..#ProductMap') IS NOT NULL DROP TABLE #ProductMap;
    SELECT
        sp.product_no,
        p.id AS product_id,
        sp.name,
        sp.category,
        sp.unit,
        CAST(sp.sell_price AS DECIMAL(18,2)) AS sell_price
    INTO #ProductMap
    FROM #SeedProducts sp
    INNER JOIN products p
        ON p.name = sp.name;

    IF OBJECT_ID('tempdb..#SupplierMap') IS NOT NULL DROP TABLE #SupplierMap;
    SELECT
        s.id AS supplier_id,
        s.name
    INTO #SupplierMap
    FROM suppliers s
    WHERE s.name IN (SELECT name FROM #SeedSuppliers);

    /* =========================================================
       4. seed product_lots
       ========================================================= */
    IF OBJECT_ID('tempdb..#SeedLots') IS NOT NULL DROP TABLE #SeedLots;
    CREATE TABLE #SeedLots (
        product_id    INT            NOT NULL,
        supplier_id   INT            NOT NULL,
        import_date   DATE           NOT NULL,
        expiry_date   DATE           NOT NULL,
        qty_in        INT            NOT NULL,
        qty_left      INT            NOT NULL,
        import_price  DECIMAL(18,2)  NOT NULL
    );

    ;WITH Slots AS (
        SELECT 1 AS slot_no UNION ALL SELECT 2 UNION ALL SELECT 3
    ),
    BaseLots AS (
        SELECT
            pm.product_id,
            pm.product_no,
            pm.name,
            pm.category,
            pm.unit,
            pm.sell_price,
            s.slot_no,
            CASE pm.category
                WHEN N'Rau củ' THEN
                    CASE ((pm.product_no + s.slot_no) % 3)
                        WHEN 0 THEN N'HTX Rau Củ Đà Lạt Xanh'
                        WHEN 1 THEN N'Nông Trại VietGAP Củ Chi'
                        ELSE        N'Trang Trại Hữu Cơ Mộc Châu'
                    END
                WHEN N'Thịt' THEN
                    CASE ((pm.product_no + s.slot_no) % 3)
                        WHEN 0 THEN N'Công ty Thực phẩm Heo Sạch An Phú'
                        WHEN 1 THEN N'Trang trại Gà Tươi Bình Phước'
                        ELSE        N'Công ty Thịt Bò Tây Nguyên Fresh'
                    END
                WHEN N'Hải sản' THEN
                    CASE ((pm.product_no + s.slot_no) % 3)
                        WHEN 0 THEN N'Hải sản Phú Quốc SeaMart'
                        WHEN 1 THEN N'Vựa Hải sản Vũng Tàu Fresh'
                        ELSE        N'Công ty Thủy sản Miền Trung HACCP'
                    END
                WHEN N'Trái cây' THEN
                    CASE ((pm.product_no + s.slot_no) % 3)
                        WHEN 0 THEN N'Vườn Trái Cây Miền Tây Select'
                        WHEN 1 THEN N'Hợp tác xã Trái cây Đồng Nai'
                        ELSE        N'Trang Trại Hữu Cơ Mộc Châu'
                    END
                ELSE
                    CASE ((pm.product_no + s.slot_no) % 3)
                        WHEN 0 THEN N'Xưởng Thực phẩm Tươi Sạch Sài Gòn'
                        WHEN 1 THEN N'Trang trại Gà Tươi Bình Phước'
                        ELSE        N'Công ty Thủy sản Miền Trung HACCP'
                    END
            END AS supplier_name,
            CAST(
                pm.sell_price *
                CASE s.slot_no
                    WHEN 1 THEN 0.61
                    WHEN 2 THEN 0.565
                    ELSE       0.527
                END
                + (s.slot_no * 7) AS DECIMAL(18,2)
            ) AS import_price,
            CASE
                WHEN pm.name = N'Xà lách romaine' THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN -1 WHEN 2 THEN -3 ELSE -6 END, @Today)
                WHEN pm.name = N'Đậu hũ non'      THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN -1 WHEN 2 THEN -2 ELSE -5 END, @Today)
                WHEN pm.name = N'Mực ống'         THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN  2 WHEN 2 THEN  5 ELSE  7 END, @Today)
                WHEN pm.name IN (N'Cải xanh', N'Thịt ba chỉ heo', N'Tôm thẻ', N'Chuối già Nam Bộ', N'Kim chi cải thảo')
                     AND s.slot_no = 1            THEN DATEADD(DAY, -(1 + (pm.product_no % 4)), @Today)
                WHEN pm.name IN (N'Cà chua bi', N'Ức gà phi lê', N'Cá basa phi lê', N'Cam sành', N'Gỏi cuốn tươi')
                     AND s.slot_no = 1            THEN DATEADD(DAY, 1 + (pm.product_no % 3), @Today)
                ELSE
                    CASE pm.category
                        WHEN N'Rau củ' THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN 4 + (pm.product_no % 3) WHEN 2 THEN 6 + (pm.product_no % 4) ELSE 8 + (pm.product_no % 4) END, @Today)
                        WHEN N'Thịt'   THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN 4 + (pm.product_no % 4) WHEN 2 THEN 7 + (pm.product_no % 4) ELSE 9 + (pm.product_no % 5) END, @Today)
                        WHEN N'Hải sản' THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN 3 + (pm.product_no % 4) WHEN 2 THEN 6 + (pm.product_no % 4) ELSE 8 + (pm.product_no % 4) END, @Today)
                        WHEN N'Trái cây' THEN DATEADD(DAY, CASE s.slot_no WHEN 1 THEN 5 + (pm.product_no % 5) WHEN 2 THEN 8 + (pm.product_no % 6) ELSE 11 + (pm.product_no % 6) END, @Today)
                        ELSE              DATEADD(DAY, CASE s.slot_no WHEN 1 THEN 7 + (pm.product_no % 6) WHEN 2 THEN 12 + (pm.product_no % 6) ELSE 18 + (pm.product_no % 7) END, @Today)
                    END
            END AS expiry_date,
            CASE
                WHEN pm.category = N'Rau củ' THEN DATEADD(DAY, -(CASE s.slot_no WHEN 1 THEN 2 + (pm.product_no % 3) WHEN 2 THEN 4 + (pm.product_no % 3) ELSE 6 + (pm.product_no % 4) END), @Today)
                WHEN pm.category = N'Thịt'   THEN DATEADD(DAY, -(CASE s.slot_no WHEN 1 THEN 3 + (pm.product_no % 3) WHEN 2 THEN 5 + (pm.product_no % 4) ELSE 7 + (pm.product_no % 4) END), @Today)
                WHEN pm.category = N'Hải sản' THEN DATEADD(DAY, -(CASE s.slot_no WHEN 1 THEN 2 + (pm.product_no % 2) WHEN 2 THEN 4 + (pm.product_no % 3) ELSE 6 + (pm.product_no % 4) END), @Today)
                WHEN pm.category = N'Trái cây' THEN DATEADD(DAY, -(CASE s.slot_no WHEN 1 THEN 4 + (pm.product_no % 3) WHEN 2 THEN 7 + (pm.product_no % 4) ELSE 10 + (pm.product_no % 5) END), @Today)
                ELSE                            DATEADD(DAY, -(CASE s.slot_no WHEN 1 THEN 5 + (pm.product_no % 4) WHEN 2 THEN 10 + (pm.product_no % 5) ELSE 16 + (pm.product_no % 6) END), @Today)
            END AS import_date,
            CASE
                WHEN pm.category = N'Rau củ' THEN CASE s.slot_no WHEN 1 THEN 35 + (pm.product_no % 8)  WHEN 2 THEN 48 + (pm.product_no % 10) ELSE 60 + (pm.product_no % 12) END
                WHEN pm.category = N'Thịt'   THEN CASE s.slot_no WHEN 1 THEN 22 + (pm.product_no % 6)  WHEN 2 THEN 30 + (pm.product_no % 7)  ELSE 38 + (pm.product_no % 8)  END
                WHEN pm.category = N'Hải sản' THEN CASE s.slot_no WHEN 1 THEN 20 + (pm.product_no % 6) WHEN 2 THEN 28 + (pm.product_no % 7)  ELSE 34 + (pm.product_no % 8)  END
                WHEN pm.category = N'Trái cây' THEN CASE s.slot_no WHEN 1 THEN 30 + (pm.product_no % 8) WHEN 2 THEN 42 + (pm.product_no % 10) ELSE 54 + (pm.product_no % 12) END
                ELSE                            CASE s.slot_no WHEN 1 THEN 18 + (pm.product_no % 5) WHEN 2 THEN 24 + (pm.product_no % 6) ELSE 30 + (pm.product_no % 7) END
            END AS qty_in,
            CASE
                WHEN pm.name = N'Rau muống' THEN CASE s.slot_no WHEN 1 THEN 5 WHEN 2 THEN 7 ELSE 4 END
                WHEN pm.name = N'Cá hồi phi lê' THEN CASE s.slot_no WHEN 1 THEN 4 WHEN 2 THEN 6 ELSE 5 END
                WHEN pm.name = N'Nho đỏ không hạt' THEN CASE s.slot_no WHEN 1 THEN 8 WHEN 2 THEN 6 ELSE 3 END
                WHEN pm.name = N'Chả lụa' THEN CASE s.slot_no WHEN 1 THEN 5 WHEN 2 THEN 6 ELSE 5 END
                WHEN pm.name = N'Ức gà phi lê' THEN CASE s.slot_no WHEN 1 THEN 4 WHEN 2 THEN 6 ELSE 5 END

                WHEN pm.name = N'Xà lách romaine' THEN CASE s.slot_no WHEN 1 THEN 8 WHEN 2 THEN 5 ELSE 3 END
                WHEN pm.name = N'Đậu hũ non'      THEN CASE s.slot_no WHEN 1 THEN 6 WHEN 2 THEN 4 ELSE 0 END
                WHEN pm.name = N'Mực ống'         THEN 0

                WHEN pm.name IN (N'Cải xanh', N'Thịt ba chỉ heo', N'Tôm thẻ', N'Chuối già Nam Bộ', N'Kim chi cải thảo')
                     AND s.slot_no = 1 THEN 7 + (pm.product_no % 5)

                ELSE
                    CASE pm.category
                        WHEN N'Rau củ' THEN CASE s.slot_no WHEN 1 THEN 10 + (pm.product_no % 6) WHEN 2 THEN 18 + (pm.product_no % 8) ELSE 20 + (pm.product_no % 10) END
                        WHEN N'Thịt'   THEN CASE s.slot_no WHEN 1 THEN 8  + (pm.product_no % 4) WHEN 2 THEN 14 + (pm.product_no % 6) ELSE 16 + (pm.product_no % 7) END
                        WHEN N'Hải sản' THEN CASE s.slot_no WHEN 1 THEN 7 + (pm.product_no % 4) WHEN 2 THEN 12 + (pm.product_no % 5) ELSE 15 + (pm.product_no % 6) END
                        WHEN N'Trái cây' THEN CASE s.slot_no WHEN 1 THEN 12 + (pm.product_no % 6) WHEN 2 THEN 18 + (pm.product_no % 7) ELSE 22 + (pm.product_no % 8) END
                        ELSE              CASE s.slot_no WHEN 1 THEN 6 + (pm.product_no % 4) WHEN 2 THEN 10 + (pm.product_no % 5) ELSE 13 + (pm.product_no % 6) END
                    END
            END AS qty_left
        FROM #ProductMap pm
        CROSS JOIN Slots s
    )
    INSERT INTO #SeedLots (product_id, supplier_id, import_date, expiry_date, qty_in, qty_left, import_price)
    SELECT
        b.product_id,
        sm.supplier_id,
        CASE WHEN b.import_date > b.expiry_date THEN DATEADD(DAY, -1, b.expiry_date) ELSE b.import_date END AS import_date,
        b.expiry_date,
        b.qty_in,
        CASE WHEN b.qty_left > b.qty_in THEN b.qty_in ELSE b.qty_left END AS qty_left,
        b.import_price
    FROM BaseLots b
    INNER JOIN #SupplierMap sm
        ON sm.name = b.supplier_name;

    MERGE product_lots AS tgt
    USING #SeedLots AS src
    ON tgt.product_id = src.product_id
       AND tgt.import_price = src.import_price
    WHEN MATCHED THEN
        UPDATE SET
            tgt.supplier_id  = src.supplier_id,
            tgt.import_date  = src.import_date,
            tgt.expiry_date  = src.expiry_date,
            tgt.qty_in       = src.qty_in,
            tgt.qty_left     = src.qty_left
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (
            product_id, supplier_id, import_date, expiry_date, qty_in, qty_left, import_price
        )
        VALUES (
            src.product_id, src.supplier_id, src.import_date, src.expiry_date, src.qty_in, src.qty_left, src.import_price
        );

    /* =========================================================
       5. seed app_settings
       ========================================================= */
    IF OBJECT_ID('tempdb..#SeedSettings') IS NOT NULL DROP TABLE #SeedSettings;
    CREATE TABLE #SeedSettings (
        setting_key   NVARCHAR(100) NOT NULL,
        setting_value NVARCHAR(255) NOT NULL,
        description   NVARCHAR(255) NULL
    );

    INSERT INTO #SeedSettings (setting_key, setting_value, description)
    VALUES
    (N'low_stock_threshold',            N'50', N'Ngưỡng cảnh báo tồn kho thấp theo tổng qty_left còn khả dụng.'),
    (N'upcoming_expiry_days',           N'7',  N'Số ngày để cảnh báo lô sắp hết hạn trên dashboard.'),
    (N'replenish_history_days',         N'30', N'Số ngày lịch sử dùng để gợi ý bổ sung hàng.'),
    (N'replenish_lead_days',            N'3',  N'Số ngày lead time mặc định dùng cho tính toán bổ sung.'),
    (N'replenish_buffer_days',          N'2',  N'Số ngày đệm trong đề xuất nhập hàng.'),
    (N'replenish_safety_days',          N'2',  N'Số ngày an toàn tối thiểu cho lượng tồn.'),
    (N'subscription_notify_days',       N'7',  N'Số ngày trước hạn để nhắc gia hạn gói PRO.'),
    (N'subscription_grace_period_days', N'3',  N'Số ngày gia hạn ân hạn sau khi PRO hết hạn.');

    MERGE app_settings AS tgt
    USING (
        SELECT
            setting_key,
            setting_value,
            description,
            @Now AS updated_at
        FROM #SeedSettings
    ) AS src
    ON tgt.setting_key = src.setting_key
    WHEN MATCHED THEN
        UPDATE SET
            tgt.setting_value = src.setting_value,
            tgt.description   = src.description,
            tgt.updated_at    = src.updated_at
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (
            setting_key, setting_value, description, updated_at
        )
        VALUES (
            src.setting_key, src.setting_value, src.description, src.updated_at
        );

    /* =========================================================
       6. seed subscriptions + tier_history
       ========================================================= */
    DELETE th
    FROM tier_history th
    WHERE CHARINDEX(N'[SEEDFM]', ISNULL(th.note, N'')) > 0;

    DELETE sp
    FROM subscription_payments sp
    WHERE sp.payment_code LIKE N'SEEDSUB-%';

    -- reset tier of seeded customers before applying latest subscription state
    UPDATE u
    SET
        u.tier = N'FREE',
        u.expired_date = NULL
    FROM users u
    WHERE u.username IN (
        SELECT username FROM #SeedUsers WHERE role = N'CUSTOMER'
    );

    IF OBJECT_ID('tempdb..#SeedPayments') IS NOT NULL DROP TABLE #SeedPayments;
    CREATE TABLE #SeedPayments (
        payment_code    NVARCHAR(50)  NOT NULL,
        username        NVARCHAR(100) NOT NULL,
        plan_name       NVARCHAR(50)  NOT NULL,
        plan_days       INT           NOT NULL,
        amount          DECIMAL(18,2) NOT NULL,
        payment_method  NVARCHAR(30)  NOT NULL,
        payment_status  NVARCHAR(30)  NOT NULL,
        start_offset    INT           NOT NULL,
        end_offset      INT           NOT NULL,
        note            NVARCHAR(255) NULL,
        created_offset  INT           NOT NULL
    );

    INSERT INTO #SeedPayments (
        payment_code, username, plan_name, plan_days, amount, payment_method, payment_status, start_offset, end_offset, note, created_offset
    )
    VALUES
    (N'SEEDSUB-001', N'cust_01', N'PRO_MONTHLY',   30,  99000, N'QR',            N'COMPLETED', -18,  12, N'[SEEDFM] Gói PRO đang hoạt động bình thường.',          -18),
    (N'SEEDSUB-002', N'cust_02', N'PRO_MONTHLY',   30,  99000, N'CARD',          N'COMPLETED', -10,  20, N'[SEEDFM] Gói PRO đang hoạt động bình thường.',          -10),
    (N'SEEDSUB-003', N'cust_03', N'PRO_QUARTERLY', 90, 279000, N'BANK_TRANSFER', N'COMPLETED', -40,  50, N'[SEEDFM] Gói PRO quý còn hạn dài.',                    -40),
    (N'SEEDSUB-004', N'cust_04', N'PRO_MONTHLY',   30,  99000, N'QR',            N'COMPLETED', -25,   5, N'[SEEDFM] Gói PRO sắp hết hạn trong 7 ngày.',          -25),
    (N'SEEDSUB-005', N'cust_05', N'PRO_MONTHLY',   30,  99000, N'CARD',          N'COMPLETED', -23,   7, N'[SEEDFM] Gói PRO sắp hết hạn trong 7 ngày.',          -23),
    (N'SEEDSUB-006', N'cust_06', N'PRO_MONTHLY',   30,  99000, N'QR',            N'COMPLETED', -28,   2, N'[SEEDFM] Gói PRO sắp hết hạn trong 7 ngày.',          -28),
    (N'SEEDSUB-007', N'cust_07', N'PRO_MONTHLY',   30,  99000, N'BANK_TRANSFER', N'COMPLETED', -40, -10, N'[SEEDFM] Gói PRO đã hết hạn và bị downgrade.',       -40),
    (N'SEEDSUB-008', N'cust_08', N'PRO_MONTHLY',   30,  99000, N'CARD',          N'COMPLETED', -33,  -3, N'[SEEDFM] Gói PRO vừa hết hạn và bị downgrade.',      -33),
    (N'SEEDSUB-009', N'cust_09', N'PRO_QUARTERLY', 90, 279000, N'BANK_TRANSFER', N'COMPLETED', -60,  30, N'[SEEDFM] Gói PRO quý đang hoạt động.',                -60),
    (N'SEEDSUB-010', N'cust_10', N'PRO_MONTHLY',   30,  99000, N'QR',            N'COMPLETED',  -5,  25, N'[SEEDFM] Gói PRO mới gia hạn.',                       -5),
    (N'SEEDSUB-011', N'cust_11', N'PRO_MONTHLY',   30,  99000, N'CARD',          N'COMPLETED', -70, -40, N'[SEEDFM] Thanh toán lịch sử đã hết hiệu lực.',      -70),
    (N'SEEDSUB-012', N'cust_12', N'PRO_MONTHLY',   30,  99000, N'QR',            N'PENDING',      1,  31, N'[SEEDFM] Yêu cầu nâng cấp đang chờ xác nhận.',         0);

    INSERT INTO subscription_payments (
        user_id, payment_code, plan_name, plan_days, amount, payment_method, payment_status, start_date, end_date, note, created_at
    )
    SELECT
        u.id,
        sp.payment_code,
        sp.plan_name,
        sp.plan_days,
        sp.amount,
        sp.payment_method,
        sp.payment_status,
        DATEADD(DAY, sp.start_offset, @Today) AS start_date,
        DATEADD(DAY, sp.end_offset, @Today)   AS end_date,
        sp.note,
        DATEADD(DAY, sp.created_offset, @Now) AS created_at
    FROM #SeedPayments sp
    INNER JOIN users u
        ON u.username = sp.username;

    INSERT INTO tier_history (
        user_id, old_tier, new_tier, old_expired_date, new_expired_date, change_type, note, created_at
    )
    SELECT
        u.id,
        N'FREE',
        N'PRO',
        NULL,
        DATEADD(DAY, sp.end_offset, @Today),
        CASE WHEN sp.plan_name = N'PRO_QUARTERLY' THEN N'UPGRADE' ELSE N'UPGRADE' END,
        N'[SEEDFM] Khởi tạo/gia hạn PRO từ seed payment ' + sp.payment_code,
        DATEADD(DAY, sp.start_offset, @Now)
    FROM #SeedPayments sp
    INNER JOIN users u
        ON u.username = sp.username
    WHERE sp.payment_status = N'COMPLETED'
      AND sp.payment_code IN (N'SEEDSUB-001', N'SEEDSUB-002', N'SEEDSUB-003', N'SEEDSUB-004', N'SEEDSUB-005', N'SEEDSUB-006', N'SEEDSUB-007', N'SEEDSUB-008', N'SEEDSUB-009', N'SEEDSUB-010', N'SEEDSUB-011');

    INSERT INTO tier_history (
        user_id, old_tier, new_tier, old_expired_date, new_expired_date, change_type, note, created_at
    )
    SELECT
        u.id,
        N'PRO',
        N'FREE',
        DATEADD(DAY, sp.end_offset, @Today),
        NULL,
        N'EXPIRE',
        N'[SEEDFM] Gói PRO hết hạn, auto downgrade về FREE từ seed payment ' + sp.payment_code,
        DATEADD(DAY, sp.end_offset + 1, @Now)
    FROM #SeedPayments sp
    INNER JOIN users u
        ON u.username = sp.username
    WHERE sp.payment_code IN (N'SEEDSUB-007', N'SEEDSUB-008')
      AND sp.payment_status = N'COMPLETED';

    ;WITH LatestCompleted AS (
        SELECT
            sp.user_id,
            sp.start_date,
            sp.end_date,
            ROW_NUMBER() OVER (PARTITION BY sp.user_id ORDER BY sp.end_date DESC, sp.created_at DESC, sp.id DESC) AS rn
        FROM subscription_payments sp
        INNER JOIN users u
            ON u.id = sp.user_id
        WHERE u.username IN (SELECT username FROM #SeedUsers WHERE role = N'CUSTOMER')
          AND sp.payment_status = N'COMPLETED'
    )
    UPDATE u
    SET
        u.tier = CASE WHEN lc.end_date >= @Today THEN N'PRO' ELSE N'FREE' END,
        u.expired_date = CASE WHEN lc.end_date >= @Today THEN lc.end_date ELSE NULL END
    FROM users u
    INNER JOIN LatestCompleted lc
        ON lc.user_id = u.id
       AND lc.rn = 1;

    /* =========================================================
       7. seed orders + order_items
       ========================================================= */
    DELETE oi
    FROM order_items oi
    INNER JOIN orders o
        ON o.id = oi.order_id
    WHERE o.order_code LIKE N'SEEDFM-ORD-%';

    DELETE o
    FROM orders o
    WHERE o.order_code LIKE N'SEEDFM-ORD-%';

    IF OBJECT_ID('tempdb..#CustomerMap') IS NOT NULL DROP TABLE #CustomerMap;
    SELECT
        ROW_NUMBER() OVER (ORDER BY u.username) AS customer_no,
        u.id
    INTO #CustomerMap
    FROM users u
    WHERE u.username IN (
        SELECT username FROM #SeedUsers WHERE role = N'CUSTOMER'
    );

    IF OBJECT_ID('tempdb..#SellerMap') IS NOT NULL DROP TABLE #SellerMap;
    SELECT
        ROW_NUMBER() OVER (ORDER BY u.username) AS seller_no,
        u.id
    INTO #SellerMap
    FROM users u
    WHERE u.username IN (
        SELECT username FROM #SeedUsers WHERE role = N'SELLER'
    );

    IF OBJECT_ID('tempdb..#StaffMap') IS NOT NULL DROP TABLE #StaffMap;
    SELECT
        ROW_NUMBER() OVER (ORDER BY u.username) AS staff_no,
        u.id
    INTO #StaffMap
    FROM users u
    WHERE u.username IN (
        SELECT username FROM #SeedUsers WHERE role = N'STAFF'
    );

    DECLARE @CustomerCount INT = (SELECT COUNT(*) FROM #CustomerMap);
    DECLARE @SellerCount   INT = (SELECT COUNT(*) FROM #SellerMap);
    DECLARE @StaffCount    INT = (SELECT COUNT(*) FROM #StaffMap);

    IF OBJECT_ID('tempdb..#SeedOrders') IS NOT NULL DROP TABLE #SeedOrders;
    CREATE TABLE #SeedOrders (
        order_no        INT            NOT NULL,
        order_code      NVARCHAR(50)   NOT NULL,
        customer_id     INT            NULL,
        created_by      INT            NOT NULL,
        type            NVARCHAR(20)   NOT NULL,
        status          NVARCHAR(20)   NOT NULL,
        payment_method  NVARCHAR(30)   NOT NULL,
        total_amount    DECIMAL(18,2)  NOT NULL,
        created_at      DATETIME2(0)   NOT NULL,
        completed_at    DATETIME2(0)   NULL
    );

    ;WITH N AS (
        SELECT 1 AS n
        UNION ALL
        SELECT n + 1 FROM N WHERE n < 330
    )
    INSERT INTO #SeedOrders (
        order_no, order_code, customer_id, created_by, type, status, payment_method, total_amount, created_at, completed_at
    )
    SELECT
        n.n AS order_no,
        CONCAT(N'SEEDFM-ORD-', RIGHT(CONCAT(N'0000', n.n), 4)) AS order_code,
        CASE
            WHEN n.n % 10 IN (1,2,3,4,5,6) THEN cm.id
            ELSE NULL
        END AS customer_id,
        CASE
            WHEN n.n % 8 = 0 THEN st.id
            ELSE sl.id
        END AS created_by,
        CASE
            WHEN n.n % 10 IN (1,2,3,4,5,6) THEN N'ONLINE'
            ELSE N'WALK_IN'
        END AS type,
        CASE
            WHEN n.n % 16 = 0 THEN N'CANCELED'
            WHEN n.n % 14 = 0 THEN N'PENDING'
            WHEN n.n % 12 = 0 THEN N'PROCESSING'
            WHEN n.n % 10 = 0 THEN N'SHIPPING'
            ELSE N'COMPLETED'
        END AS status,
        CASE
            WHEN n.n % 10 IN (1,2,3,4,5,6) THEN
                CASE (n.n % 5)
                    WHEN 0 THEN N'COD'
                    WHEN 1 THEN N'QR'
                    WHEN 2 THEN N'CARD'
                    WHEN 3 THEN N'BANK_TRANSFER'
                    ELSE       N'QR'
                END
            ELSE
                CASE (n.n % 3)
                    WHEN 0 THEN N'CASH'
                    WHEN 1 THEN N'QR'
                    ELSE       N'CARD'
                END
        END AS payment_method,
        CAST(0 AS DECIMAL(18,2)) AS total_amount,
        DATEADD(MINUTE, ((n.n * 37) % 600) + 420, CAST(DATEADD(DAY, -(330 - n.n), @Today) AS DATETIME2(0))) AS created_at,
        CASE
            WHEN CASE
                    WHEN n.n % 16 = 0 THEN N'CANCELED'
                    WHEN n.n % 14 = 0 THEN N'PENDING'
                    WHEN n.n % 12 = 0 THEN N'PROCESSING'
                    WHEN n.n % 10 = 0 THEN N'SHIPPING'
                    ELSE N'COMPLETED'
                 END = N'COMPLETED'
            THEN DATEADD(MINUTE, 20 + ((n.n * 11) % 180), DATEADD(MINUTE, ((n.n * 37) % 600) + 420, CAST(DATEADD(DAY, -(330 - n.n), @Today) AS DATETIME2(0))))
            ELSE NULL
        END AS completed_at
    FROM N
    LEFT JOIN #CustomerMap cm
        ON cm.customer_no = ((n.n - 1) % @CustomerCount) + 1
    LEFT JOIN #SellerMap sl
        ON sl.seller_no = ((n.n - 1) % @SellerCount) + 1
    LEFT JOIN #StaffMap st
        ON st.staff_no = ((n.n - 1) % @StaffCount) + 1
    OPTION (MAXRECURSION 0);

    INSERT INTO orders (
        order_code, customer_id, created_by, type, status, payment_method, total_amount, created_at, completed_at
    )
    SELECT
        order_code, customer_id, created_by, type, status, payment_method, total_amount, created_at, completed_at
    FROM #SeedOrders;

    IF OBJECT_ID('tempdb..#OrderMap') IS NOT NULL DROP TABLE #OrderMap;
    SELECT
        so.order_no,
        o.id AS order_id,
        so.order_code
    INTO #OrderMap
    FROM #SeedOrders so
    INNER JOIN orders o
        ON o.order_code = so.order_code;

    IF OBJECT_ID('tempdb..#SeedOrderItems') IS NOT NULL DROP TABLE #SeedOrderItems;
    CREATE TABLE #SeedOrderItems (
        order_id     INT            NOT NULL,
        product_id   INT            NOT NULL,
        quantity     INT            NOT NULL,
        unit_price   DECIMAL(18,2)  NOT NULL,
        line_total   DECIMAL(18,2)  NOT NULL
    );

    ;WITH ItemSlots AS (
        SELECT 1 AS item_no UNION ALL SELECT 2 UNION ALL SELECT 3
    ),
    RawItems AS (
        SELECT
            om.order_id,
            om.order_no,
            i.item_no,
            CASE i.item_no
                WHEN 1 THEN ((om.order_no * 7) % 40) + 1
                WHEN 2 THEN ((om.order_no * 7 + 13) % 40) + 1
                ELSE       ((om.order_no * 7 + 27) % 40) + 1
            END AS product_no
        FROM #OrderMap om
        CROSS JOIN ItemSlots i
        WHERE i.item_no <= CASE WHEN om.order_no % 2 = 0 THEN 2 ELSE 3 END
    )
    INSERT INTO #SeedOrderItems (
        order_id, product_id, quantity, unit_price, line_total
    )
    SELECT
        ri.order_id,
        pm.product_id,
        CASE
            WHEN pm.unit = N'kg'  THEN 1 + ((ri.order_no + ri.item_no) % 3)
            WHEN pm.unit = N'bó'  THEN 1 + ((ri.order_no + ri.item_no) % 4)
            WHEN pm.unit = N'đòn' THEN 1 + ((ri.order_no + ri.item_no) % 2)
            ELSE 1 + ((ri.order_no + ri.item_no) % 3)
        END AS quantity,
        pm.sell_price AS unit_price,
        CAST(
            (CASE
                WHEN pm.unit = N'kg'  THEN 1 + ((ri.order_no + ri.item_no) % 3)
                WHEN pm.unit = N'bó'  THEN 1 + ((ri.order_no + ri.item_no) % 4)
                WHEN pm.unit = N'đòn' THEN 1 + ((ri.order_no + ri.item_no) % 2)
                ELSE 1 + ((ri.order_no + ri.item_no) % 3)
             END) * pm.sell_price AS DECIMAL(18,2)
        ) AS line_total
    FROM RawItems ri
    INNER JOIN #ProductMap pm
        ON pm.product_no = ri.product_no;

    INSERT INTO order_items (
        order_id, product_id, quantity, unit_price, line_total
    )
    SELECT
        order_id, product_id, quantity, unit_price, line_total
    FROM #SeedOrderItems;

    UPDATE o
    SET o.total_amount = x.total_amount
    FROM orders o
    INNER JOIN (
        SELECT
            oi.order_id,
            CAST(SUM(oi.line_total) AS DECIMAL(18,2)) AS total_amount
        FROM order_items oi
        INNER JOIN orders oo
            ON oo.id = oi.order_id
        WHERE oo.order_code LIKE N'SEEDFM-ORD-%'
        GROUP BY oi.order_id
    ) x
        ON x.order_id = o.id
    WHERE o.order_code LIKE N'SEEDFM-ORD-%';

    /* =========================================================
       8. rebuild revenue_daily
       generate riêng để luôn có 365 điểm dữ liệu liên tục,
       phù hợp chart, moving average và forecast demo
       ========================================================= */
    ;WITH D AS (
        SELECT 0 AS day_idx, @Today AS revenue_date
        UNION ALL
        SELECT day_idx + 1, DATEADD(DAY, -1, revenue_date)
        FROM D
        WHERE day_idx < 364
    ),
    RevenueSource AS (
        SELECT
            revenue_date,
            CAST(
                5500000
                + ((364 - day_idx) * 8000)
                + CASE WHEN DATEPART(WEEKDAY, revenue_date) IN (6,7) THEN 1200000 ELSE 0 END
                + CASE
                    WHEN MONTH(revenue_date) IN (1, 8, 12) THEN 600000
                    WHEN MONTH(revenue_date) IN (4, 5) THEN 250000
                    WHEN MONTH(revenue_date) = 7 THEN -200000
                    ELSE 0
                  END
                + ((ABS(CHECKSUM(CONVERT(CHAR(8), revenue_date, 112))) % 900001) - 450000)
                AS DECIMAL(18,2)
            ) AS total_revenue
        FROM D
    )
    MERGE revenue_daily AS tgt
    USING RevenueSource AS src
    ON tgt.revenue_date = src.revenue_date
    WHEN MATCHED THEN
        UPDATE SET
            tgt.total_revenue = CASE WHEN src.total_revenue < 1800000 THEN 1800000 ELSE src.total_revenue END
    WHEN NOT MATCHED BY TARGET THEN
        INSERT (revenue_date, total_revenue)
        VALUES (src.revenue_date, CASE WHEN src.total_revenue < 1800000 THEN 1800000 ELSE src.total_revenue END);

    COMMIT TRAN;

    /* =========================================================
       quick checks
       ========================================================= */

    -- số users theo role
    SELECT
        role,
        COUNT(*) AS user_count
    FROM users
    GROUP BY role
    ORDER BY role;

    -- số products theo category
    SELECT
        category,
        COUNT(*) AS product_count
    FROM products
    GROUP BY category
    ORDER BY category;

    -- số lots expired / expiring soon / active
    SELECT
        SUM(CASE WHEN expiry_date < @Today THEN 1 ELSE 0 END) AS expired_lots,
        SUM(CASE WHEN expiry_date BETWEEN @Today AND DATEADD(DAY, 7, @Today) THEN 1 ELSE 0 END) AS expiring_soon_lots,
        SUM(CASE WHEN expiry_date > DATEADD(DAY, 7, @Today) THEN 1 ELSE 0 END) AS active_lots
    FROM product_lots
    WHERE product_id IN (SELECT product_id FROM #ProductMap);

    -- top 10 sản phẩm availableQty thấp
    SELECT TOP 10
        p.name,
        p.category,
        p.unit,
        COALESCE(SUM(CASE WHEN pl.expiry_date >= @Today THEN pl.qty_left ELSE 0 END), 0) AS availableQty
    FROM products p
    LEFT JOIN product_lots pl
        ON pl.product_id = p.id
    WHERE p.id IN (SELECT product_id FROM #ProductMap)
    GROUP BY p.name, p.category, p.unit
    ORDER BY availableQty ASC, p.name ASC;

    -- doanh thu 30 ngày gần nhất
    SELECT
        revenue_date,
        total_revenue
    FROM revenue_daily
    WHERE revenue_date BETWEEN DATEADD(DAY, -29, @Today) AND @Today
    ORDER BY revenue_date;

    -- số subscription theo tier/status
    SELECT
        u.tier,
        CASE
            WHEN u.tier = N'PRO' AND u.expired_date IS NOT NULL AND u.expired_date >= @Today AND DATEDIFF(DAY, @Today, u.expired_date) <= 7 THEN N'PRO_EXPIRING_SOON'
            WHEN u.tier = N'PRO' AND u.expired_date IS NOT NULL AND u.expired_date >= @Today THEN N'PRO_ACTIVE'
            WHEN u.tier = N'FREE' AND EXISTS (
                SELECT 1
                FROM subscription_payments sp
                WHERE sp.user_id = u.id
                  AND sp.payment_status = N'COMPLETED'
            ) THEN N'FREE_AFTER_EXPIRE'
            ELSE N'FREE'
        END AS subscription_status,
        COUNT(*) AS user_count
    FROM users u
    WHERE u.role = N'CUSTOMER'
    GROUP BY
        u.tier,
        CASE
            WHEN u.tier = N'PRO' AND u.expired_date IS NOT NULL AND u.expired_date >= @Today AND DATEDIFF(DAY, @Today, u.expired_date) <= 7 THEN N'PRO_EXPIRING_SOON'
            WHEN u.tier = N'PRO' AND u.expired_date IS NOT NULL AND u.expired_date >= @Today THEN N'PRO_ACTIVE'
            WHEN u.tier = N'FREE' AND EXISTS (
                SELECT 1
                FROM subscription_payments sp
                WHERE sp.user_id = u.id
                  AND sp.payment_status = N'COMPLETED'
            ) THEN N'FREE_AFTER_EXPIRE'
            ELSE N'FREE'
        END
    ORDER BY u.tier, subscription_status;

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRAN;

    THROW;
END CATCH;