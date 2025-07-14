package io.github.joabsonlg.sigac_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SigacApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SigacApiApplication.class, args);
	}

}
