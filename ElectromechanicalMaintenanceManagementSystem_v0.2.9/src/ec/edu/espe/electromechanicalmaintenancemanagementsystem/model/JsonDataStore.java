package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class JsonDataStore {
    public static final String CLIENTS_FILE = "clients.json";
    public static final String EQUIPMENTS_FILE = "equipments.json";
    public static final String QR_CODES_FILE = "qr_codes.json";
    public static final String TECHNICIANS_FILE = "technicians.json";
    public static final String SCHEDULES_FILE = "maintenance_schedules.json";
    public static final String WORK_ORDERS_FILE = "work_orders.json";
    public static final String TASKS_FILE = "maintenance_tasks.json";
    public static final String PARTS_FILE = "parts.json";
    public static final String REGULATIONS_FILE = "safety_regulations.json";
    public static final String HISTORIES_FILE = "maintenance_history.json";
    public static final String ALERTS_FILE = "alerts.json";
    public static final String REPORTS_FILE = "reports.json";

    private final Path directory;

    public JsonDataStore(String directory) {
        this(Path.of(directory));
    }

    public JsonDataStore(Path directory) {
        this.directory = directory;
    }

    public Path getDirectory() {
        return directory;
    }

    private JsonFileManager file(String name) {
        return new JsonFileManager(directory.resolve(name));
    }

    private List<Map<String, Object>> read(String name) {
        return file(name).readObjects();
    }

    
    public SystemState load() {
        SystemState state = new SystemState();

        Map<Integer, QrCode> qrCodesById = new HashMap<>();
        for (Map<String, Object> row : read(QR_CODES_FILE)) {
            QrCode qrCode = new QrCode(Json.integer(row, "qrId", 0),
                    Json.string(row, "codeData"), Json.string(row, "generateUrl"));
            state.qrCodes.add(qrCode);
            qrCodesById.put(qrCode.getQrId(), qrCode);
        }

        Map<Integer, MaintenanceHistory> historiesById = new HashMap<>();
        for (Map<String, Object> row : read(HISTORIES_FILE)) {
            MaintenanceHistory history = new MaintenanceHistory(Json.integer(row, "historyId", 0),
                    Json.string(row, "maintenanceDate"), Json.string(row, "maintenanceType"),
                    Json.string(row, "description"));
            state.histories.add(history);
            historiesById.put(history.getHistoryId(), history);
        }

        Map<Integer, Part> partsById = new HashMap<>();
        for (Map<String, Object> row : read(PARTS_FILE)) {
            Part part = new Part(Json.integer(row, "partId", 0), Json.string(row, "name"),
                    Json.integer(row, "stockQuantity", 0), Json.decimal(row, "unitCost", 0f),
                    Json.string(row, "supplier"));
            state.parts.add(part);
            partsById.put(part.getPartId(), part);
        }

        Map<Integer, SafetyRegulation> regulationsById = new HashMap<>();
        for (Map<String, Object> row : read(REGULATIONS_FILE)) {
            SafetyRegulation regulation = new SafetyRegulation(Json.integer(row, "regulationId", 0),
                    Json.string(row, "title"), Json.string(row, "description"), Json.string(row, "riskLevel"));
            state.regulations.add(regulation);
            regulationsById.put(regulation.getRegulationId(), regulation);
        }

        Map<Integer, Technician> techniciansById = new HashMap<>();
        for (Map<String, Object> row : read(TECHNICIANS_FILE)) {
            Technician technician = new Technician(Json.integer(row, "technicianId", 0),
                    Json.string(row, "name"), Json.string(row, "expertise"),
                    Json.bool(row, "availabilityStatus", true));
            for (Integer id : Json.integerList(row, "reviewedHistoryIds")) {
                technician.reviewHistory(historiesById.get(id));
            }
            state.technicians.add(technician);
            techniciansById.put(technician.getTechnicianId(), technician);
        }

        Map<Integer, WorkOrder> ordersById = new HashMap<>();
        for (Map<String, Object> row : read(WORK_ORDERS_FILE)) {
            WorkOrder order = new WorkOrder(Json.integer(row, "workOrderId", 0),
                    Json.string(row, "description"), Json.string(row, "priority"),
                    Json.string(row, "status"), Json.string(row, "creationDate"));
            Technician technician = techniciansById.get(Json.optionalInteger(row, "technicianId"));
            if (technician != null) {
                order.assignTechnician(technician);
            }
            state.orders.add(order);
            ordersById.put(order.getWorkOrderId(), order);
        }

        for (Map<String, Object> row : read(TASKS_FILE)) {
            MaintenanceTask task = new MaintenanceTask(Json.integer(row, "taskId", 0),
                    Json.string(row, "taskDescription"), Json.string(row, "taskType"),
                    Json.decimal(row, "estimatedDuration", 0f), Json.bool(row, "isCompleted", false));
            for (Integer id : Json.integerList(row, "partIds")) {
                task.recordPart(partsById.get(id));
            }
            for (Integer id : Json.integerList(row, "regulationIds")) {
                task.verifySafety(regulationsById.get(id));
            }
            WorkOrder order = ordersById.get(Json.optionalInteger(row, "workOrderId"));
            if (order == null) {
                System.out.println("Task " + task.getTaskId() + " was skipped: its work order was not found.");
                continue;
            }
            order.addTask(task);
        }

        for (Map<String, Object> row : read(ALERTS_FILE)) {
            Alert alert = new Alert(Json.integer(row, "alertId", 0), Json.string(row, "message"),
                    Json.string(row, "severity"), Json.bool(row, "isResolved", false));
            state.alerts.add(alert);
            WorkOrder order = ordersById.get(Json.optionalInteger(row, "workOrderId"));
            if (order != null) {
                order.addAlert(alert);
            }
        }

        for (Map<String, Object> row : read(REPORTS_FILE)) {
            Report report = new Report(Json.integer(row, "reportId", 0), Json.string(row, "reportType"),
                    Json.string(row, "generatedDate"), Json.string(row, "dataContent"));
            state.reports.add(report);
            WorkOrder order = ordersById.get(Json.optionalInteger(row, "workOrderId"));
            if (order != null) {
                order.addReport(report);
            }
        }

        Map<Integer, MaintenanceSchedule> schedulesById = new HashMap<>();
        for (Map<String, Object> row : read(SCHEDULES_FILE)) {
            MaintenanceSchedule schedule = new MaintenanceSchedule(Json.integer(row, "scheduleId", 0),
                    Json.string(row, "frequency"), Json.string(row, "scheduleDate"),
                    Json.bool(row, "isPreventive", false));
            for (Integer id : Json.integerList(row, "workOrderIds")) {
                schedule.addWorkOrder(ordersById.get(id));
            }
            state.schedules.add(schedule);
            schedulesById.put(schedule.getScheduleId(), schedule);
        }

        Map<Integer, Equipment> equipmentsById = new HashMap<>();
        for (Map<String, Object> row : read(EQUIPMENTS_FILE)) {
            Equipment equipment = new Equipment(Json.integer(row, "equipmentId", 0),
                    Json.string(row, "name"), Json.string(row, "serialNumber"),
                    Json.string(row, "location"), Json.string(row, "status"));
            equipment.attachQrCode(qrCodesById.get(Json.optionalInteger(row, "qrId")));
            for (Integer id : Json.integerList(row, "historyIds")) {
                equipment.attachHistory(historiesById.get(id));
            }
            for (Integer id : Json.integerList(row, "scheduleIds")) {
                equipment.assignSchedule(schedulesById.get(id));
            }
            for (Integer id : Json.integerList(row, "workOrderIds")) {
                equipment.attachWorkOrder(ordersById.get(id));
            }
            state.equipments.add(equipment);
            equipmentsById.put(equipment.getEquipmentId(), equipment);
        }

        for (Map<String, Object> row : read(CLIENTS_FILE)) {
            Client client = new Client(Json.integer(row, "clientId", 0), Json.string(row, "name"),
                    Json.string(row, "contactInfo"), Json.string(row, "address"));
            for (Integer id : Json.integerList(row, "equipmentIds")) {
                client.addEquipment(equipmentsById.get(id));
            }
            state.clients.add(client);
        }

        return state;
    }

   
    public void save(SystemState state) {
       
        Unique<Equipment> equipmentCollector = new Unique<Equipment>().addAll(state.equipments);
        for (Client client : state.clients) {
            equipmentCollector.addAll(Arrays.asList(client.getEquipmentList()));
        }
        List<Equipment> equipments = equipmentCollector.list();

        Unique<WorkOrder> orderCollector = new Unique<WorkOrder>().addAll(state.orders);
        Unique<MaintenanceSchedule> scheduleCollector = new Unique<MaintenanceSchedule>().addAll(state.schedules);
        Unique<MaintenanceHistory> historyCollector = new Unique<MaintenanceHistory>().addAll(state.histories);
        Unique<QrCode> qrCollector = new Unique<QrCode>().addAll(state.qrCodes);
        for (Equipment equipment : equipments) {
            orderCollector.addAll(equipment.getWorkOrders());
            scheduleCollector.addAll(equipment.getMaintenanceSchedules());
            historyCollector.addAll(equipment.getMaintenanceHistories());
            if (equipment.getQrCode() != null) {
                qrCollector.addAll(Collections.singletonList(equipment.getQrCode()));
            }
        }

        Unique<Technician> technicianCollector = new Unique<Technician>().addAll(state.technicians);
        for (Technician technician : state.technicians) {
            historyCollector.addAll(technician.getReviewedHistories());
        }

        List<WorkOrder> orders = orderCollector.list();
        Unique<Alert> alertCollector = new Unique<Alert>().addAll(state.alerts);
        Unique<Report> reportCollector = new Unique<Report>().addAll(state.reports);
        Unique<Part> partCollector = new Unique<Part>().addAll(state.parts);
        Unique<SafetyRegulation> regulationCollector = new Unique<SafetyRegulation>().addAll(state.regulations);
        List<MaintenanceTask> tasks = new ArrayList<>();
        Map<Object, Integer> ownerOfAlert = new IdentityHashMap<>();
        Map<Object, Integer> ownerOfReport = new IdentityHashMap<>();
        Map<Object, Integer> ownerOfTask = new IdentityHashMap<>();
        for (WorkOrder order : orders) {
            if (order.getTechnician() != null) {
                technicianCollector.addAll(Collections.singletonList(order.getTechnician()));
            }
            alertCollector.addAll(order.getAlerts());
            reportCollector.addAll(order.getReports());
            for (Alert alert : order.getAlerts()) {
                ownerOfAlert.put(alert, order.getWorkOrderId());
            }
            for (Report report : order.getReports()) {
                ownerOfReport.put(report, order.getWorkOrderId());
            }
            for (MaintenanceTask task : order.getTasks()) {
                tasks.add(task);
                ownerOfTask.put(task, order.getWorkOrderId());
                partCollector.addAll(task.getParts());
                regulationCollector.addAll(task.getRegulations());
            }
        }

        Set<Object> savedOrders = Collections.newSetFromMap(new IdentityHashMap<>());
        savedOrders.addAll(orders);

        file(CLIENTS_FILE).writeObjects(clientRows(state.clients));
        file(EQUIPMENTS_FILE).writeObjects(equipmentRows(equipments));
        file(QR_CODES_FILE).writeObjects(qrRows(qrCollector.list()));
        file(TECHNICIANS_FILE).writeObjects(technicianRows(technicianCollector.list()));
        file(SCHEDULES_FILE).writeObjects(scheduleRows(scheduleCollector.list(), savedOrders));
        file(WORK_ORDERS_FILE).writeObjects(orderRows(orders));
        file(TASKS_FILE).writeObjects(taskRows(tasks, ownerOfTask));
        file(PARTS_FILE).writeObjects(partRows(partCollector.list()));
        file(REGULATIONS_FILE).writeObjects(regulationRows(regulationCollector.list()));
        file(HISTORIES_FILE).writeObjects(historyRows(historyCollector.list()));
        file(ALERTS_FILE).writeObjects(alertRows(alertCollector.list(), ownerOfAlert));
        file(REPORTS_FILE).writeObjects(reportRows(reportCollector.list(), ownerOfReport));
    }

    private static List<Map<String, Object>> clientRows(List<Client> clients) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Client client : clients) {
            List<Integer> equipmentIds = new ArrayList<>();
            for (Equipment equipment : client.getEquipmentList()) {
                equipmentIds.add(equipment.getEquipmentId());
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("clientId", client.getClientId());
            row.put("name", client.getName());
            row.put("contactInfo", client.getContactInfo());
            row.put("address", client.getAddress());
            row.put("equipmentIds", equipmentIds);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> equipmentRows(List<Equipment> equipments) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Equipment equipment : equipments) {
            List<Integer> historyIds = new ArrayList<>();
            for (MaintenanceHistory history : equipment.getMaintenanceHistories()) {
                historyIds.add(history.getHistoryId());
            }
            List<Integer> scheduleIds = new ArrayList<>();
            for (MaintenanceSchedule schedule : equipment.getMaintenanceSchedules()) {
                scheduleIds.add(schedule.getScheduleId());
            }
            List<Integer> orderIds = new ArrayList<>();
            for (WorkOrder order : equipment.getWorkOrders()) {
                orderIds.add(order.getWorkOrderId());
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("equipmentId", equipment.getEquipmentId());
            row.put("name", equipment.getName());
            row.put("serialNumber", equipment.getSerialNumber());
            row.put("location", equipment.getLocation());
            row.put("status", equipment.getStatus());
            row.put("qrId", equipment.getQrCode() == null ? null : equipment.getQrCode().getQrId());
            row.put("historyIds", historyIds);
            row.put("scheduleIds", scheduleIds);
            row.put("workOrderIds", orderIds);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> qrRows(List<QrCode> qrCodes) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QrCode qrCode : qrCodes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("qrId", qrCode.getQrId());
            row.put("codeData", qrCode.getCodeData());
            row.put("generateUrl", qrCode.getGenerateUrl());
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> technicianRows(List<Technician> technicians) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Technician technician : technicians) {
            List<Integer> reviewedIds = new ArrayList<>();
            for (MaintenanceHistory history : technician.getReviewedHistories()) {
                reviewedIds.add(history.getHistoryId());
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("technicianId", technician.getTechnicianId());
            row.put("name", technician.getName());
            row.put("expertise", technician.getExpertise());
            row.put("availabilityStatus", technician.getAvailabilityStatus());
            row.put("reviewedHistoryIds", reviewedIds);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> scheduleRows(List<MaintenanceSchedule> schedules,
            Set<Object> savedOrders) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MaintenanceSchedule schedule : schedules) {
            List<Integer> orderIds = new ArrayList<>();
            for (WorkOrder order : schedule.getWorkOrders()) {
                if (savedOrders.contains(order)) {
                    orderIds.add(order.getWorkOrderId());
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("scheduleId", schedule.getScheduleId());
            row.put("frequency", schedule.getFrequency());
            row.put("scheduleDate", schedule.getScheduleDate());
            row.put("isPreventive", schedule.getIsPreventive());
            row.put("workOrderIds", orderIds);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> orderRows(List<WorkOrder> orders) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (WorkOrder order : orders) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("workOrderId", order.getWorkOrderId());
            row.put("description", order.getDescription());
            row.put("priority", order.getPriority());
            row.put("status", order.getStatus());
            row.put("creationDate", order.getCreationDate());
            row.put("technicianId", order.getTechnician() == null ? null : order.getTechnician().getTechnicianId());
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> taskRows(List<MaintenanceTask> tasks, Map<Object, Integer> owners) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MaintenanceTask task : tasks) {
            List<Integer> partIds = new ArrayList<>();
            for (Part part : task.getParts()) {
                partIds.add(part.getPartId());
            }
            List<Integer> regulationIds = new ArrayList<>();
            for (SafetyRegulation regulation : task.getRegulations()) {
                regulationIds.add(regulation.getRegulationId());
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("workOrderId", owners.get(task));
            row.put("taskId", task.getTaskId());
            row.put("taskDescription", task.getTaskDescription());
            row.put("taskType", task.getTaskType());
            row.put("estimatedDuration", task.getEstimatedDuration());
            row.put("isCompleted", task.getIsCompleted());
            row.put("partIds", partIds);
            row.put("regulationIds", regulationIds);
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> partRows(List<Part> parts) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Part part : parts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("partId", part.getPartId());
            row.put("name", part.getName());
            row.put("stockQuantity", part.getStockQuantity());
            row.put("unitCost", part.getUnitCost());
            row.put("supplier", part.getSupplier());
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> regulationRows(List<SafetyRegulation> regulations) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SafetyRegulation regulation : regulations) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regulationId", regulation.getRegulationId());
            row.put("title", regulation.getTitle());
            row.put("description", regulation.getDescription());
            row.put("riskLevel", regulation.getRiskLevel());
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> historyRows(List<MaintenanceHistory> histories) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MaintenanceHistory history : histories) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("historyId", history.getHistoryId());
            row.put("maintenanceDate", history.getMaintenanceDate());
            row.put("maintenanceType", history.getMaintenanceType());
            row.put("description", history.getDescription());
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> alertRows(List<Alert> alerts, Map<Object, Integer> owners) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Alert alert : alerts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("workOrderId", owners.get(alert));
            row.put("alertId", alert.getAlertId());
            row.put("message", alert.getMessage());
            row.put("severity", alert.getSeverity());
            row.put("isResolved", alert.getIsResolved());
            rows.add(row);
        }
        return rows;
    }

    private static List<Map<String, Object>> reportRows(List<Report> reports, Map<Object, Integer> owners) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Report report : reports) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("reportId", report.getReportId());
            row.put("reportType", report.getReportType());
            row.put("generatedDate", report.getGeneratedDate());
            row.put("dataContent", report.getDataContent());
            row.put("workOrderId", owners.get(report));
            rows.add(row);
        }
        return rows;
    }

    /** Keeps each object once (compared by identity) in first-seen order. */
    private static final class Unique<T> {
        private final Set<T> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        private final List<T> items = new ArrayList<>();

        Unique<T> addAll(Collection<? extends T> values) {
            for (T value : values) {
                if (value != null && seen.add(value)) {
                    items.add(value);
                }
            }
            return this;
        }

        List<T> list() {
            return items;
        }
    }
}
