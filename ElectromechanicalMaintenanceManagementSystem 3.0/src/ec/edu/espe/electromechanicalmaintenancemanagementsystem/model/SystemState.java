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
}
