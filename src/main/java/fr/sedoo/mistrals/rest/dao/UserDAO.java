package fr.sedoo.mistrals.rest.dao;

import java.util.List;

import fr.sedoo.mistrals.rest.domain.User;

public interface UserDAO {

	List<User> findAll();
	User findByEmail(String email);
	User upsert(User user);
	void validateRegistration(String email);
}
