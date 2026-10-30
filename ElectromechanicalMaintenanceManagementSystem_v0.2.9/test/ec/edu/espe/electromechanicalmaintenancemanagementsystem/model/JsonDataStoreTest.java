package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import ec.edu.espe.electromechanicalmaintenancemanagementsystem.testutils.TestData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonDataStoreTest {

    private static void deleteRecursively(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    private void assertClientRoundTrip(int caseNumber) throws Exception {
        String[] row = TestData.row("json_saveLoadClients.csv", caseNumber);
        int clientId = Integer.parseInt(row[1]);
        String name = row[2];
        String contactInfo = row[3];
        String address = row[4];
        String expResult = row[5];
        Path folder = Files.createTempDirectory("json_store_test_");
        try {
            JsonDataStore store = new JsonDataStore(folder);
            SystemState state = new SystemState();
            state.clients.add(new Client(clientId, name, contactInfo, address));
            store.save(state);
            SystemState loaded = store.load();
            assertEquals(1, loaded.clients.size());
            assertEquals(expResult, loaded.clients.get(0).getName());
            assertEquals(contactInfo, loaded.clients.get(0).getContactInfo());
            assertEquals(address, loaded.clients.get(0).getAddress());
        } finally {
            deleteRecursively(folder);
        }
    }

    @Test
    public void testSaveAndLoadClients1() throws Exception {
        assertClientRoundTrip(1);
    }

    @Test
    public void testSaveAndLoadClients2() throws Exception {
        assertClientRoundTrip(2);
    }

    @Test
    public void testSaveAndLoadClients3() throws Exception {
        assertClientRoundTrip(3);
    }

    @Test
    public void testSaveAndLoadClients4() throws Exception {
        assertClientRoundTrip(4);
    }

    @Test
    public void testSaveAndLoadClients5() throws Exception {
        assertClientRoundTrip(5);
    }

    @Test
    public void testSaveAndLoadClients6() throws Exception {
        assertClientRoundTrip(6);
    }

    @Test
    public void testSaveAndLoadClients7() throws Exception {
        assertClientRoundTrip(7);
    }

    @Test
    public void testSaveAndLoadClients8() throws Exception {
        assertClientRoundTrip(8);
    }

    @Test
    public void testSaveAndLoadClients9() throws Exception {
        assertClientRoundTrip(9);
    }

    @Test
    public void testSaveAndLoadClients10() throws Exception {
        assertClientRoundTrip(10);
    }

    @Test
    public void testLoadFromMissingFolderReturnsEmptyState() {
        SystemState loaded = new JsonDataStore(Path.of("folder_that_does_not_exist_123")).load();
        assertTrue(loaded.clients.isEmpty());
        assertTrue(loaded.equipments.isEmpty());
        assertTrue(loaded.orders.isEmpty());
    }

    @Test
    public void testEveryRecordTypeIsSavedAndRelationsAreRebuilt() throws Exception {
        Path folder = Files.createTempDirectory("json_store_graph_");
        try {
            SystemState state = new SystemState();

            Client client = new Client(1, "Ana \"Q\" Ruiz", "0991234567", "Calle 1\nPiso 2 \\ Quito");
            Equipment equipment = new Equipment(1, "Pump", "SN-1", "Plant A", "Available");
            client.addEquipment(equipment);
            QrCode qr = equipment.createQrCode();
            Technician technician = new Technician(1, "Luis", "Electrical", true);
            MaintenanceSchedule schedule = new MaintenanceSchedule(1, "Monthly", "2026-12-01", true);
            equipment.assignSchedule(schedule);

            WorkOrder order = equipment.requestMaintenanceOrder();
            order.setWorkOrderId(1);
            order.setDescription("Equipment ID: 1 | Check motor");
            technician.acceptWorkOrder(order);
            order.setStatus("In progress");

            MaintenanceTask task = order.createTask();
            task.setEstimatedDuration(2.5f);
            Part part = new Part(1, "Bearing", 4, 12.75f, "ACME");
            SafetyRegulation regulation = new SafetyRegulation(1, "Lockout", "Lock the breaker", "high");
            task.recordPart(part);
            task.verifySafety(regulation);
            task.executeTask();
            order.closeOrder();
            Report report = order.createReport();
            report.setReportId(1);
            Alert alert = order.createAlert();
            MaintenanceHistory history = new MaintenanceHistory(1, "2026-10-30", "Corrective", "Done");
            equipment.attachHistory(history);
            technician.reviewHistory(history);

            state.clients.add(client);
            state.equipments.add(equipment);
            state.qrCodes.add(qr);
            state.technicians.add(technician);
            state.schedules.add(schedule);
            state.orders.add(order);
            state.parts.add(part);
            state.regulations.add(regulation);
            state.reports.add(report);
            state.alerts.add(alert);
            state.histories.add(history);

            JsonDataStore store = new JsonDataStore(folder);
            store.save(state);

            for (String file : new String[]{JsonDataStore.CLIENTS_FILE, JsonDataStore.EQUIPMENTS_FILE,
                JsonDataStore.QR_CODES_FILE, JsonDataStore.TECHNICIANS_FILE, JsonDataStore.SCHEDULES_FILE,
                JsonDataStore.WORK_ORDERS_FILE, JsonDataStore.TASKS_FILE, JsonDataStore.PARTS_FILE,
                JsonDataStore.REGULATIONS_FILE, JsonDataStore.HISTORIES_FILE, JsonDataStore.ALERTS_FILE,
                JsonDataStore.REPORTS_FILE}) {
                assertTrue(file + " was not created", Files.exists(folder.resolve(file)));
            }

            SystemState loaded = store.load();
            assertEquals(1, loaded.clients.size());
            assertEquals(1, loaded.equipments.size());
            assertEquals(1, loaded.qrCodes.size());
            assertEquals(1, loaded.technicians.size());
            assertEquals(1, loaded.schedules.size());
            assertEquals(1, loaded.orders.size());
            assertEquals(1, loaded.parts.size());
            assertEquals(1, loaded.regulations.size());
            assertEquals(1, loaded.reports.size());
            assertEquals(1, loaded.alerts.size());
            assertEquals(1, loaded.histories.size());

            Client loadedClient = loaded.clients.get(0);
            assertEquals("Ana \"Q\" Ruiz", loadedClient.getName());
            assertEquals("Calle 1\nPiso 2 \\ Quito", loadedClient.getAddress());

            Equipment loadedEquipment = loaded.equipments.get(0);
            assertEquals(1, loadedClient.getEquipmentList().length);
            assertSame(loadedEquipment, loadedClient.getEquipmentList()[0]);
            assertSame(loaded.qrCodes.get(0), loadedEquipment.getQrCode());
            assertSame(loaded.schedules.get(0), loadedEquipment.getMaintenanceSchedules().get(0));
            assertSame(loaded.histories.get(0), loadedEquipment.getMaintenanceHistories().get(0));
            assertSame(loaded.histories.get(0), loaded.technicians.get(0).getReviewedHistories().get(0));

            WorkOrder loadedOrder = loaded.orders.get(0);
            assertSame(loadedOrder, loadedEquipment.getWorkOrders().get(0));
            assertSame(loaded.technicians.get(0), loadedOrder.getTechnician());
            assertEquals("Closed", loadedOrder.getStatus());
            assertSame(loaded.alerts.get(0), loadedOrder.getAlerts().get(0));
            assertSame(loaded.reports.get(0), loadedOrder.getReports().get(0));

            assertEquals(1, loadedOrder.getTasks().size());
            MaintenanceTask loadedTask = loadedOrder.getTasks().get(0);
            assertTrue(loadedTask.getIsCompleted());
            assertEquals(2.5f, loadedTask.getEstimatedDuration(), 0.0001f);
            assertSame(loaded.parts.get(0), loadedTask.getParts().get(0));
            assertEquals(12.75f, loaded.parts.get(0).getUnitCost(), 0.0001f);
            assertSame(loaded.regulations.get(0), loadedTask.getRegulations().get(0));

            // Saving the loaded state again must give exactly the same files.
            Path second = Files.createTempDirectory("json_store_graph_again_");
            try {
                new JsonDataStore(second).save(loaded);
                assertEquals(Files.readString(folder.resolve(JsonDataStore.WORK_ORDERS_FILE)),
                        Files.readString(second.resolve(JsonDataStore.WORK_ORDERS_FILE)));
                assertEquals(Files.readString(folder.resolve(JsonDataStore.EQUIPMENTS_FILE)),
                        Files.readString(second.resolve(JsonDataStore.EQUIPMENTS_FILE)));
                assertEquals(Files.readString(folder.resolve(JsonDataStore.TASKS_FILE)),
                        Files.readString(second.resolve(JsonDataStore.TASKS_FILE)));
            } finally {
                deleteRecursively(second);
            }
        } finally {
            deleteRecursively(folder);
        }
    }

    @Test
    public void testDamagedFileIsKeptAndDoesNotCrash() throws Exception {
        Path folder = Files.createTempDirectory("json_store_damaged_");
        try {
            Files.writeString(folder.resolve(JsonDataStore.CLIENTS_FILE), "[ {\"clientId\": 1, ");
            SystemState loaded = new JsonDataStore(folder).load();
            assertTrue(loaded.clients.isEmpty());
            assertTrue(Files.exists(folder.resolve(JsonDataStore.CLIENTS_FILE + ".corrupt")));
        } finally {
            deleteRecursively(folder);
        }
    }
}
