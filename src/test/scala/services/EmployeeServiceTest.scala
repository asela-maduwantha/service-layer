package services

import models.Employee
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import repositories.EmployeeRepository

import scala.concurrent.Future

class EmployeeServiceTest extends AnyFunSuite with MockitoSugar with ScalaFutures with Matchers {

  val mockEmployeeRepository: EmployeeRepository = mock[EmployeeRepository]
  val employeeService = new EmployeeService(mockEmployeeRepository)
  val testEmployee: Employee = Employee(None,"John Doe", "john.doe@example.com", "colombo", 2000.0)

  test("EmployeeService.addEmployee should return employee ID") {
    when(mockEmployeeRepository.create(testEmployee)).thenReturn(Future.successful(1))

    val result = employeeService.addEmployee(testEmployee)

    whenReady(result) { id =>
      id shouldBe 1
    }
  }

  test("EmployeeService.getEmployeeById should return an employee if exists") {
    when(mockEmployeeRepository.getById(1)).thenReturn(Future.successful(Some(testEmployee)))

    val result = employeeService.getEmployeeById(1)

    whenReady(result) { employee =>
      employee shouldBe Some(testEmployee)
    }
  }

  test("EmployeeService.updateEmployee should return 1 on success") {
    when(mockEmployeeRepository.update(testEmployee)).thenReturn(Future.successful(1))

    val result = employeeService.updateEmployee(testEmployee)

    whenReady(result) { updateCount =>
      updateCount shouldBe 1
    }
  }

  test("EmployeeService.deleteEmployeeById should return 1 on success") {
    when(mockEmployeeRepository.delete(1)).thenReturn(Future.successful(1))

    val result = employeeService.deleteEmployeeById(1)

    whenReady(result) { deleteCount =>
      deleteCount shouldBe 1
    }
  }

  test("EmployeeService.addEmployee should handle repository failure") {
    when(mockEmployeeRepository.create(testEmployee)).thenReturn(Future.failed(new Exception("DB error")))

    val result = employeeService.addEmployee(testEmployee)

    whenReady(result.failed) { ex =>
      ex shouldBe a[Exception]
      ex.getMessage should include("DB error")
    }
  }

  test("EmployeeService.getEmployeeById should return None if employee does not exist") {
    when(mockEmployeeRepository.getById(1)).thenReturn(Future.successful(None))

    val result = employeeService.getEmployeeById(1)

    whenReady(result) { employee =>
      employee shouldBe None
    }
  }

  test("EmployeeService.updateEmployee should return 0 if employee does not exist") {
    when(mockEmployeeRepository.update(testEmployee)).thenReturn(Future.successful(0))

    val result = employeeService.updateEmployee(testEmployee)

    whenReady(result) { updateCount =>
      updateCount shouldBe 0
    }
  }

  test("EmployeeService.deleteEmployeeById should return 0 if employee does not exist") {
    when(mockEmployeeRepository.delete(1)).thenReturn(Future.successful(0))

    val result = employeeService.deleteEmployeeById(1)

    whenReady(result) { deleteCount =>
      deleteCount shouldBe 0
    }
  }

  test("EmployeeService should handle exceptions from repository methods") {
    when(mockEmployeeRepository.getById(1)).thenReturn(Future.failed(new Exception("DB failure")))

    val result = employeeService.getEmployeeById(1)

    whenReady(result.failed) { ex =>
      ex shouldBe a[Exception]
      ex.getMessage should include("DB failure")
    }
  }

}



