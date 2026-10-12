package feusap.org.notificatioservice;

import org.springframework.boot.SpringApplication;

public class TestNotificatioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(NotificatioServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
