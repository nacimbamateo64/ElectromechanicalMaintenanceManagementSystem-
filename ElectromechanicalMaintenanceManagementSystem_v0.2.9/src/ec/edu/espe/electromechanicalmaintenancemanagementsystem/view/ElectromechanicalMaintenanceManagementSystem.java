package ec.edu.espe.electromechanicalmaintenancemanagementsystem.view;

import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ClientActions;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ConsoleInput;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.EquipmentActions;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.JsonDataStore;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.RecordActions;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.SystemState;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.TechnicianActions;
import ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.WorkOrderActions;
import java.util.Scanner;

public class ElectromechanicalMaintenanceManagementSystem {

    public static void main(String[] args) {
        JsonDataStore dataStore = new JsonDataStore("data");
        SystemState state = dataStore.load();
        ConsoleInput input = new ConsoleInput(new Scanner(System.in));

        ClientActions clients = new ClientActions(state, input);
        EquipmentActions equipments = new EquipmentActions(state, input);
        TechnicianActions technicians = new TechnicianActions(state, input);
        WorkOrderActions workOrders = new WorkOrderActions(state, input);
        RecordActions records = new RecordActions(state);

        int option;
        do {
            System.out.println("\n===== ELECTROMECHANICAL MAINTENANCE MANAGEMENT SYSTEM =====");
            System.out.println("1. Register client");
            System.out.println("2. Register equipment for a client");
            System.out.println("3. Register technician");
            System.out.println("4. Schedule maintenance");
            System.out.println("5. Create work order");
            System.out.println("6. Assign technician to work order");
            System.out.println("7. Add task to work order");
            System.out.println("8. Register part and safety regulation");
            System.out.println("9. Execute task");
            System.out.println("10. Close work order and generate report");
            System.out.println("11. Record maintenance history");
            System.out.println("12. Show records");
            System.out.println("13. Remove equipment from client");
            System.out.println("14. Generate CSV Report (Excel)");
            System.out.println("15. Edit or delete a client");
            System.out.println("17. Search clients and equipment");
            System.out.println("18. Update equipment information");
            System.out.println("19. Search complete equipment maintenance history");
            System.out.println("20. Consult equipment by QR code or serial number");
            System.out.println("21. Show upcoming maintenance alerts");
            System.out.println("22. Update technician availability");
            System.out.println("23. Update client contact and address");
            System.out.println("24. Exit");

            option = input.readInt("Choose an option: ");

            switch (option) {
                case 1: clients.register(); break;
                case 2: equipments.register(); break;
                case 3: technicians.register(); break;
                case 4: workOrders.scheduleMaintenance(); break;
                case 5: workOrders.createOrder(); break;
                case 6: workOrders.assignTechnician(); break;
                case 7: workOrders.addTask(); break;
                case 8: workOrders.registerPartAndRegulation(); break;
                case 9: workOrders.executeTask(); break;
                case 10: workOrders.closeOrder(); break;
                case 11: equipments.recordHistory(); break;
                case 12: records.showAll(); break;
                case 13: clients.removeEquipment(); break;
                case 14: records.exportCsv(); break;
                case 15: clients.editOrDelete(); break;
                case 17: clients.search(); break;
                case 18: equipments.update(); break;
                case 19: equipments.showCompleteHistory(); break;
                case 20: equipments.consultByQrOrSerial(); break;
                case 21: workOrders.showUpcomingAlerts(); break;
                case 22: technicians.updateAvailability(); break;
                case 23: clients.updateContact(); break;
                case 24: System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid option.");
            }

            dataStore.save(state);
        } while (option != 24 && option != 16);
    }
}
