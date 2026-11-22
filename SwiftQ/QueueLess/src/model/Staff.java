package model; public class Staff extends User { private static final long serialVersionUID=1L; private int counterId;
 public Staff(String id,String name,String password,int counterId){super(id,name,password);this.counterId=counterId;}
 public int getCounterId(){return counterId;} public void setCounterId(int id){counterId=id;}
 public void displayDetails(){System.out.println("Staff ID: "+id+" | Name: "+name+" | Counter: "+counterId);} }
