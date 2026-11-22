package service; import model.*; import exceptions.*;
public class AuthenticationService {
 public void registerStudent(Student s)throws DuplicateStudentException{if(DataStore.studentIds.contains(s.getId().toLowerCase()))throw new DuplicateStudentException("Student ID already registered.");DataStore.studentIds.add(s.getId().toLowerCase());DataStore.students.add(s);DataStore.save();}
 public Student studentLogin(String id,String p)throws InvalidLoginException{for(Student s:DataStore.students)if(s.getId().equalsIgnoreCase(id)&&s.getPassword().equals(p))return s;throw new InvalidLoginException("Invalid student ID or password.");}
 public Staff staffLogin(String id,String p)throws InvalidLoginException{for(Staff s:DataStore.staffMembers)if(s.getId().equalsIgnoreCase(id)&&s.getPassword().equals(p))return s;throw new InvalidLoginException("Invalid staff ID or password.");}
 public Admin adminLogin(String id,String p)throws InvalidLoginException{for(Admin a:DataStore.admins)if(a.getId().equals(id)&&a.getPassword().equals(p))return a;throw new InvalidLoginException("Invalid admin ID or password.");}
}
