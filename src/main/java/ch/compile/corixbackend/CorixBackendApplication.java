package ch.compile.corixbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@SpringBootApplication
public class CorixBackendApplication {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Corix Backend")
						.version("1.0.0")
						.description("Corix Backend API"));
	}

	public static void main(String[] args) {
		SpringApplication.run(CorixBackendApplication.class, args);
	}

}
