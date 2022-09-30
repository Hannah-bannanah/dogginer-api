package com.dogginer.dog.repository;

import com.dogginer.dog.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClientRepository extends JpaRepository<Client, Integer>{
}
