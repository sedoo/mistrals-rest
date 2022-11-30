package fr.sedoo.mistrals.rest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.mistrals.rest.domain.User;

@Component
public class UserDAOFakeImpl implements UserDAO{

	@Override
	public List<User> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User findByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User upsert(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validateRegistration(String email) {
		// TODO Auto-generated method stub
		
	}
	
	
}
