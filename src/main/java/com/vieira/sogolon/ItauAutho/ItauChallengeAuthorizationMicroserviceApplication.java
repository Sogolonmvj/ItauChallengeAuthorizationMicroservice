package com.vieira.sogolon.ItauAutho;

import com.vieira.sogolon.ItauAutho.domain.Role;
import com.vieira.sogolon.ItauAutho.domain.UserCritic;
import com.vieira.sogolon.ItauAutho.enums.UserRole;
import com.vieira.sogolon.ItauAutho.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ItauChallengeAuthorizationMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItauChallengeAuthorizationMicroserviceApplication.class, args);
	}

	@Bean
	CommandLineRunner run(UserService userService) {
		return args -> {
			userService.saveRole(new Role(null, "READER"));
			userService.saveRole(new Role(null, "BASIC"));
			userService.saveRole(new Role(null, "ADVANCED"));
			userService.saveRole(new Role(null, "MODERATOR"));

			userService.saveUser(new UserCritic("John", "Travolta", "john@gmail.com", "1234", UserRole.READER));
			userService.saveUser(new UserCritic("Will", "Smith", "will@gmail.com", "1234", UserRole.READER));
			userService.saveUser(new UserCritic("Jim", "Carry", "jim@gmail.com", "1234", UserRole.MODERATOR));
			userService.saveUser(new UserCritic("Arnold", "Schwarzenegger", "arnold@gmail.com", "1234", UserRole.ADVANCED));
			userService.saveUser(new UserCritic("Carl", "Cox", "cox@gmail.com", "1234", UserRole.BASIC));

			userService.addRoleToUser("john@gmail.com", "READER");
			userService.addRoleToUser("will@gmail.com", "READER");
			userService.addRoleToUser("jim@gmail.com", "MODERATOR");
			userService.addRoleToUser("arnold@gmail.com", "ADVANCED");
			userService.addRoleToUser("cox@gmail.com", "BASIC");
		};
	}

}
