package trchicuong.ezweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EzwebApplication {

	public static void main(String[] args) {
		SpringApplication.run(EzwebApplication.class, args);
	}

}
