package clientChat;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {
//    public static Logger log = Logger.getLogger(Application.class);

    public static void main(String[] args) {
        //log.info("start server");
        SpringApplication.run(Application.class, args);
    }
}
