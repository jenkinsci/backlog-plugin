package hudson.plugins.backlog;

import hudson.Extension;
import hudson.MarkupText;
import hudson.MarkupText.SubText;
import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link ChangeLogAnnotator} that picks up Backlog issue KEY.
 * 
 * @author dragon3
 */
@Extension
public class BacklogChangelogAnnotator extends ChangeLogAnnotator {

	private static final Log LOG = LogFactory
			.getLog(BacklogChangelogAnnotator.class);

	public static final Pattern ISSUE_KEY_PATTERN = Pattern
			.compile("(?:(?<![/A-Z0-9a-z-])([A-Z0-9]+-[1-9][0-9]*)|\\[\\[([A-Z0-9]+-[1-9][0-9]*)\\]\\])");

	@Override
	public void annotate(AbstractBuild<?, ?> build, Entry change,
			MarkupText text) {
		BacklogProjectProperty bpp = build.getProject().getProperty(
				BacklogProjectProperty.class);
		if (bpp == null || bpp.getSpaceURL() == null) {
			LOG.debug("BacklogProjectProperty is null or BacklogProjectProperty's spaceURL is null");
			return;
		}
		annotate(bpp.getSpaceURL(), text);
	}

	void annotate(String spaceURL, MarkupText text) {
		Matcher m = ISSUE_KEY_PATTERN.matcher(text.getText());
		List<SubText> r = new ArrayList<SubText>();

		while (m.find()) {
			SubText st = text.new SubText(m, 0);
			r.add(st);
		}

		for (SubText token : r) {
			LOG.debug("token=" + token.getText());
			token.surroundWith("<a href=\"" + spaceURL + "view/$1$2\">", "</a>");
		}
	}
}
