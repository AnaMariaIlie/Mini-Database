import java.util.ArrayList;
import java.util.HashMap;

public class Database implements MyDatabase {

	/* lista de tabele */
	public ArrayList<Table> database = new ArrayList<Table>();
	/*
	 * HashMap pentru asocierea denumirilor tabelelor si pozitiile in lista de
	 * tabele.
	 */
	public HashMap<String, Integer> tableNames = new HashMap<String, Integer>();
	/* pozitia fiecarui tabel in lista */
	static int POSITION = 0;

	@Override
	public void initDb(int numWorkerThreads) {
	}

	@Override
	public void stopDb() {
		POSITION = 0;
	}

	@Override
	public void createTable(String tableName, String[] columnNames, String[] columnTypes) {
		Table table = new Table(tableName, columnNames, columnTypes);
		database.add(table);
		tableNames.put(tableName, POSITION);
		POSITION++;
	}

	@Override
	public ArrayList<ArrayList<Object>> select(String tableName, String[] operations, String condition) {

		return database.get(tableNames.get(tableName)).selectValues(operations, condition);

	}

	@Override
	public void update(String tableName, ArrayList<Object> values, String condition) {

		database.get(tableNames.get(tableName)).updateValues(condition, values);
	}

	@Override
	public void insert(String tableName, ArrayList<Object> values) {

		database.get(tableNames.get(tableName)).addValues(values);
	}

	@Override
	public void startTransaction(String tableName) {

	}

	@Override
	public void endTransaction(String tableName) {

	}

}

