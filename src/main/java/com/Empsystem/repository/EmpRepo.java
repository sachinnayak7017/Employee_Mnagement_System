package com.Empsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Empsystem.entity.Employee;


@Repository
public interface EmpRepo extends JpaRepository<Employee, Integer>  {

	///////filter by string 
	@Query(value="select * from emp_system e where e.name like %:keyword% or e.address like %:keyword% " , nativeQuery=true)
	List<Employee> findBykeyword(@Param("keyword") String keyword);

	
	/// filter by number
	@Query(value="select * from emp_system e where e.id like %:id% " , nativeQuery=true)
	List<Employee> findByid(@Param("id") int id);
}
