package model; public class Student extends User { 
    private static final long serialVersionUID=1L; private String department;
 public Student(String id,String name,String password,String department){
    super(id,name,password);
    this.department=department;}
 public String getDepartment(){
    return department;} 
    public void displayDetails(){
        System.out.println("Student ID: "+id+" | Name: "+name+" | Department: "+department);
    } 
    }
