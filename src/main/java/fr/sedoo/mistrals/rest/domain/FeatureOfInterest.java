package fr.sedoo.mistrals.rest.domain;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeatureOfInterest {
	String name;
	List<String> parameters;
	Set<Integer> years;
}
