package model; import java.io.Serializable;
public abstract class User implements Serializable {
     private static final long serialVersionUID=1L; 
     protected String id,name,password;
 public User(String id,String name,String password){
    this.id=id;
    this.name=name;
    this.password=password;
}
 public String getId(){
    return id;
} 
public String getName(){
    return name;
} 
public String getPassword(){
    return password;
} 
public void setPassword(String p){
    password=p;
} 
public abstract void displayDetails(); 
}
