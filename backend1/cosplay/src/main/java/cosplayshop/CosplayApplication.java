package cosplayshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CosplayApplication {

    public static void main(String[] args) {

        SpringApplication.run(CosplayApplication.class, args);
        System.out.println("CosplayShop started");
    }

}
