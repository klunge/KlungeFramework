package io.klunge.api.emon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mehment Ali Aydın
 */
@Slf4j
@SpringBootApplication
public class EmonApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmonApplication.class, args);
    }
}
