package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import static ec.edu.espe.electromechanicalmaintenancemanagementsystem.model.ConsoleOutput.show;

import java.util.Arrays;
import java.util.Locale;


public class ClientActions {
    private final SystemState state;
    private final ConsoleInput input;

    public ClientActions(SystemState state, ConsoleInput input) {
        this.state = state;
        this.input = input;
    }

    public void register() {
        String name = input.readName("Client name (letters only): ");
        String contact = input.readPhone("Contact information (10 digits starting with 0): ");
        String address = input.readText("Address: ");
        Client client = new Client(state.nextClientId(), name, contact, address);
        state.clients.add(client);
        System.out.println("Client saved to data/clients.json.");
        System.out.println(client);
    }

    public void removeEquipment() {
        show(state.clients);
        Client client = state.findClient(input.readInt("Client ID: "));
        if (client == null) {
            System.out.println("Client not found.");
            return;
        }
        show(Arrays.asList(client.getEquipmentList()));
        int equipmentId = input.readInt("Equipment ID to remove from client: ");
        if (!belongsToClient(client, equipmentId)) {
            System.out.println("That equipment is not assigned to the selected client.");
            return;
        }
        client.removeEquipment(equipmentId);
        System.out.println("Equipment association removed from this client. The equipment record remains in the system.");
        System.out.println("Updated client equipment list:");
        show(Arrays.asList(client.getEquipmentList()));
    }

    public void editOrDelete() {
        if (state.clients.isEmpty()) {
            System.out.println("No clients registered.");
            return;
        }
        System.out.println("1. Correct a client's name");
        System.out.println("2. Delete a client completely");
        int action = input.readIntInRange("Choose: ", 1, 2);
        show(state.clients);
        Client selected = state.findClient(input.readPositiveInt("Client ID: "));
        if (selected == null) {
            System.out.println("Client not found.");
            return;
        }
        if (action == 1) {
            selected.setName(input.readName("Corrected client name: "));
            System.out.println("Client name updated and saved.");
        } else {
            deleteClient(selected);
        }
    }

    public void updateContact() {
        show(state.clients);
        Client client = state.findClient(input.readPositiveInt("Client ID: "));
        if (client == null) {
            System.out.println("Client not found.");
            return;
        }
        client.setName(input.readName("Client name: "));
        client.setContactInfo(input.readPhone("Contact phone: "));
        client.setAddress(input.readText("Address: "));
        System.out.println("Client information updated.");
    }

    public void search() {
        System.out.println("1. Search client by ID or name");
        System.out.println("2. Search equipment by ID, name or serial number");
        int searchType = input.readIntInRange("Choose: ", 1, 2);
        String term = input.readText("Search term: ").toLowerCase(Locale.ROOT);
        if (searchType == 1) {
            searchClients(term);
        } else {
            searchEquipment(term);
        }
    }

    private void deleteClient(Client client) {
        if (input.readBoolean("Confirm permanent deletion (yes/no): ")) {
            state.clients.remove(client);
            System.out.println("Client deleted from data/clients.json.");
            System.out.println("Note: the client's equipment records are not automatically deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private boolean belongsToClient(Client client, int equipmentId) {
        for (Equipment item : client.getEquipmentList()) {
            if (item.getEquipmentId() == equipmentId) {
                return true;
            }
        }
        return false;
    }

    private void searchClients(String term) {
        for (Client c : state.clients) {
            if (String.valueOf(c.getClientId()).equals(term)
                    || c.getName().toLowerCase(Locale.ROOT).contains(term)
                    || c.getContactInfo().contains(term)) {
                System.out.println(c);
            }
        }
    }

    private void searchEquipment(String term) {
        for (Equipment e : state.equipments) {
            if (String.valueOf(e.getEquipmentId()).equals(term)
                    || e.getName().toLowerCase(Locale.ROOT).contains(term)
                    || e.getSerialNumber().toLowerCase(Locale.ROOT).contains(term)) {
                System.out.println(e);
                for (Client c : state.clients) {
                    if (belongsToClient(c, e.getEquipmentId())) {
                        System.out.println("Client: " + c.getName() + " (ID " + c.getClientId() + ")");
                    }
                }
            }
        }
    }
}
