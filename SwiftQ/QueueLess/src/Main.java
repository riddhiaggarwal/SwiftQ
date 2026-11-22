import java.util.*;
import model.*;
import service.*;
import exceptions.*;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final AuthenticationService auth = new AuthenticationService();
    private static final QueueService queues = new QueueService();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("       SwiftQ - SMART CAMPUS SYSTEM");
        System.out.println("==============================================");
        System.out.println("Default admin: admin / admin123");
        System.out.println("Demo staff: S101, S102, S103 / staff123");
        System.out.println("Data is automatically saved in queueless-data.ser");
        while (true) {
            System.out.println("\nMAIN MENU\n1. Student Registration\n2. Student Login\n3. Staff Login\n4. Admin Login\n5. Exit");
            switch (number("Enter choice: ")) {
                case 1 -> registerStudent();
                case 2 -> studentLogin();
                case 3 -> staffLogin();
                case 4 -> adminLogin();
                case 5 -> { DataStore.save(); System.out.println("Data saved. Goodbye!"); return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static int number(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid number."); }
        }
    }
    private static String ask(String prompt) { System.out.print(prompt); return sc.nextLine().trim(); }

    private static void registerStudent() {
        System.out.println("\n--- STUDENT REGISTRATION ---");
        String id=ask("Student ID: "), name=ask("Name: "), password=ask("Create password: "), dept=ask("Department: ");
        if (id.isBlank() || name.isBlank() || password.isBlank() || dept.isBlank()) { System.out.println("All fields are required."); return; }
        try { auth.registerStudent(new Student(id,name,password,dept)); System.out.println("Registration successful. You can now log in."); }
        catch (DuplicateStudentException e) { System.out.println("ERROR: "+e.getMessage()); }
    }
    private static void studentLogin() {
        try { studentMenu(auth.studentLogin(ask("Student ID: "),ask("Password: "))); }
        catch (InvalidLoginException e) { System.out.println("ERROR: "+e.getMessage()); }
    }
    private static void staffLogin() {
        try { staffMenu(auth.staffLogin(ask("Staff ID: "),ask("Password: "))); }
        catch (InvalidLoginException e) { System.out.println("ERROR: "+e.getMessage()); }
    }
    private static void adminLogin() {
        try { adminMenu(auth.adminLogin(ask("Admin username: "),ask("Password: "))); }
        catch (InvalidLoginException e) { System.out.println("ERROR: "+e.getMessage()); }
    }

    private static void studentMenu(Student student) {
        while (true) {
            System.out.println("\n--- STUDENT PORTAL: "+student.getName()+" ---");
            System.out.println("1. View services\n2. Generate token\n3. Check token position / waiting time\n4. Cancel waiting token\n5. View token history\n6. View counters\n7. Logout");
            switch (number("Enter choice: ")) {
                case 1 -> showServices();
                case 2 -> {
                    showServices(); int sid=number("Select service ID: ");
                    if (DataStore.findService(sid)==null) { System.out.println("Invalid service."); break; }
                    boolean priority=ask("Priority appointment? (y/n): ").equalsIgnoreCase("y");
                    Token token=queues.generateToken(student.getId(),sid,priority);
                    if (token==null) System.out.println("Could not generate token. You may already have an active token for this service.");
                    else { System.out.println("Token generated successfully:"); showToken(token); System.out.println("Queue position: "+queues.getPosition(token.getTokenId())); System.out.println("Estimated wait: "+queues.getWaitingTime(token.getTokenId())+" minutes"); }
                }
                case 3 -> {
                    try { Token t=queues.findToken(number("Token ID: ")); if(!t.getStudentId().equalsIgnoreCase(student.getId())) { System.out.println("That token does not belong to your account."); break; }
                        showToken(t); if(t.getStatus().equals("Waiting")) System.out.println("Position: "+queues.getPosition(t.getTokenId())+" | Estimated wait: "+queues.getWaitingTime(t.getTokenId())+" minutes");
                    } catch(TokenNotFoundException e) { System.out.println(e.getMessage()); }
                }
                case 4 -> {
                    try { int id=number("Token ID to cancel: "); Token t=queues.findToken(id); if(!t.getStudentId().equalsIgnoreCase(student.getId())) { System.out.println("You can cancel only your own token."); break; } queues.cancelToken(id); System.out.println("Token cancelled."); }
                    catch(Exception e) { System.out.println("ERROR: "+e.getMessage()); }
                }
                case 5 -> { List<Token> history=queues.getStudentTokens(student.getId()); if(history.isEmpty())System.out.println("No token history."); for(Token t:history)showToken(t); }
                case 6 -> showCounters();
                case 7 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void staffMenu(Staff staff) {
        while (true) {
            Counter counter=DataStore.findCounter(staff.getCounterId());
            System.out.println("\n--- STAFF PORTAL: "+staff.getName()+" ---");
            System.out.println("Assigned Counter: "+staff.getCounterId());
            System.out.println("1. View assigned queue\n2. Call next token\n3. Start serving called token\n4. Complete serving token\n5. Skip called token (student absent)\n6. Logout");
            int choice=number("Enter choice: "); if(choice==6)return;
            if(counter==null || !counter.isActive()) { System.out.println("Assigned counter unavailable."); continue; }
            switch(choice) {
                case 1 -> showQueue(counter.getServiceId());
                case 2 -> { Token t=queues.callNextToken(counter.getServiceId()); if(t==null)System.out.println("No waiting token, or another token is already being served."); else {System.out.println("Now calling:");showToken(t);} }
                case 3 -> { Token t=findLatest(counter.getServiceId(),"Called"); if(t!=null&&queues.startServing(t))System.out.println("Token "+t.getTokenId()+" is now being served."); else System.out.println("No called token found."); }
                case 4 -> { Token t=findLatest(counter.getServiceId(),"Serving"); if(t!=null&&queues.completeToken(t))System.out.println("Token "+t.getTokenId()+" completed."); else System.out.println("No token is being served."); }
                case 5 -> { Token t=findLatest(counter.getServiceId(),"Called"); if(t!=null&&queues.skipToken(t))System.out.println("Token skipped."); else System.out.println("No called token to skip."); }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static Token findLatest(int serviceId,String status) {
        for(int i=DataStore.tokenHistory.size()-1;i>=0;i--){Token t=DataStore.tokenHistory.get(i);if(t.getServiceId()==serviceId&&t.getStatus().equals(status))return t;} return null;
    }

    private static void adminMenu(Admin admin) {
        while(true) {
            System.out.println("\n--- ADMIN PORTAL ---");
            System.out.println("1. View services\n2. Add service\n3. Update service\n4. View counters\n5. Activate/deactivate counter\n6. View students\n7. View all tokens\n8. Queue statistics\n9. Add staff account\n10. View staff accounts\n11. Run simple multithreading demo\n12. Change admin password\n13. Logout");
            switch(number("Enter choice: ")) {
                case 1 -> showServices(); case 2 -> addService(); case 3 -> updateService(); case 4 -> showCounters(); case 5 -> toggleCounter();
                case 6 -> {if(DataStore.students.isEmpty())System.out.println("No students registered.");for(Student s:DataStore.students)s.displayDetails();}
                case 7 -> {if(DataStore.tokenHistory.isEmpty())System.out.println("No tokens yet.");for(Token t:DataStore.tokenHistory)showToken(t);}
                case 8 -> showStatistics(); case 9 -> addStaff(); case 10 -> {if(DataStore.staffMembers.isEmpty())System.out.println("No staff accounts.");for(Staff s:DataStore.staffMembers)s.displayDetails();}
                case 11 -> ConcurrencyDemo.run();
                case 12 -> {String p=ask("New admin password (minimum 4 characters): ");if(p.length()<4)System.out.println("Password too short.");else{admin.setPassword(p);DataStore.save();System.out.println("Admin password changed.");}}
                case 13 -> {return;} default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void addStaff() {
        String name=ask("Staff name: "), password=ask("Temporary password: "); showServices(); int serviceId=number("Service ID for new counter: ");
        if(name.isBlank()||password.length()<4||DataStore.findService(serviceId)==null){System.out.println("Enter valid details and an existing service ID.");return;}
        int max=100; for(Staff s:DataStore.staffMembers){try{max=Math.max(max,Integer.parseInt(s.getId().substring(1)));}catch(Exception ignored){}}
        int counterId=0;for(Counter c:DataStore.counters)counterId=Math.max(counterId,c.getCounterId());counterId++;
        String staffId="S"+(max+1);DataStore.staffMembers.add(new Staff(staffId,name,password,counterId));DataStore.counters.add(new Counter(counterId,"Counter "+counterId,serviceId,staffId));DataStore.save();
        System.out.println("\nSTAFF ACCOUNT CREATED\nName: "+name+"\nStaff ID: "+staffId+"\nTemporary password: "+password+"\nAssigned counter: "+counterId);
    }
    private static void addService() {
        int id=number("New service ID: ");String name=ask("Service name: ");int mins=number("Average service time (minutes): ");
        if(id<=0||DataStore.findService(id)!=null||name.isBlank()||mins<=0){System.out.println("Invalid or duplicate service details.");return;}
        DataStore.services.add(new Service(id,name,mins));DataStore.serviceQueues.put(id,new PriorityQueue<>(DataStore.TOKEN_ORDER));DataStore.save();System.out.println("Service added.");
    }
    private static void updateService(){int id=number("Service ID: ");Service s=DataStore.findService(id);if(s==null){System.out.println("Service not found.");return;}String n=ask("New service name: ");int m=number("New average time: ");if(n.isBlank()||m<=0){System.out.println("Invalid values.");return;}s.setServiceName(n);s.setAverageServiceTime(m);DataStore.save();System.out.println("Service updated.");}
    private static void toggleCounter(){showCounters();Counter c=DataStore.findCounter(number("Counter ID: "));if(c==null){System.out.println("Counter not found.");return;}c.setActive(!c.isActive());DataStore.save();System.out.println("Counter is now "+(c.isActive()?"active":"inactive"));}
    private static void showStatistics(){System.out.printf("%-28s %-8s %-10s %-10s %-8s%n","Service","Waiting","Completed","Cancelled","Skipped");for(Service s:DataStore.services){int w=0,d=0,c=0,k=0;for(Token t:DataStore.tokenHistory)if(t.getServiceId()==s.getServiceId())switch(t.getStatus()){case "Waiting"->w++;case "Completed"->d++;case "Cancelled"->c++;case "Skipped"->k++;}System.out.printf("%-28s %-8d %-10d %-10d %-8d%n",s.getServiceName(),w,d,c,k);}}
    private static void showServices(){System.out.println("\nSERVICE ID  SERVICE                         AVG. TIME");for(Service s:DataStore.services)s.displayService();}
    private static void showCounters(){System.out.println("\nCOUNTER DETAILS");for(Counter c:DataStore.counters)c.displayCounter();}
    private static void showQueue(int serviceId){List<Token> q=queues.getQueueSnapshot(serviceId);System.out.println("\nTOKEN ID STUDENT      TYPE      STATUS       ARRIVAL");int p=1;for(Token t:q){System.out.print("Position "+p+++": ");t.displayToken();}if(q.isEmpty())System.out.println("Queue is empty.");}
    private static void showToken(Token t){Service s=DataStore.findService(t.getServiceId());System.out.println("Token ID: T"+t.getTokenId()+" | Student: "+t.getStudentId()+" | Service: "+(s==null?t.getServiceId():s.getServiceName())+" | Type: "+(t.isPriority()?"Priority":"Normal")+" | Status: "+t.getStatus()+" | Time: "+t.getArrivalTime());}
}
