package com.dmm.task.data.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.entity.Users;



@Repository
public interface TaskRepository extends JpaRepository<Tasks, Integer> {

    @Query("SELECT t FROM Task t WHERE t.date BETWEEN :from AND :to AND t.name = :name")
    List<Tasks> findByDateBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("name") String name
    );
    
    
    Optional<Users> findByUsername(String username);
    
}
	
