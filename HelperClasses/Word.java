package spelling.HelperClasses;

/**
 * 
 * This class is used to represent the word 
 * from a special word list
 * @author hchu167
 *
 */
public class Word implements Comparable<Word>{
	private String spellWord;
	private int masteredStat;
	private int faultedStat;
	private int failedStat;
	private StatisticsType wordType;
	
	//types of stats possible
	public enum StatisticsType {
		MASTERED, FAULTED, FAILED
	}
	
	//Constructor
	public Word(String word, int master, int fault, int fail){
		spellWord = word;
		setMasteredStat(master);
		setFaultedStat(fault);
		setFailedStat(fail);
	}
	// Initialise word with empty statistics
	public Word(String word){
		this(word, 0, 0, 0);
	}
	// Increase count of word
	public void increaseStats(StatisticsType type){
		setWordType(type);
		switch (type){
		case MASTERED:
			setMasteredStat(getMasteredStat() + 1);
			break;
		case FAULTED:
			setFaultedStat(getFaultedStat() + 1);
			break;
		case FAILED:
			setFailedStat(getFailedStat() + 1);
			break;
		default:
			break;
		}
	}
	
	@Override
	public String toString(){
		return spellWord;
	}

	@Override
	public int compareTo(Word w) {
		return spellWord.compareTo(w.spellWord);
	}

	@Override
	public boolean equals(Object o){
		if (this.toString().compareToIgnoreCase(o.toString()) == 0 ) {
			return true;
		}
		return false;
	}
	public int getFailedStat() {
		return failedStat;
	}
	public void setFailedStat(int failedStat) {
		this.failedStat = failedStat;
	}
	public int getFaultedStat() {
		return faultedStat;
	}
	public void setFaultedStat(int faultedStat) {
		this.faultedStat = faultedStat;
	}
	public StatisticsType getWordType() {
		return wordType;
	}
	public void setWordType(StatisticsType wordType) {
		this.wordType = wordType;
	}
	public int getMasteredStat() {
		return masteredStat;
	}
	public void setMasteredStat(int masteredStat) {
		this.masteredStat = masteredStat;
	}
}