package hudson.plugins.backlog.api.entity;

import java.util.Arrays;
import java.util.Date;

public class Issue {

	public Issue() {
	}

	public Issue(final String key, final String summary,
			final String description, final String url, final Date startDate,
			final Date dueDate, final Double estimatedHours,
			final Double actualHours, final String issueType,
			final String[] components, final String[] affectsVersions,
			final String[] milestoneVersions, final Priority priority,
			final String resolution, final String status,
			final User assignerUser, final User createdUser,
			final Date createdOn, final Date updatedOn) {
		this.key = key;
		this.summary = summary;
		this.description = description;
		this.url = url;
		this.startDate = startDate;
		this.dueDate = dueDate;
		this.estimatedHours = estimatedHours;
		this.actualHours = actualHours;
		this.issueType = issueType;
		this.components = components;
		this.affectsVersions = affectsVersions;
		this.milestoneVersions = milestoneVersions;
		this.priority = priority;
		this.resolution = resolution;
		this.status = status;
		this.assignerUser = assignerUser;
		this.createdUser = createdUser;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
	}

	/** Issue key */
	private String key;

	/** summary */
	private String summary;

	/** description */
	private String description;

	/** URL */
	private String url;

	/** Start date */
	private Date startDate;

	/** Due date */
	private Date dueDate;

	/** Estimated hour */
	private Double estimatedHours;

	/** Actual hour */
	private Double actualHours;

	/** Issue type */
	private String issueType;

	/** Component */
	private String[] components;

	/** Affects version */
	private String[] affectsVersions;

	/** Milestone */
	private String[] milestoneVersions;

	/** Priority */
	private Priority priority;

	/** Resolution */
	private String resolution;

	/** Status */
	private String status;

	/** Assigner user */
	private User assignerUser;

	/** Created user */
	private User createdUser;

	/** Created date */
	private Date createdOn;

	/** Updated date */
	private Date updatedOn;

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(final Date dueDate) {
		this.dueDate = dueDate;
	}

	public Double getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(final Double estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

	public Double getActualHours() {
		return actualHours;
	}

	public void setActualHours(final Double actualHours) {
		this.actualHours = actualHours;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(final String issueType) {
		this.issueType = issueType;
	}

	public String[] getComponents() {
		return components;
	}

	public void setComponents(final String[] components) {
		this.components = components;
	}

	public String[] getAffectsVersions() {
		return affectsVersions;
	}

	public void setAffectsVersions(final String[] affectsVersions) {
		this.affectsVersions = affectsVersions;
	}

	public String[] getMilestoneVersions() {
		return milestoneVersions;
	}

	public void setMilestoneVersions(final String[] milestoneVersions) {
		this.milestoneVersions = milestoneVersions;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(final Priority priority) {
		this.priority = priority;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(final String resolution) {
		this.resolution = resolution;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public User getAssignerUser() {
		return assignerUser;
	}

	public void setAssignerUser(final User assignerUser) {
		this.assignerUser = assignerUser;
	}

	public User getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(final User createdUser) {
		this.createdUser = createdUser;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(final Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(final Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	@Override
	public String toString() {
		return "Issue [actualHours=" + actualHours + ", affectsVersions="
				+ Arrays.toString(affectsVersions) + ", assignerUser="
				+ assignerUser + ", components=" + Arrays.toString(components)
				+ ", createdOn=" + createdOn + ", createdUser=" + createdUser
				+ ", description=" + description + ", dueDate=" + dueDate
				+ ", estimatedHours=" + estimatedHours + ", issueType="
				+ issueType + ", key=" + key + ", milestoneVersions="
				+ Arrays.toString(milestoneVersions) + ", priority=" + priority
				+ ", resolution=" + resolution + ", startDate=" + startDate
				+ ", status=" + status + ", summary=" + summary
				+ ", updatedOn=" + updatedOn + ", url=" + url + "]";
	}

}
