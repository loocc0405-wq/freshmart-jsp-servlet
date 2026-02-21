package com.freshmart.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class CodeGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private CodeGenerator() {}

    public static String orderCode() {
        String ts = LocalDateTime.now().format(FMT);
        int rnd = ThreadLocalRandom.current().nextInt(100, 1000);
        return "OD" + ts + rnd;
    }
}
