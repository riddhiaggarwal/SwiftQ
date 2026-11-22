package model; 
public class Admin extends User { private static final long serialVersionUID=1L;
 public Admin(String id,String name,String password){
    super(id,name,password);
} 
public void displayDetails(){
    System.out.println("Admin ID: "+id+" | Name: "+name);} 
}
