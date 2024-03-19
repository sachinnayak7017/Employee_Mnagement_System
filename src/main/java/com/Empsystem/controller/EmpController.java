package com.Empsystem.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Empsystem.entity.Employee;

import com.Empsystem.repository.EmpRepo;

import com.Empsystem.service.EmpService;

import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class EmpController {

	@Autowired
	private EmpService service;
	@Autowired
	private EmpRepo repo;

	//// for filter by string
	@GetMapping("/employees")
	public String getEmp(Model m, String keyword) {
		List<Employee> emp = service.getAllEmp();
		if (keyword != null) {
			m.addAttribute("emp", service.findByKeyword(keyword));
		} else {
			m.addAttribute("emp", emp);
		}
		return "index";
	}

	//////// filter by number

	@GetMapping("/employees1")
	public String getEmpid(Model m, int id) {
		List<Employee> emp = service.getAllEmp();
		if (id <= 0) {
			m.addAttribute("emp", emp);
		} else {
			m.addAttribute("emp", service.findByid(id));
		}
		return "index";
	}

	@GetMapping("/")
	public String index(Model m) {
		List<Employee> emp = service.getAllEmp();
		m.addAttribute("emp", emp);
		return "index";
	}

	//// show image in html table
	@GetMapping("/employee-image/{id}")
	public ResponseEntity<byte[]> getEmployeeImage(@PathVariable int id) {
		Employee employee = service.getEmpById(id);

		if (employee != null && employee.getImageData() != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG); // Adjust content type as needed
		
			return new ResponseEntity<>(employee.getImageData(), headers, HttpStatus.OK);
		}

		// Return a placeholder image or an error image if needed
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/add_emp")
	public String addemp() {
		return "add_emp";
	}

	// for image
//	ALTER TABLE EMP_SYSTEM
//	MODIFY COLUMN image_data LONGBLOB; --
	@PostMapping("/register")
	public String empRegister(@ModelAttribute Employee e, HttpSession session,
			@RequestParam("file") MultipartFile file) {

		try {
			if (!file.isEmpty()) {

				long MAX_FILE_SIZE = 10 * 1024 * 1024;
				if (file.getSize() <= MAX_FILE_SIZE) {

					byte[] imageData = file.getBytes();
					e.setImageData(imageData);

					session.setAttribute("msg", "Employee Added Sucessfully..");
				} else {
					session.setAttribute("msg", "Employee NOT Added Sucessfully..");
				}

			}
			service.addEmp(e);
		} catch (IOException e1) {
			// Handle the exception
		}
		return "redirect:/";
	}

	@GetMapping("/editEmp/{id}")
	public String edit(@PathVariable int id, Model m) {

		Employee e = service.getEmpById(id);

		m.addAttribute("emp", e);
		return "editEmp";
	}

	@PostMapping("/update/{id}")
	public String updateEmp(@ModelAttribute Employee e, HttpSession session, @RequestParam("file") MultipartFile file) {
		try {

			if (!file.isEmpty()) {
				long MAX_FILE_SIZE = 10 * 1024 * 1024;
				if (file.getSize() <= MAX_FILE_SIZE) {
					byte[] imageData = file.getBytes();
					e.setImageData(imageData);
				}
			}

			else {
				session.setAttribute("msg", "Employee NOT Added Sucessfully..");
			}

			service.addEmp(e);
		} catch (IOException e1) {
			// Handle the exception
		}
		return "redirect:/";
	}
	/// delete multipel
//	 @PostMapping("/deletes")
//	    public String deleteStudents(@RequestParam(name = "employeeIds") List<Integer> employeeIds) {
//	        repo.deleteAllById(employeeIds);
//	        return "redirect:/";
//	    }
	  @PostMapping("/deletes")
	    public String deleteStudents(@RequestParam(name = "employeeIds", required = false) List<Integer> employeeIds,
	                                 @RequestParam(name = "deleteAll", required = false) boolean deleteAll) {
	        if (deleteAll) {
	            // Delete all students
	            repo.deleteAll();
	        } else if (employeeIds != null && !employeeIds.isEmpty()) {
	            // Delete selected students
	            repo.deleteAllById(employeeIds);
	        }
	        return "redirect:";
	    }
	
	

	@GetMapping("/delete/{id}")
	public String deleteEMp(@PathVariable int id, HttpSession session) {

		service.deleteEMp(id);
		session.setAttribute("msg", "emp data delete sucessfully ...");
		return "redirect:/";
	}

	//////////////// import data from excel to data base

	@PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile file) {
		try {

			if (file.isEmpty()) {
				return "redirect:/?error=Empty file";
			}
			DataFormatter dataFormatter = new DataFormatter();

			Workbook workbook = new XSSFWorkbook(file.getInputStream());
			org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext()) {
				rowIterator.next();
			}

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Employee entity = new Employee();
				entity.setName(row.getCell(0).getStringCellValue());
				entity.setAddress(row.getCell(1).getStringCellValue());
				entity.setEmail(row.getCell(2).getStringCellValue());
				entity.setPhone(dataFormatter.formatCellValue(row.getCell(3)));

				Cell salaryCell = row.getCell(4);
				if (salaryCell.getCellType() == CellType.STRING) {

					try {
						entity.setSalary(Integer.parseInt(salaryCell.getStringCellValue()));
					} catch (NumberFormatException e) {

						entity.setSalary(0);
					}
				} else if (salaryCell.getCellType() == CellType.NUMERIC) {

					entity.setSalary((int) salaryCell.getNumericCellValue());
				} else {

					entity.setSalary(0);
				}

				entity.setImageData(null);
				repo.save(entity);
				System.out.println(entity);
			}

			workbook.close();
			return "redirect:/?success";
		} catch (Exception e) {
			return "redirect:/?error=Error uploading file: " + e.getMessage();
		}
	}

	/// generate formeta excel file
	@GetMapping("/generate-template")
	public void generateTemplate(HttpServletResponse response) throws IOException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=EMP_SYSTEM.xlsx");

		Workbook workbook = new XSSFWorkbook();
		org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Data Template");

		// Create header row
		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("name");
		header.createCell(1).setCellValue("address");
		header.createCell(2).setCellValue("email");
		header.createCell(3).setCellValue("phone");
		header.createCell(4).setCellValue("salary");

		workbook.write(response.getOutputStream());
		workbook.close();
	}

	@RequestMapping("/Show_Employee/{id}")
	public String openShowStudentPersonal(@PathVariable("id") Integer id, Model m) {

		m.addAttribute("service", new Employee());

		Optional<Employee> stpOptional = this.repo.findById(id);
		Employee emp = stpOptional.get();

		m.addAttribute("emp", emp);

		return "Show_Employee";
	}
////seconde way  of import data excel to database
//@PostMapping("/upload")
//public String uploadFile(@RequestParam("file") MultipartFile file) {
//    if (file.isEmpty()) {
//        return "redirect:/?error=File is empty";
//    }
//    DataFormatter dataFormatter = new DataFormatter();
//    try (InputStream in = file.getInputStream()) {
//        Workbook workbook = new XSSFWorkbook(in);
////        Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
//        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
//        
//        for (Row row : sheet) {
//            Employee entity = new Employee();
//
////            entity.setName(row.getCell(0).getStringCellValue());
////            entity.setAddress(row.getCell(1).getStringCellValue());
////            entity.setEmail(row.getCell(2).getStringCellValue());
////            entity.setPhone(row.getCell(3).getStringCellValue());
//	   entity.setSalary((int) row.getCell(4).getNumericCellValue());
	/////////////////////////////////////////////

//            entity.setName(dataFormatter.formatCellValue(row.getCell(0)));
//            entity.setAddress(dataFormatter.formatCellValue(row.getCell(1)));
//            entity.setEmail(dataFormatter.formatCellValue(row.getCell(2)));
//            entity.setPhone(dataFormatter.formatCellValue(row.getCell(3)));
//              entity.setSalary((int) row.getCell(4).getNumericCellValue());
////////////////////////////////////////////
////            // Handle the salary cell (column 4)
////            Cell salaryCell = row.getCell(4);
////            if (salaryCell.getCellType() == CellType.STRING) {
////                // Handle it as a string
////                entity.setSalary(Integer.parseInt(salaryCell.getStringCellValue()));
////            } else if (salaryCell.getCellType() == CellType.NUMERIC) {
////                // Handle it as a numeric value
////                entity.setSalary((int) salaryCell.getNumericCellValue());
////            } else {
////                // Handle other cases or set a default value
////                entity.setSalary(0); // You can set a default value or handle other cases accordingly
////            }
//            ///////////////////////////////////////////////
//            Cell salaryCell = row.getCell(4);
//            if (salaryCell.getCellType() == CellType.STRING) {
//                // Check if the cell contains a numeric string
//                try {
//                    entity.setSalary(Integer.parseInt(salaryCell.getStringCellValue()));
//                } catch (NumberFormatException e) {
//                    // Handle non-numeric values
//                    entity.setSalary(0); // Set a default value or handle non-numeric data appropriately
//                }
//            } else if (salaryCell.getCellType() == CellType.NUMERIC) {
//                // Handle it as a numeric value
//                entity.setSalary((int) salaryCell.getNumericCellValue());
//            } else {
//                // Handle other cases or set a default value
//                entity.setSalary(0); // You can set a default value or handle other cases accordingly
//            }
//            /////////////////////////////////
//            
//            repo.save(entity);
//			System.out.println(entity);
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//     
//        return "redirect:/?error=Error processing the file";
//    }
//
//    return "redirect:/?success=File uploaded successfully";
//}

	///////////////////

}
