package com.dogginer.dog.repository;

import com.dogginer.dog.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEventRepository extends JpaRepository<Event, Integer> {
}
