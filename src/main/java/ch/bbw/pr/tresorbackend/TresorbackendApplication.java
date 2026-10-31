package ch.bbw.pr.tresorbackend;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TresorbackendApplication {
    @Getter
    private static final Logger logger = LoggerFactory.getLogger(TresorbackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TresorbackendApplication.class, args);
    }
}
