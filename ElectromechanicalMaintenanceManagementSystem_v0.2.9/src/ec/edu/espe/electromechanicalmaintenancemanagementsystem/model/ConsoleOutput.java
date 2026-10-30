package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.util.List;


public final class ConsoleOutput {

    private ConsoleOutput() {
    }

    public static void show(List<?> items) {
        if (items.isEmpty()) {
            System.out.println("No records.");
        }
        for (Object item : items) {
            System.out.println(item);
        }
    }
}
