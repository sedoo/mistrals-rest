package fr.sedoo.mistrals.rest.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class ApplicationConfig {
	
	@Value("${mistrals.data}")
	private String mistralsFolderName;

	@Value("${application.folder}/tmp")
	private String temporaryDownloadFolderName;
	

	
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
