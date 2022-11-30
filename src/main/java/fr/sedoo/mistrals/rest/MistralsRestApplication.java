package fr.sedoo.mistrals.rest;

import javax.servlet.Filter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = { "fr.sedoo.mistrals" })
public class MistralsRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MistralsRestApplication.class, args);
	}

	@Bean
	public Filter etagFilter() {
		return new ShallowEtagHeaderFilter();
	}

}
