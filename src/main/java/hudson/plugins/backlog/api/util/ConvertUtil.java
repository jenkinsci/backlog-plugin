package hudson.plugins.backlog.api.util;

import hudson.plugins.backlog.api.entity.Issue;
import hudson.plugins.backlog.api.entity.Project;
import hudson.plugins.backlog.api.entity.User;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for convertion.
 * 
 * @author ikikko
 */
public class ConvertUtil {

	private ConvertUtil() {
	}

	/**
	 * Convert from XML-RPC response to {@link Project}.
	 */
	@SuppressWarnings("unchecked")
	public static Project responseToProject(final Object response) {
		if (response == null) {
			return null;
		}

		final Map<String, Object> map = (Map<String, Object>) response;
		if (map.isEmpty()) {
			return null;
		}

		final int id = ((Integer) map.get("id")).intValue();
		final String name = (String) map.get("name");
		final String key = (String) map.get("key");
		final String url = (String) map.get("url");

		return new Project(id, name, key, url);
	}

	/**
	 * Convert from XML-RPC response to {@link Issue}.
	 */
	@SuppressWarnings("unchecked")
	public static Issue responseToIssue(final Object response) {
		if (response == null) {
			return null;
		}

		final Map<String, Object> map = (Map<String, Object>) response;
		if (map.isEmpty()) {
			return null;
		}

		final String key = (String) map.get("key");
		final String summary = (String) map.get("summary");
		final String description = (String) map.get("description");
		final String url = (String) map.get("url");

		final Date startDate = responseToDate(map.get("start_date"));
		final Date dueDate = responseToDate(map.get("due_date"));
		final Double estimatedHours = (Double) map.get("estimated_hours");
		final Double actualHours = (Double) map.get("actual_hours");

		String issueType = null;
		final Map<String, Object> issueTypeObj = (Map<String, Object>) map
				.get("issueType");
		if (issueTypeObj != null) {
			issueType = (String) issueTypeObj.get("name");
		}

		String[] components = null;
		final Object[] componentObjs = (Object[]) map.get("components");
		if (componentObjs != null) {
			components = new String[componentObjs.length];
			for (int i = 0; i < componentObjs.length; i++) {
				final Map<String, Object> componentObj = (Map<String, Object>) componentObjs[i];
				components[i] = (String) componentObj.get("name");
			}
		}
		String[] affectsVersions = null;
		final Object[] affectsVersionObjs = (Object[]) map.get("versions");
		if (affectsVersionObjs != null) {
			affectsVersions = new String[affectsVersionObjs.length];
			for (int i = 0; i < affectsVersionObjs.length; i++) {
				final Map<String, Object> affectsVersionObj = (Map<String, Object>) affectsVersionObjs[i];
				affectsVersions[i] = (String) affectsVersionObj.get("name");
			}
		}
		String[] milestoneVersions = null;
		final Object[] milestoneObjs = (Object[]) map.get("milestones");
		if (milestoneObjs != null) {
			milestoneVersions = new String[milestoneObjs.length];
			for (int i = 0; i < milestoneObjs.length; i++) {
				final Map<String, Object> milestoneObj = (Map<String, Object>) milestoneObjs[i];
				milestoneVersions[i] = (String) milestoneObj.get("name");
			}
		}

		int priority = 0;
		final Map<String, Object> priorityObj = (Map<String, Object>) map
				.get("priority");
		if (priorityObj != null) {
			priority = (Integer) priorityObj.get("id");
		}
		String resolution = null;
		final Map<String, Object> resolutionObj = (Map<String, Object>) map
				.get("resolution");
		if (resolutionObj != null) {
			resolution = (String) resolutionObj.get("name");
		}
		String status = null;
		final Map<String, Object> statusObj = (Map<String, Object>) map
				.get("status");
		if (statusObj != null) {
			status = (String) statusObj.get("name");
		}

		final User assignerUser = responseToUser(map.get("assigner"));
		final User createdUser = responseToUser(map.get("created_user"));

		final Date createdOn = responseToDatetime(map.get("created_on"));
		final Date updatedOn = responseToDatetime(map.get("updated_on"));

		return new Issue(key, summary, description, url, startDate, dueDate,
				estimatedHours, actualHours, issueType, components,
				affectsVersions, milestoneVersions, priority, resolution,
				status, assignerUser, createdUser, createdOn, updatedOn);
	}

	/**
	 * Convert from XML-RPC response to {@link Date}.
	 */
	public static Date responseToDate(final Object value) {
		if (value == null || value.equals("")) {
			return null;
		}
		if (!(value instanceof String)) {
			throw new IllegalArgumentException("illegal date type " + value);
		}
		final String dateString = (String) value;
		if (dateString.length() != 8) {
			throw new IllegalArgumentException("illegal date format " + value);
		}

		try {
			final Date date = DateUtil.parseYyyyMMddHHmmssSSS(dateString);
			return date;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Convert from XML-RPC response to {@link Date}.
	 */
	public static Date responseToDatetime(final Object value) {
		if (value == null) {
			return null;
		}
		if (!(value instanceof String)) {
			throw new IllegalArgumentException("illegal date type " + value);
		}
		final String dateString = (String) value;
		if (dateString.length() != 14) {
			throw new IllegalArgumentException("illegal date format " + value);
		}

		try {
			final Date date = DateUtil.parseYyyyMMddHHmmssSSS(StringUtil.fill(
					dateString, 16, '0'));
			return date;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Convert from XML-RPC response to {@link User}.
	 */
	@SuppressWarnings("unchecked")
	public static User responseToUser(final Object value) {
		if (value == null) {
			return null;
		}

		final Map<String, Object> map = (Map<String, Object>) value;
		if (map.isEmpty()) {
			return null;
		}

		final String name = (String) map.get("name");
		final int id = ((Integer) map.get("id")).intValue();
		final Date updatedOn = responseToDatetime(map.get("updated_on"));
		final User user = new User(name, id, updatedOn);

		return user;
	}

	/**
	 * Convert from {@link Issue} to XML-RPC request.
	 */
	public static Map<String, Object> issueToRequest(final Issue issue) {
		final Map<String, Object> request = new HashMap<String, Object>();

		if (issue.getSummary() != null) {
			request.put("summary", issue.getSummary());
		}
		if (issue.getDescription() != null) {
			request.put("description", issue.getDescription());
		}

		if (issue.getStartDate() != null) {
			request.put("start_date",
					DateUtil.formatYyyyMMdd(issue.getStartDate()));
		}
		if (issue.getDueDate() != null) {
			request.put("due_date", DateUtil.formatYyyyMMdd(issue.getDueDate()));
		}
		if (issue.getEstimatedHours() != null) {
			request.put("estimated_hours", issue.getEstimatedHours());
		}
		if (issue.getActualHours() != null) {
			request.put("actual_hours", issue.getActualHours());
		}

		if (issue.getIssueType() != null) {
			request.put("issueType", issue.getIssueType());
		}

		if (issue.getComponents() != null) {
			request.put("component", Arrays.asList(issue.getComponents()));
		}
		if (issue.getAffectsVersions() != null) {
			request.put("version", Arrays.asList(issue.getAffectsVersions()));
		}
		if (issue.getMilestoneVersions() != null) {
			request.put("milestone",
					Arrays.asList(issue.getMilestoneVersions()));
		}

		if (issue.getPriority() == 0) {
			request.put("priority", issue.getPriority());
		}
		if (issue.getAssignerUser() != null) {
			request.put("assignerId", issue.getAssignerUser().getId());
		}

		return request;
	}

}
