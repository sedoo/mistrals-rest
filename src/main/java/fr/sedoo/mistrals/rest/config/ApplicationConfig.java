package fr.sedoo.mistrals.rest.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Component
public class ApplicationConfig {
	
	@Value("${mistrals.data}")
	private String mistralsFolderName;

	@Value("${local.baseFolder}")
	private String temporaryDownloadFolderName;
	
	@Value("${sso.login}")
	private String login;
	
	@Value("${sso.password}")
	private String password;
	
	@Value("${mail.hostname}")
	private String hostname;
	
	@Value("${mail.subjectPrefix}")
	private String subjectPrefix;
	
	@Value("${mail.from}")
	private String from;
	

	
	public File getMistralsRootFolder() {
		File result = new File(mistralsFolderName);
		if (!result.exists()) {
			result.mkdirs();
		}
		return result;
	}
	
	
	public File getTemporaryDownloadFolder() {
		File result = new File(temporaryDownloadFolderName);
		if (!result.exists()) {
			result.mkdirs();
		}
		return result;
	}
	

	

	

}
