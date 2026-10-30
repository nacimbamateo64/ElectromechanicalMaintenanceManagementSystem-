package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import static ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ConsoleOutput.show;

import java.time.LocalDate;
import java.util.Locale;


public class EquipmentActions {
    private final SystemState state;
    private final ConsoleInput input;

    public EquipmentActions(SystemState state, ConsoleInput input) {
        this.state = state;
        this.input = input;
    }

    public void register() {
        if (state.clients.isEmpty()) {
            System.out.println("Register a client first.");
            return;
        }
        show(state.clients);
        Client client = state.findClient(input.readInt("Client ID: "));
        if (client == null) {
            System.out.println("Client not found.");
            return;
        }
        String name = input.readName("Equipment name: ");
        String serial = input.readText("Serial number: ");
        String location = input.readText("Location: ");
        Equipment equipment = new Equipment(state.equipments.size() + 1, name, serial, location, "Available");
        state.equipments.add(equipment);
        client.addEquipment(equipment);
        QrCode qr = equipment.createQrCode();
        state.qrCodes.add(qr);
        System.out.println(equipment);
        System.out.println(qr);
    }

    public void recordHistory() {
        show(state.equipments);
        Equipment equipment = state.findEquipment(input.readInt("Equipment ID: "));
        if (equipment == null) {
            System.out.println("Equipment not found.");
            return;
        }
        String type = input.readText("Maintenance type: ");
        String description = input.readText("Description: ");
        MaintenanceHistory history = new MaintenanceHistory(state.histories.size() + 1,
                LocalDate.now().toString(), type, description);
        state.histories.add(history);
        equipment.attachHistory(history);
        System.out.println(history);
    }

    public void update() {
        show(state.equipments);
        Equipment equipment = state.findEquipment(input.readPositiveInt("Equipment ID: "));
        if (equipment == null) {
            System.out.println("Equipment not found.");
            return;
        }
        equipment.setName(input.readName("New equipment name: "));
        equipment.setSerialNumber(input.readText("New serial number: "));
        equipment.setLocation(input.readText("New location: "));
        equipment.setStatus(input.readText("New status: "));
        System.out.println("Equipment updated: " + equipment);
    }

    public void showCompleteHistory() {
        show(state.equipments);
        Equipment equipment = state.findEquipment(input.readPositiveInt("Equipment ID: "));
        if (equipment == null) {
            System.out.println("Equipment not found.");
            return;
        }
        System.out.println("History recorded directly for equipment:");
        show(equipment.getMaintenanceHistories());
        System.out.println("Scheduled maintenance:");
        show(equipment.getMaintenanceSchedules());
        System.out.println("Work orders:");
        for (WorkOrder order : state.orders) {
            if (mentionsEquipment(order, equipment)) {
                System.out.println(order);
            }
        }
    }

    public void consultByQrOrSerial() {
        String code = input.readText("Scan/paste QR payload, QR ID, or equipment serial number: ");
        Equipment found = findByQrOrSerial(code);
        if (found == null) {
            System.out.println("No equipment matches that QR payload or serial number.");
            return;
        }
        System.out.println("Equipment information: " + found);
        for (Client client : state.clients) {
            for (Equipment owned : client.getEquipmentList()) {
                if (owned.getEquipmentId() == found.getEquipmentId()) {
                    System.out.println("Client: " + client);
                }
            }
        }
        System.out.println("QR payload: " + found.createQrCode().getGenerateUrl());
    }

    private boolean mentionsEquipment(WorkOrder order, Equipment equipment) {
        String description = order.getDescription().toLowerCase(Locale.ROOT);
        return description.contains(equipment.getName().toLowerCase(Locale.ROOT))
                || description.contains("equipment id: " + equipment.getEquipmentId());
    }

    private Equipment findByQrOrSerial(String code) {
        for (Equipment equipment : state.equipments) {
            QrCode qr = equipment.createQrCode();
            if (code.equalsIgnoreCase(String.valueOf(qr.getQrId()))
                    || code.equalsIgnoreCase(qr.getCodeData())
                    || code.equalsIgnoreCase(qr.getGenerateUrl())
                    || code.equalsIgnoreCase(equipment.getSerialNumber())) {
                return equipment;
            }
        }
        return null;
    }
}
