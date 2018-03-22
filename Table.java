import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clasa corespunzatoare reprezentarii unui tabel.
 * 
 * @author Ana
 *
 */
public class Table {

	/* Denumirea tabelului. */
	protected String tableName;
	/* Lista de coloane a tabelului. */
	protected ArrayList<ArrayList<Object>> content;
	/*
	 * HashMap pentru asocierea denumirilor coloanelor si pozitiilor in lista de
	 * coloane.
	 */
	protected HashMap<String, Integer> hashMapNames = new HashMap<String, Integer>();
	/*
	 * HashMap pentru asocierea pozitiilor din lista de coloane cu tipurile
	 * coloanelor.
	 */
	protected HashMap<Integer, String> hashMapTypes = new HashMap<Integer, String>();
	/* Numarul de coloane. */
	protected int n;
	/* Semafor pentru excluderea mutuala intre procese. */
	protected Semaphore rw = new Semaphore(1);
	/* Semafor pentru accesul cititorilor. */
	protected Semaphore mutexR = new Semaphore(1);
	/* Numarul cititorilor. */
	protected AtomicInteger no = new AtomicInteger(0);

	public Table(String tableName, String[] columnNames, String[] columnTypes) {
		super();
		this.tableName = tableName;
		this.n = columnNames.length;
		content = new ArrayList<ArrayList<Object>>(n);
		for (int i = 0; i < n; i++) {
			hashMapNames.put(columnNames[i], i);
			hashMapTypes.put(i, columnTypes[i]);
		}

		for (int i = 0; i < n; i++) {
			content.add(new ArrayList<Object>());
		}
	}

	/**
	 * Functie pentru inserarea de linii.
	 * 
	 * @param values
	 */
	public void addValues(ArrayList<Object> values) {

		try {
			rw.acquire();
		} catch (InterruptedException e) {
		}

		for (int i = 0; i < values.size(); i++) {

			if ((hashMapTypes.get(i).equals("int") && !(values.get(i) instanceof Integer))
					|| ((hashMapTypes.get(i).equals("bool") && !(values.get(i) instanceof Boolean)))
					|| ((hashMapTypes.get(i).equals("string") && !(values.get(i) instanceof String)))) {
				throw new RuntimeException("Se incearca inserarea unor date invalide");
			} else {
				this.content.get(i).add(values.get(i));
			}

		}
		rw.release();
	}

	/**
	 * Functie pentru modificarea unei linii.
	 * 
	 * @param values
	 * @param line
	 */
	public void modify(ArrayList<Object> values, int line) {

		for (int i = 0; i < values.size(); i++) {
			if ((hashMapTypes.get(i).equals("int") && !(values.get(i) instanceof Integer))
					|| ((hashMapTypes.get(i).equals("bool") && !(values.get(i) instanceof Boolean)))
					|| ((hashMapTypes.get(i).equals("string") && !(values.get(i) instanceof String)))) {
				throw new RuntimeException("Se incearca inserarea unor date invalide");
			} else {
				this.content.get(i).set(line, values.get(i));
			}
		}

	}

	/**
	 * Functie pentru actualizarea tabelului in functie de o conditie.
	 * 
	 * @param condition
	 * @param values
	 */
	public void updateValues(String condition, ArrayList<Object> values) {

		try {
			rw.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (condition.equals("")) {
			if (!content.get(0).isEmpty()) {
				int nrElem = content.get(0).size();
				for (int j = 0; j < nrElem; j++) {
					this.modify(values, j);
				}
			}
		} else {
			String[] tokens = condition.split(" ");
			int column = hashMapNames.get(tokens[0]);
			String cond = tokens[1];
			String type = hashMapTypes.get(column);
			String rightOp = tokens[2];
			int nrElem = content.get(column).size();
			int toCompare;

			for (int j = 0; j < nrElem; j++) {

				if (type.equals("int")) {

					toCompare = Integer.valueOf(content.get(column).get(j).toString());
					if (cond.equals("==")) {

						if (toCompare == Integer.valueOf(rightOp)) {
							this.modify(values, j);
						}
					} else if (cond.equals(">")) {

						if (toCompare > Integer.valueOf(rightOp)) {
							this.modify(values, j);
						}
					} else if (cond.equals("<")) {

						if (toCompare < Integer.valueOf(rightOp)) {
							this.modify(values, j);
						}
					}

				} else {

					if (cond.equals("==")) {
						if (content.get(column).get(j).equals(rightOp)) {
							this.modify(values, j);
						}
					}
				}
			}
		}

		rw.release();

	}

	/**
	 * Functie pentru selectarea anumitor valori pe baza unei conditii.
	 * 
	 * @param operations
	 * @param condition
	 * @return
	 */
	public ArrayList<ArrayList<Object>> selectValues(String[] operations, String condition) {

		try {
			mutexR.acquire();
			no.incrementAndGet();
			if (no.get() == 1) {
				rw.acquire();
			}
			mutexR.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ArrayList<Integer> validLines = new ArrayList<Integer>();
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();

		if (condition.equals("")) {

			if (!content.isEmpty()) {
				int nrElem = content.get(0).size();
				for (int j = 0; j < nrElem; j++) {
					validLines.add(j);
				}

			}
		} else {
			String[] tokens = condition.split(" ");
			int column = hashMapNames.get(tokens[0]);
			String cond = tokens[1];
			String type = hashMapTypes.get(column);
			String rightOp = tokens[2];
			int nrElem = content.get(column).size();
			int toCompare;

			for (int j = 0; j < nrElem; j++) {

				if (type.equals("int")) {

					toCompare = Integer.valueOf(content.get(column).get(j).toString());
					if (cond.equals("==")) {

						if (toCompare == Integer.valueOf(rightOp)) {
							validLines.add(j);
						}
					} else if (cond.equals(">")) {

						if (toCompare > Integer.valueOf(rightOp)) {
							validLines.add(j);
						}
					} else if (cond.equals("<")) {

						if (toCompare < Integer.valueOf(rightOp)) {
							validLines.add(j);
						}
					}

				} else {

					if (cond.equals("==")) {
						if (content.get(column).get(j).equals(rightOp)) {
							validLines.add(j);
						}
					}
				}

			}
		}

		int opSize = operations.length;
		for (int i = 0; i < opSize; i++) {
			ArrayList<Object> columnResult = new ArrayList<Object>();
			String colName;
			int col;
			String operation = operations[i];

			if (operation.startsWith("min(")) {
				colName = operation.substring(4, operation.length() - 1);
				col = hashMapNames.get(colName);

				int elem;
				int min = Integer.valueOf(content.get(col).get(validLines.get(0)).toString());
				for (int j = 0; j < validLines.size(); j++) {

					elem = Integer.valueOf(content.get(col).get(validLines.get(j)).toString());
					min = Math.min(elem, min);

				}

				columnResult.add(min);
				result.add(columnResult);

			} else if (operation.startsWith("max(")) {
				colName = operation.substring(4, operation.length() - 1);
				col = hashMapNames.get(colName);

				int elem;
				int max = Integer.valueOf(content.get(col).get(validLines.get(0)).toString());
				for (int j = 0; j < validLines.size(); j++) {

					elem = Integer.valueOf(content.get(col).get(validLines.get(j)).toString());
					max = Math.max(elem, max);

				}

				columnResult.add(max);
				result.add(columnResult);

			} else if (operation.startsWith("count(")) {
				colName = operation.substring(6, operation.length() - 1);
				col = hashMapNames.get(colName);

				columnResult.add(new Integer(validLines.size()));
				result.add(columnResult);

			} else if (operation.startsWith("avg(")) {
				colName = operation.substring(4, operation.length() - 1);
				col = hashMapNames.get(colName);
				int sum = 0;

				int elem;
				for (int j = 0; j < validLines.size(); j++) {

					elem = Integer.valueOf(content.get(col).get(validLines.get(j)).toString());
					sum += elem;

				}

				columnResult.add(sum / validLines.size());
				result.add(columnResult);

			} else if (operation.startsWith("sum(")) {
				colName = operation.substring(4, operation.length() - 1);
				col = hashMapNames.get(colName);
				int sum = 0;

				int elem;
				for (int j = 0; j < validLines.size(); j++) {

					elem = Integer.valueOf(content.get(col).get(validLines.get(j)).toString());
					sum += elem;

				}

				columnResult.add(sum);
				result.add(columnResult);

			} else {// daca se cere o anumita coloana

				colName = operation;
				col = hashMapNames.get(colName);

				String elem;
				for (int j = 0; j < validLines.size(); j++) {
					elem = content.get(col).get(validLines.get(j)).toString();

					if (hashMapTypes.get(col).equals("int")) {
						columnResult.add(Integer.valueOf(elem));
					} else {
						columnResult.add(elem);
					}

				}
				result.add(columnResult);

			}
		}

		try {
			mutexR.acquire();
			no.decrementAndGet();
			if (no.get() == 0){
				rw.release();
			}
			mutexR.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

}

