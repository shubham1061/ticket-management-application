package com.shubham.csa.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.shubham.csa.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User,String>{
Optional<User> findByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByActiveTrue();
    
    List<User> findByRoleAndActiveTrue(User.Role role);
}
