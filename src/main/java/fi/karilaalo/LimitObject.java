package fi.karilaalo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;

public class LimitObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7047492722998054063L;
	private int count;
	private LocalDateTime initTime;
	private final int limitMinutes;
	private static Logger log = Logger.getLogger(LimitObject.class);
	
	public LimitObject() {
		this(0, 15);
	}
	
	public LimitObject (int count, int limitMinutes) {
		this.count = count;
		this.initTime = LocalDateTime.now();
		this.limitMinutes = limitMinutes;
	}
	
	public boolean valid() {
		long validTime = ChronoUnit.MINUTES.between(initTime, LocalDateTime.now());
		log.trace("validTime: " + validTime + " initTime: " + initTime + " limitMinutes: " + limitMinutes);
		return validTime < limitMinutes;
	}
	
	public int getCount() {
		return count;
	}
	
	public void putCount(int count) {
		this.count = count;
	}

}
