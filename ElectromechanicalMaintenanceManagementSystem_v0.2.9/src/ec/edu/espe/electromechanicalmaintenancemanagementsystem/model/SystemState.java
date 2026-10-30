package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.util.ArrayList;


public class SystemState {
    public ArrayList<Client> clients = new ArrayList<>();
    public ArrayList<Equipment> equipments = new ArrayList<>();
    public ArrayList<Technician> technicians = new ArrayList<>();
    public ArrayList<WorkOrder> orders = new ArrayList<>();
    public ArrayList<MaintenanceSchedule> schedules = new ArrayList<>();
    public ArrayList<MaintenanceHistory> histories = new ArrayList<>();
    public ArrayList<Part> parts = new ArrayList<>();
    public ArrayList<SafetyRegulation> regulations = new ArrayList<>();
    public ArrayList<Alert> alerts = new ArrayList<>();
    public ArrayList<Report> reports = new ArrayList<>();
    public ArrayList<QrCode> qrCodes = new ArrayList<>();

    public Client findClient(int id) {
        for (Client client : clients) {
            if (client.getClientId() == id) return client;
        }
        return null;
    }

    public Equipment findEquipment(int id) {
        for (Equipment equipment : equipments) {
            if (equipment.getEquipmentId() == id) return equipment;
        }
        return null;
    }

    public Technician findTechnician(int id) {
        for (Technician technician : technicians) {
            if (technician.getTechnicianId() == id) return technician;
        }
        return null;
    }

    public WorkOrder findOrder(int id) {
        for (WorkOrder order : orders) {
            if (order.getWorkOrderId() == id) return order;
        }
        return null;
    }

    public int nextClientId() {
        int next = 1;
        for (Client client : clients) {
            if (client.getClientId() >= next) next = client.getClientId() + 1;
        }
        return next;
    }

    public int nextReportId() {
        int max = 0;
        for (Report report : reports) max = Math.max(max, report.getReportId());
        return max + 1;
    }
}
