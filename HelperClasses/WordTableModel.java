package spelling.HelperClasses;

/**
 * 
 * This class is used to represent the model
 * of the statistics table for special words
 * @author hchu167
 *
 */
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class WordTableModel extends AbstractTableModel {
	private static final int WORD 	= 0;
	private static final int WORD_MASTERED 	= 1;
	private static final int WORD_FAULTED	= 2;
	private static final int WORD_FAILED	= 3;
	
	private String[] columnNames = {"Word", "Mastered", "Faulted", "Failed"};
	private List<Word> wordList;
	
	public WordTableModel(List<Word> list) {
		this.wordList = list;

	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return wordList.size();
	}
	
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		Word w = wordList.get(row);
		Object returnValue = null;
		
		switch (col) {
		case WORD:
			returnValue = w.toString();
			break;
		case WORD_MASTERED:
			returnValue = w.getMasteredStat();
			break;
		case WORD_FAULTED:
			returnValue = w.getFaultedStat();
			break;
		case WORD_FAILED:
			returnValue = w.getFailedStat();
			break;
		default:
			throw new IllegalArgumentException("Invalid column index");
		}
		
		return returnValue;
	}
	

}