package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import static ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ConsoleOutput.show;


/** Console actions that show or export all records. */
public class RecordActions {
    private final SystemState state;

    public RecordActions(SystemState state) {
        this.state = state;
    }

    public void showAll() {
        System.out.println("\nCLIENTS");
        show(state.clients);
        System.out.println("\nEQUIPMENT");
        show(state.equipments);
        System.out.println("\nQR CODES");
        show(state.qrCodes);
        System.out.println("\nTECHNICIANS");
        show(state.technicians);
        System.out.println("\nMAINTENANCE SCHEDULES");
        show(state.schedules);
        System.out.println("\nWORK ORDERS");
        show(state.orders);
        for (WorkOrder order : state.orders) {
            System.out.println("Tasks for work order " + order.getWorkOrderId());
            show(order.getTasks());
        }
        System.out.println("\nPARTS");
        show(state.parts);
        System.out.println("\nSAFETY REGULATIONS");
        show(state.regulations);
        System.out.println("\nMAINTENANCE HISTORIES");
        show(state.histories);
        System.out.println("\nALERTS");
        show(state.alerts);
        System.out.println("\nREPORTS");
        show(state.reports);
    }

    public void exportCsv() {
        CsvExporter.export("maintenance_report.csv", state);
    }
}
