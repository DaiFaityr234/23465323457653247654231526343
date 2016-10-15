package spelling.HelperClasses;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial")
public class StatisticsTable extends JFrame {
	private JTable table;
	
	public StatisticsTable(List<Word> wordList) {

		TableModel tableModel = new WordTableModel(wordList);
		table = new JTable(tableModel);
		
		// ENABLE SORTING
		table.setAutoCreateRowSorter(true);
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		table.setRowSorter(sorter);
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		
		// SORT BY MULTIPE COLUMNS
		int columnIndexForJob = 0;
		sortKeys.add(new RowSorter.SortKey(columnIndexForJob, SortOrder.ASCENDING));
		
		sorter.setSortKeys(sortKeys);

		// LISTEN TO SORTING EVENTS
		sorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent evt) {
				int indexOfNoColumn = 0;
				for (int i = 0; i < table.getRowCount(); i++) {
					table.setValueAt(i + 1, i, indexOfNoColumn);
				}
			}
		});
		sorter.sort();
		
		
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
	}

}