package fr.sedoo.mistrals.rest.dao;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Component
public class DataDirectory {

	public HashMap<String, String> cache = new HashMap<>();
	
	private String dataResourcePath = "classpath:dataDirectory.txt";

	@Autowired
	private ResourceLoader rl;
	
	public String getFolderFromUuid(String uuid) {
		return cache.get(uuid);
	}

	@PostConstruct
	public void initCache() throws Exception {
		
		Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(rl)
				.getResources(dataResourcePath);
		if (resources.length == 0) {
			throw new Exception("The data file doesn't exist");
		}

		Resource resource = resources[0];
		String text = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8.name());
		String lines[] = text.split("\\r?\\n");
		for (int i = 0; i < lines.length; i++) {
			String[] split = lines[i].split(",");
			//if (split[1].startsWith("baobab")) {
				cache.put(split[0], split[1]);
			//}
		}
		
	}
	
}
