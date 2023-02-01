package fr.sedoo.mistrals.rest.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

public class TestMistrals {

	public static void main(String[] args) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> response = template.getForEntity("https://api.sedoo.fr/catalogue-mistrals-prod/metadata/summaries", String.class);
        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);
        JSONArray uuids = jsonContext.read("$.results.*.id");
        
        for (Object object : uuids) {
            response = template.getForEntity("https://api.sedoo.fr/catalogue-mistrals-prod/metadata/"+object.toString()+"?language=en&output=json", String.class);
            json = response.getBody();
            System.out.println("coucou "+json);
            if (json.indexOf("OPENSEARCH_LINK")<0) {
                System.out.println("La fiche suivante n'a pas de lien OPENSEARCH_LINK: "+object.toString());
            }
        }
    }
}
