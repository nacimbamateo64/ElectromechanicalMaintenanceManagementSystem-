package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import static ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ConsoleOutput.show;

import java.time.LocalDate;


public class WorkOrderActions {
    private static final String CLOSED = "Closed";

    private final SystemState state;
    private final ConsoleInput input;

    public WorkOrderActions(SystemState state, ConsoleInput input) {
        this.state = state;
        this.input = input;
    }

    public void scheduleMaintenance() {
        Equipment equipment = chooseEquipment();
        if (equipment == null) {
            return;
        }
        String frequency = input.readText("Frequency: ");
        String date = input.readDate("Scheduled date (YYYY-MM-DD, today or future within this year): ");
        boolean preventive = input.readBoolean("Preventive maintenance (yes/no): ");
        MaintenanceSchedule schedule = new MaintenanceSchedule(state.schedules.size() + 1, frequency, date, preventive);
        state.schedules.add(schedule);
        equipment.assignSchedule(schedule);
        System.out.println(schedule);
    }

    public void createOrder() {
        Equipment equipment = chooseEquipment();
        if (equipment == null) {
            return;
        }
        WorkOrder order = equipment.requestMaintenanceOrder();
        order.setWorkOrderId(state.orders.size() + 1);
        order.setDescription("Equipment ID: " + equipment.getEquipmentId() + " | " + input.readText("Work description: "));
        order.setPriority(input.readChoice("Priority (low/medium/high): ", "low", "medium", "high"));
        state.orders.add(order);
        equipment.updateStatus("Maintenance required");
        System.out.println(order);
    }

    public void assignTechnician() {
        if (state.orders.isEmpty() || state.technicians.isEmpty()) {
            System.out.println("Register a technician and a work order first.");
            return;
        }
        show(state.orders);
        WorkOrder order = state.findOrder(input.readInt("Work order ID: "));
        show(state.technicians);
        Technician technician = state.findTechnician(input.readInt("Technician ID: "));
        if (order == null || technician == null || CLOSED.equals(order.getStatus())
                || order.getTechnician() != null || !technician.getAvailabilityStatus()) {
            System.out.println("Invalid selection or technician unavailable.");
            return;
        }
        technician.acceptWorkOrder(order);
        order.setStatus("In progress");
        System.out.println("Technician assigned: " + technician.getName());
    }

    public void addTask() {
        if (state.orders.isEmpty()) {
            System.out.println("Create a work order first.");
            return;
        }
        show(state.orders);
        WorkOrder order = state.findOrder(input.readInt("Work order ID: "));
        if (order == null || CLOSED.equals(order.getStatus())) {
            System.out.println("Work order not found or closed.");
            return;
        }
        MaintenanceTask task = order.createTask();
        task.setTaskDescription(input.readText("Task description: "));
        task.setTaskType(input.readText("Task type: "));
        task.setEstimatedDuration(input.readNonNegativeFloat("Estimated duration in hours (0 or greater): "));
        System.out.println(task);
    }

    public void registerPartAndRegulation() {
        if (state.orders.isEmpty()) {
            System.out.println("Create a work order and task first.");
            return;
        }
        WorkOrder order = chooseOpenOrderWithTasks("No open order with tasks found.");
        if (order == null) {
            return;
        }
        MaintenanceTask task = chooseTask(order);
        if (task == null || task.getIsCompleted()) {
            System.out.println("Task not found or already completed.");
            return;
        }
        if (input.readBoolean("Add a part (yes/no): ")) {
            registerPart(task);
        }
        registerRegulation(task);
    }

    public void executeTask() {
        WorkOrder order = chooseOpenOrderWithTasks("No open order with tasks found.");
        if (order == null) {
            return;
        }
        MaintenanceTask task = chooseTask(order);
        if (task == null || task.getIsCompleted() || task.getRegulations().isEmpty()) {
            System.out.println("Task not found, already completed, or missing safety verification.");
            return;
        }
        task.executeTask();
        System.out.println("Task completed: " + task.getIsCompleted());
    }

    public void closeOrder() {
        WorkOrder order = chooseOpenOrderWithTasks("No open work order with tasks found.");
        if (order == null) {
            return;
        }
        order.closeOrder();
        if (!CLOSED.equals(order.getStatus())) {
            System.out.println("Complete all tasks before closing the order.");
            return;
        }
        recordHistoryOf(order);
        Report report = order.createReport();
        report.setReportId(state.nextReportId());
        state.reports.add(report);
        Alert alert = order.createAlert();
        alert.setMessage("Work order completed");
        alert.setIsResolved(true);
        state.alerts.add(alert);
        System.out.println(report);
        System.out.println(alert);
    }

    public void showUpcomingAlerts() {
        LocalDate today = LocalDate.now();
        boolean any = false;
        for (MaintenanceSchedule schedule : state.schedules) {
            any |= printIfDue(schedule, today);
        }
        for (Alert alert : state.alerts) {
            if (!alert.getIsResolved()) {
                any = true;
                System.out.println("PENDING ALERT: " + alert);
            }
        }
        if (!any) {
            System.out.println("No upcoming or overdue maintenance in the next 30 days.");
        }
    }

    private Equipment chooseEquipment() {
        if (state.equipments.isEmpty()) {
            System.out.println("Register equipment first.");
            return null;
        }
        show(state.equipments);
        Equipment equipment = state.findEquipment(input.readInt("Equipment ID: "));
        if (equipment == null) {
            System.out.println("Equipment not found.");
        }
        return equipment;
    }

    
    private WorkOrder chooseOpenOrderWithTasks(String notFoundMessage) {
        show(state.orders);
        WorkOrder order = state.findOrder(input.readInt("Work order ID: "));
        if (order == null || CLOSED.equals(order.getStatus()) || order.getTasks().isEmpty()) {
            System.out.println(notFoundMessage);
            return null;
        }
        return order;
    }

    private MaintenanceTask chooseTask(WorkOrder order) {
        show(order.getTasks());
        return order.findTask(input.readInt("Task ID: "));
    }

    private void registerPart(MaintenanceTask task) {
        String name = input.readText("Part name: ");
        int quantity = input.readNonNegativeInt("Stock quantity (0 or greater): ");
        float cost = input.readNonNegativeFloat("Unit cost (0 or greater): ");
        String supplier = input.readText("Supplier: ");
        Part part = new Part(state.parts.size() + 1, name, quantity, cost, supplier);
        state.parts.add(part);
        task.recordPart(part);
        System.out.println(part);
    }

    private void registerRegulation(MaintenanceTask task) {
        String title = input.readText("Safety regulation title: ");
        String description = input.readText("Safety description: ");
        String risk = input.readChoice("Risk level (low/medium/high/critical): ", "low", "medium", "high", "critical");
        SafetyRegulation regulation = new SafetyRegulation(state.regulations.size() + 1, title, description, risk);
        state.regulations.add(regulation);
        task.verifySafety(regulation);
        System.out.println(regulation);
    }

    private void recordHistoryOf(WorkOrder order) {
        Equipment maintained = state.findEquipment(order.extractEquipmentId());
        if (maintained == null) {
            return;
        }
        MaintenanceHistory history = new MaintenanceHistory(state.histories.size() + 1,
                LocalDate.now().toString(), "Corrective/Completed work order", order.buildMaintenanceDetails());
        state.histories.add(history);
        maintained.attachHistory(history);
    }

    private boolean printIfDue(MaintenanceSchedule schedule, LocalDate today) {
        try {
            LocalDate due = LocalDate.parse(schedule.getScheduleDate());
            if (due.isBefore(today)) {
                System.out.println("OVERDUE: " + schedule);
                return true;
            }
            if (!due.isAfter(today.plusDays(30))) {
                System.out.println("UPCOMING: " + schedule);
                return true;
            }
        } catch (RuntimeException ignored) {
          
        }
        return false;
    }
}
