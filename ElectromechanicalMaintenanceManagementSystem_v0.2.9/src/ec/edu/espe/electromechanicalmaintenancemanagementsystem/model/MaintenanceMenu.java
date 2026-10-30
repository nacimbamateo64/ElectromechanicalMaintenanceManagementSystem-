package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.util.Scanner;


public class MaintenanceMenu {
    private static final int EXIT_OPTION = 24;
    private static final int LEGACY_EXIT_OPTION = 16;

    private static final String[] OPTIONS = {
        "1. Register client",
        "2. Register equipment for a client",
        "3. Register technician",
        "4. Schedule maintenance",
        "5. Create work order",
        "6. Assign technician to work order",
        "7. Add task to work order",
        "8. Register part and safety regulation",
        "9. Execute task",
        "10. Close work order and generate report",
        "11. Record maintenance history",
        "12. Show records",
        "13. Remove equipment from client",
        "14. Generate CSV Report (Excel)",
        "15. Edit or delete a client",
        "17. Search clients and equipment",
        "18. Update equipment information",
        "19. Search complete equipment maintenance history",
        "20. Consult equipment by QR code or serial number",
        "21. Show upcoming maintenance alerts",
        "22. Update technician availability",
        "23. Update client contact and address",
        "24. Exit"
    };

    private final JsonDataStore dataStore;
    private final SystemState state;
    private final ConsoleInput input;
    private final ClientActions clients;
    private final EquipmentActions equipments;
    private final TechnicianActions technicians;
    private final WorkOrderActions workOrders;
    private final RecordActions records;

    public MaintenanceMenu(JsonDataStore dataStore, Scanner scanner) {
        this.dataStore = dataStore;
        this.state = dataStore.load();
        this.input = new ConsoleInput(scanner);
        this.clients = new ClientActions(state, input);
        this.equipments = new EquipmentActions(state, input);
        this.technicians = new TechnicianActions(state, input);
        this.workOrders = new WorkOrderActions(state, input);
        this.records = new RecordActions(state);
    }

    public void run() {
        int option;
        do {
            printMenu();
            option = input.readInt("Choose an option: ");
            execute(option);
            dataStore.save(state);
        } while (option != LEGACY_EXIT_OPTION && option != EXIT_OPTION);
    }

    private void printMenu() {
        System.out.println("\n===== ELECTROMECHANICAL MAINTENANCE MANAGEMENT SYSTEM =====");
        for (String option : OPTIONS) {
            System.out.println(option);
        }
    }

    private void execute(int option) {
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
            case EXIT_OPTION: System.out.println("Goodbye!"); break;
            default: System.out.println("Invalid option.");
        }
    }
}
