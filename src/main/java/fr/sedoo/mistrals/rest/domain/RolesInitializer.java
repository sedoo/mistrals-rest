package fr.sedoo.mistrals.rest.domain;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import fr.sedoo.mistrals.rest.config.ApplicationConfig;
import fr.sedoo.sso.utils.ClientProvider;
import fr.sedoo.sso.utils.RoleProvider;
import fr.sedoo.sso.utils.TokenProvider;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class RolesInitializer {

	@Autowired
    ApplicationConfig config;

	@PostConstruct
	 public void initClientAndRoles() {
		 
		try {
			String token = TokenProvider.getToken(config.getLogin(), config.getPassword());
			ClientProvider.createBackendClient("catalogue-mistrals-services", token);
			
			RestTemplate restTemplate = new RestTemplate();
			String url = "https://api.sedoo.fr/catalogue-mistrals-prod/metadata/projects?language=en&all=false";
			
			String urlCache = "https://api.sedoo.fr/catalogue-mistrals-prod/cache/refresh";
			
			//Project[] projects = restTemplate.getForObject(url, Project[].class);
			
			ResponseEntity<Project[]> response =   restTemplate.getForEntity(url ,Project[].class);
			Project[] projects = response.getBody();
			 
			for (Project project : projects) {
                for (int i = 0; i<project.getThesaurusItems().size(); i++) {
                	RoleProvider.createRole(project.getThesaurusItems().get(i).name.toUpperCase()+"_METADATA_EDITOR", "catalogue-mistrals-services", token);
                }
            } 
			
			ResponseEntity<?> cachedResponse =   restTemplate.getForEntity(urlCache ,Object.class);
			cachedResponse.getBody();
			
			
		}catch (Exception e) {
			log.error("An error has occured while creating clients and roles: "+ExceptionUtils.getFullStackTrace(e));
		}
	 }
}
