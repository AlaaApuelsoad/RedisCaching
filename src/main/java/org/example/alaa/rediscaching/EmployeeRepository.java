package org.example.alaa.rediscaching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(value = "SELECT * FROM employee WHERE LOWER(name) LIKE LOWER(Concat('%',:name,'%'))",nativeQuery = true)
    List<Employee> findEmployeeByName(@Param("name") String name);
}
