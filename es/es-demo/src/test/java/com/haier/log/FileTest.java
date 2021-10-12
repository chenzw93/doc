package com.haier.log;

import com.google.common.base.Joiner;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class FileTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsApplicationTests.class);
    private static final Pattern PATTERN = Pattern.compile("^(\\d{4})([/-]\\d{2}){2}\\s\\d{1,2}(:\\d{1,2}){2}.*$");

//    @Test
    void test_read_log() throws IOException {
        List<String> content = new ArrayList<>(300000);
        List<String> temp = new ArrayList<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get("D:\\haier\\work\\damand\\elastic\\paas_bak.log"))) {
            String line = null;
            while (Objects.nonNull(line = bufferedReader.readLine())) {
                if (PATTERN.matcher(line).matches()) {
                    if (!CollectionUtils.isEmpty(temp)) {
                        temp.add(0, content.remove(content.size() - 1));
                        content.add(Joiner.on("").join(temp));
                        temp.clear();
                    }
                    content.add(line);
                    continue;
                }
                temp.add(line);
            }
        }
        LOGGER.info("list: {}", content);
    }
}
