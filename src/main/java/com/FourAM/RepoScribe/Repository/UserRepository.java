package com.FourAM.RepoScribe.Repository;

import com.FourAM.RepoScribe.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {


}
