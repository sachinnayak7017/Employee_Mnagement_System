package com.Empsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Empsystem.entity.Employee;
import com.Empsystem.repository.EmpRepo;

@Service
public class EmpService {

	@Autowired
	private EmpRepo repo;

	public void addEmp(Employee e) {

		repo.save(e);
	}
//	public void addEmp(Employee e ,byte[] imageBytes) {
//		e.setImageData(imageBytes);
//		repo.save(e);
//	}
	
	
	
	
	public List<Employee> getAllEmp() {

		return repo.findAll();
	}

	public Employee getEmpById(int id) {

		Optional<Employee> e = repo.findById(id);
		if (e.isPresent()) {
			return e.get();
		}
		return null;

	}

	public void deleteEMp(Integer id) {
		repo.deleteById(id);
	}

	/// get employees by keyword filter by string
	public List<Employee> findByKeyword(String keyword) {
		return repo.findBykeyword(keyword);
	}

//filter by number
	public List<Employee> findByid(int id) {

		return repo.findByid(id);
	}

	/// upload excel file
	public void save(Employee entity) {
		// TODO Auto-generated method stub

	}

}
