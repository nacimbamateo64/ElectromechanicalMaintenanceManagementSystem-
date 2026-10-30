package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import static ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ConsoleOutput.show;



public class TechnicianActions {
    private final SystemState state;
    private final ConsoleInput input;

    public TechnicianActions(SystemState state, ConsoleInput input) {
        this.state = state;
        this.input = input;
    }

    public void register() {
        String name = input.readName("Technician name: ");
        String expertise = input.readText("Expertise: ");
        Technician technician = new Technician(state.technicians.size() + 1, name, expertise, true);
        state.technicians.add(technician);
        System.out.println(technician);
    }

    public void updateAvailability() {
        show(state.technicians);
        Technician technician = state.findTechnician(input.readPositiveInt("Technician ID: "));
        if (technician == null) {
            System.out.println("Technician not found.");
            return;
        }
        technician.setAvailabilityStatus(input.readBoolean("Technician available (yes/no): "));
        System.out.println("Technician availability updated: " + technician);
    }
}
