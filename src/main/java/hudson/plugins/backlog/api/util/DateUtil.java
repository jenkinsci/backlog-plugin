package hudson.plugins.backlog.api.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for date.
 * 
 * @author ikikko
 */
public class DateUtil {

	public static String formatYyyyMMdd(final Date date) {
		if (date == null) {
			return null;
		}

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		final String dateString = StringUtil.fill(
				String.valueOf(calendar.get(Calendar.YEAR)), -4, '0')
				+ StringUtil.fill(
						String.valueOf(calendar.get(Calendar.MONTH) + 1), -2,
						'0')
				+ StringUtil.fill(String.valueOf(calendar.get(Calendar.DATE)),
						-2, '0');
		return dateString;
	}

	public static Date parseYyyyMMddHHmmssSSS(final String dateString)
			throws IllegalArgumentException {
		final Calendar calendar = Calendar.getInstance();

		if (dateString == null || dateString.equals("")) {
			throw new IllegalArgumentException(
					"Invalid String to Parse as Date - dateString was null or empty");
		}

		final int strSize = dateString.length();

		if (strSize < 8) {
			throw new IllegalArgumentException(
					"Invalid String to Parse as Date - dateString invalid string length ("
							+ strSize + ")");
		}

		final String filled = StringUtil.fill(dateString, 17, '0');

		final String yearStr = filled.substring(0, 4);
		final String monthStr = filled.substring(4, 6);
		final String dayStr = filled.substring(6, 8);
		final String hourStr = filled.substring(8, 10);
		final String minuteStr = filled.substring(10, 12);
		final String secondsStr = filled.substring(12, 14);
		final String millisStr = filled.substring(14, 17);

		int year = 0;
		int day = 0;
		int month = 0;
		int hour = 0;
		int minute = 0;
		int seconds = 0;
		int millis = 0;

		try {
			year = Integer.parseInt(yearStr);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + yearStr
					+ "' as a valid year", e);
		}
		try {
			day = Integer.parseInt(dayStr);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + dayStr
					+ "' as a valid day", e);
		}
		try {
			month = Integer.parseInt(monthStr) - 1; // Zero Based Months
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + monthStr
					+ "' as a valid month", e);
		}

		try {
			hour = Integer.parseInt(hourStr);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + hourStr
					+ "' as a valid hour", e);
		}
		try {
			minute = Integer.parseInt(minuteStr);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + minuteStr
					+ "' as a valid minute");
		}
		try {
			seconds = Integer.parseInt(secondsStr);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + secondsStr
					+ "' as a valid seconds");
		}
		try {
			millis = Integer.parseInt(millisStr);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Could not parse '" + millisStr
					+ "' as a valid millis");
		}

		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, day);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, seconds);
		calendar.set(Calendar.MILLISECOND, millis);

		final Date date = calendar.getTime();
		return date;
	}
}
