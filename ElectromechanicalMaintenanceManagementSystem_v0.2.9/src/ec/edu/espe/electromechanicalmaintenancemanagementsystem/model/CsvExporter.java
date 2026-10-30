package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static void export(String fileName, SystemState state) {
        try {
            Path output = Path.of(fileName).toAbsolutePath().normalize();
            Path folder = output.getParent();
            if (folder == null) {
                folder = Path.of(".").toAbsolutePath().normalize();
            }
            Files.createDirectories(folder);
            int count = 0;
            count += exportCsvFile(folder.resolve("clients.csv"), Client.class, state.clients);
            count += exportCsvFile(folder.resolve("equipments.csv"), Equipment.class, state.equipments);
            count += exportCsvFile(folder.resolve("technicians.csv"), Technician.class, state.technicians);
            count += exportCsvFile(folder.resolve("maintenance_schedules.csv"), MaintenanceSchedule.class, state.schedules);
            count += exportCsvFile(folder.resolve("work_orders.csv"), WorkOrder.class, state.orders);

            ArrayList<MaintenanceTask> tasks = new ArrayList<>();
            for (WorkOrder order : state.orders) {
                tasks.addAll(order.getTasks());
            }
            count += exportCsvFile(folder.resolve("maintenance_tasks.csv"), MaintenanceTask.class, tasks);
            count += exportCsvFile(folder.resolve("maintenance_history.csv"), MaintenanceHistory.class, state.histories);
            count += exportCsvFile(folder.resolve("parts.csv"), Part.class, state.parts);
            count += exportCsvFile(folder.resolve("safety_regulations.csv"), SafetyRegulation.class, state.regulations);
            count += exportCsvFile(folder.resolve("alerts.csv"), Alert.class, state.alerts);
            count += exportCsvFile(folder.resolve("reports.csv"), Report.class, state.reports);
            count += exportCsvFile(folder.resolve("qr_codes.csv"), QrCode.class, state.qrCodes);

            System.out.println("CSV files generated successfully.");
            System.out.println("Folder: " + folder);
            System.out.println("Records exported: " + count);
        } catch (IOException | SecurityException exception) {
            System.out.println("Could not generate CSV files: " + exception.getMessage());
        } catch (InvalidPathException exception) {
            System.out.println("Invalid file path: " + exception.getMessage());
        }
    }

    private static int exportCsvFile(Path output, Class<?> recordClass,
            java.util.List<?> records) throws IOException {

        java.util.List<java.lang.reflect.Method> getters =
                new ArrayList<>();

        for (java.lang.reflect.Method method : recordClass.getMethods()) {
            String name = method.getName();

            boolean getter = (name.startsWith("get") && name.length() > 3)
                    || (name.startsWith("is") && name.length() > 2);

            if (!getter || method.getParameterCount() != 0
                    || name.equals("getClass")) {
                continue;
            }

            Class<?> type = method.getReturnType();

            boolean simple = type.isPrimitive()
                    || type == String.class
                    || Number.class.isAssignableFrom(type)
                    || type == Boolean.class
                    || type == Character.class
                    || type.isEnum()
                    || java.util.Date.class.isAssignableFrom(type)
                    || type.getName().startsWith("java.time.");

            if (simple) {
                getters.add(method);
            }
        }

        getters.sort((a, b) -> {
            String nameA = csvPropertyName(a);
            String nameB = csvPropertyName(b);

            boolean idA = nameA.toLowerCase().endsWith("id");
            boolean idB = nameB.toLowerCase().endsWith("id");

            if (idA != idB) {
                return idA ? -1 : 1;
            }

            return nameA.compareToIgnoreCase(nameB);
        });

        try (BufferedWriter writer = Files.newBufferedWriter(
                output, StandardCharsets.UTF_8)) {

            writer.write("\uFEFF");

            for (int i = 0; i < getters.size(); i++) {
                if (i > 0) {
                    writer.write(',');
                }

                writer.write(csvCell(
                        csvHeader(csvPropertyName(getters.get(i)))));
            }

            writer.newLine();

            int count = 0;

            for (Object record : records) {
                for (int i = 0; i < getters.size(); i++) {
                    if (i > 0) {
                        writer.write(',');
                    }

                    try {
                        Object value = getters.get(i).invoke(record);

                        writer.write(csvCell(
                                value == null ? "" : String.valueOf(value)));

                    } catch (ReflectiveOperationException exception) {
                        throw new IOException(
                                "Could not read field from "
                                + recordClass.getSimpleName(),
                                exception);
                    }
                }

                writer.newLine();
                count++;
            }

            System.out.println("Generated: " + output.getFileName());
            return count;
        }
    }

    private static String csvPropertyName(
            java.lang.reflect.Method method) {

        String name = method.getName();

        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }

        if (name.startsWith("Is") && name.length() > 2) {
            name = name.substring(2);
        }

        return Character.toLowerCase(name.charAt(0))
                + name.substring(1);
    }

    private static String csvHeader(String property) {
        String spaced = property.replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(spaced.charAt(0))
                + spaced.substring(1);
    }

    private static String csvCell(String value) {
        if (value == null) {
            value = "";
        }

        if (!value.isEmpty()
                && (value.charAt(0) == '='
                || value.charAt(0) == '+'
                || value.charAt(0) == '-'
                || value.charAt(0) == '@'
                || value.charAt(0) == '\t'
                || value.charAt(0) == '\r')) {
            value = "'" + value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
