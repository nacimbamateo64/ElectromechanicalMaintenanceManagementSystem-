Electromechanical Maintenance Management System - corrected build

HOW TO BUILD AND RUN (Apache NetBeans / Java Ant)
1. Open this folder as an existing Java Ant project in NetBeans.
2. Run Build Project, then Run Project.
3. In a terminal opened at the project root, the main source can also be compiled with:
   javac -encoding UTF-8 -d out @sources.txt
   (Create sources.txt first with: dir /s /b src\*.java > sources.txt on Windows.)
4. Run from the project root with:
   java -cp out ec.edu.espe.electromechanicalmaintenancemanagementsystem.view.ElectromechanicalMaintenanceManagementSystem

IMPLEMENTED / FIXED
- Console menu has 15 options; option 14 now actually generates maintenance_report.csv.
- Option 15 exits cleanly.
- CSV export uses UTF-8 with BOM for Excel, quotes CSV cells, and exports the in-memory records from all current record lists, including tasks, task parts, and task safety regulations.
- All data is saved as JSON, one file per record type, inside the data/ folder, and loaded on startup:
    clients.json, equipments.json, qr_codes.json, technicians.json,
    maintenance_schedules.json, work_orders.json, maintenance_tasks.json,
    parts.json, safety_regulations.json, maintenance_history.json,
    alerts.json, reports.json
  Relations (client-equipment, equipment-work orders, task-parts, etc.) are stored as IDs and rebuilt on load.
- All JSON code lives in the model package (Json, JsonFileManager, JsonDataStore, SystemState).
  The controller package is intentionally empty.
- A damaged JSON file is not overwritten silently: a copy is kept as <name>.json.corrupt.
- Equipment removal checks that the equipment is actually associated with the selected client. It removes the association only; it does not delete the equipment record from the whole system.
- Uses only standard Java APIs; no JSON or CSV external library is required.

IMPORTANT LIMITATIONS TO KNOW
- CSV records are exported in two columns (Record Type and Record Data). The data column contains each object's readable toString() representation rather than one spreadsheet column per field.
- The supplied JUnit test source files refer to JUnit libraries and testdata CSV fixtures that are not included in this project ZIP. Main application source compilation and console workflow smoke tests were performed, but the existing JUnit suite cannot run as packaged until those dependencies/fixtures are restored.
