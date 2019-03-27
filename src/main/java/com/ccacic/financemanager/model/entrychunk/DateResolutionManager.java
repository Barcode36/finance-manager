package com.ccacic.financemanager.model.entrychunk;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Provides utility methods around different date resolutions
 * @author Cameron Cacic
 *
 */
public class DateResolutionManager {
	
	private DateResolution resolution;
	
	/**
	 * Creates a new DateResolutionManager around the passed resolution
	 * @param resolution the DateResolution
	 */
	public DateResolutionManager(DateResolution resolution) {
		this.resolution = resolution;
	}
	
	/**
	 * Formats the passed range into a resolved range displayable to the user
	 * @param start the start of the range
	 * @param end the end of the range
	 * @return the formatted, resolved range
	 */
	public String getFormattedRange(LocalDateTime start, LocalDateTime end) {
		
		LocalDateTime[] range = getResolvedRange(start, end);
		
		DateTimeFormatter df;
		switch (resolution) {
		case ANNUALY:
			if (range[0].getYear() == range[1].getYear()) {
				return range[0].getYear() + "";
			} else {
				return range[0].getYear() + " - " + range[1].getYear();
			}
			
		case DAILY:
			df = DateTimeFormatter.ofPattern("MMM dd");
			if (range[0].getYear() == range[1].getYear()) {
				return range[0].getYear() + ": " + df.format(range[0]) + " - " + df.format(range[1]);
			} else {
				return range[0].getYear() + " - " + range[1].getYear() + ": " + df.format(range[0]) + " - " + df.format(range[1]);
			}
			
		case MONTHLY:
			df = DateTimeFormatter.ofPattern("MMM");
			if (range[0].getYear() == range[1].getYear()) {
				return range[0].getYear() + ": " + df.format(range[0]) + " - " + df.format(range[1]);
			} else {
				return range[0].getYear() + " - " + range[1].getYear() + ": " + df.format(range[0]) + " - " + df.format(range[1]);
			}
		case WEEKLY:
			df = DateTimeFormatter.ofPattern("MMM dd");
			if (range[0].getYear() == range[1].getYear()) {
				return range[0].getYear() + ": " + df.format(range[0]) + " - " + df.format(range[1]);
			} else {
				return range[0].getYear() + " - " + range[1].getYear() + ": " + df.format(range[0]) + " - " + df.format(range[1]);
			}
		default:
			return null;
		}
	}
	
	/**
	 * Resolves the passed range to the extreme ends of the resolution
	 */
	public LocalDateTime[] getResolvedRange(LocalDateTime start, LocalDateTime end) {
		
		LocalDateTime[] range = new LocalDateTime[2];
		
		switch (resolution) {
		case ANNUALY:
			range[0] = LocalDateTime.of(start.getYear(), 1, 1, 0, 0);
			range[1] = LocalDateTime.of(end.getYear(), 12, 31, 23, 59);
			break;
		case DAILY:
			range[0] = LocalDateTime.of(start.getYear(), start.getMonth(), start.getDayOfMonth(), 0, 0);
			range[1] = LocalDateTime.of(end.getYear(), end.getMonth(), end.getDayOfMonth(), 23, 59);
			break;
		case MONTHLY:
			range[0] = LocalDateTime.of(start.getYear(), start.getMonth(), 1, 0, 0);
			range[1] = LocalDateTime.of(end.getYear(), end.getMonth(), end.toLocalDate().lengthOfMonth(),
					23, 59);
			break;
		case WEEKLY:
			TemporalField field = WeekFields.of(Locale.getDefault()).dayOfWeek();
			range[0] = start.with(field, 1);
			range[0] = LocalDateTime.of(range[0].toLocalDate(), LocalTime.MIDNIGHT);
			range[1] = end.with(field, 7);
			range[1] = LocalDateTime.of(range[1].toLocalDate(), LocalTime.MIDNIGHT.minusMinutes(1));
			break;
		default:
			break;
		}
		
		return range;
		
	}
	
	/**
	 * Calculates the amount of resolution increments there are between the passed
	 * start and end at the current resolution
	 * @param start the starting date and time
	 * @param end the ending date and time
	 * @return the resolution count
	 */
	public int getResolutionCount(LocalDateTime start, LocalDateTime end) {
		
		LocalDateTime[] adjRange = getResolvedRange(start, end);
		start = adjRange[0];
		end = adjRange[1];
		
		switch (resolution) {
		case ANNUALY:
			return (int) ChronoUnit.YEARS.between(start, end) + 1;
		case DAILY:
			return (int) ChronoUnit.DAYS.between(start, end) + 1;
		case MONTHLY:
			return (int) ChronoUnit.MONTHS.between(start, end) + 1;
		case WEEKLY:
			return (int) ChronoUnit.WEEKS.between(start, end) + 1;
		default:
			return 0;
		}
		
	}
	
	public LocalDateTime addResolution(LocalDateTime dateTime, long count) {
		switch (resolution) {
		case ANNUALY:
			return dateTime.plusYears(count);
		case DAILY:
			return dateTime.plusDays(count);
		case MONTHLY:
			return dateTime.plusMonths(count);
		case WEEKLY:
			return dateTime.plusWeeks(count);
		default:
			return dateTime;
		}
	}
	
	public LocalDateTime subtractResolution(LocalDateTime dateTime, long count) {
		switch (resolution) {
		case ANNUALY:
			return dateTime.minusYears(count);
		case DAILY:
			return dateTime.minusDays(count);
		case MONTHLY:
			return dateTime.minusMonths(count);
		case WEEKLY:
			return dateTime.minusWeeks(count);
		default:
			return dateTime;
		}
	}
	
	public DateResolution getResolution() {
		return resolution;
	}
	
	public void setResolution(DateResolution resolution) {
		this.resolution = resolution;
	}

}
