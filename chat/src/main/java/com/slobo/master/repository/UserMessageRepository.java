package com.slobo.master.repository;

import com.slobo.master.model.ProcessedUserMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserMessageRepository extends MongoRepository<ProcessedUserMessage, String> {

}