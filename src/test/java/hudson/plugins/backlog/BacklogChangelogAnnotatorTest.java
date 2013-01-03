package hudson.plugins.backlog;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import hudson.MarkupText;

import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BacklogChangelogAnnotatorTest {

	private BacklogChangelogAnnotator annotator;

	@Before
	public final void setup() {
		annotator = new BacklogChangelogAnnotator();
	}

	@Test
	public final void testAnnotate() {
		{
			String src = "[[BLG-123]]について、対応しました。";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"<a href=\"https://backlog.backlog.jp/view/BLG-123\">[[BLG-123]]</a>について、対応しました。",
					text.toString(false));
		}

		{
			String src = "BLG-123について、対応しました。";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"<a href=\"https://backlog.backlog.jp/view/BLG-123\">BLG-123</a>について、対応しました。",
					text.toString(false));
		}

		{
			String src = "IEの場合に表示がおかしかったのを修正（BLG-1384）";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"IEの場合に表示がおかしかったのを修正（<a href=\"https://backlog.backlog.jp/view/BLG-1384\">BLG-1384</a>）",
					text.toString(false));
		}
		{
			String src = "2009-05-15 IEの場合に表示がおかしかったのを修正（BLG-1384）";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"2009-05-15 IEの場合に表示がおかしかったのを修正（<a href=\"https://backlog.backlog.jp/view/BLG-1384\">BLG-1384</a>）",
					text.toString(false));
		}
	}

	@Test
	public final void testIssuePattern() {
		{
			String src = "[[BLG-123]]について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(true));
			assertThat(m.groupCount(), is(2));
			assertThat(m.group(0), is("[[BLG-123]]"));
			assertThat(m.group(2), is("BLG-123"));
		}
		{
			String src = "BLG-123について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(true));
			assertThat(m.groupCount(), is(2));
			assertThat(m.group(1), is("BLG-123"));
		}
	}

	@Test
	public final void testIssuePattern_WithUnderscore() {
		{
			String src = "[[BLG_2-123]]について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(true));
			assertThat(m.groupCount(), is(2));
			assertThat(m.group(0), is("[[BLG_2-123]]"));
			assertThat(m.group(2), is("BLG_2-123"));
		}
		{
			String src = "BLG_2-123について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(true));
			assertThat(m.groupCount(), is(2));
			assertThat(m.group(1), is("BLG_2-123"));
		}
	}

	@Test
	public final void testIssuePattern_NoMatch() {
		{
			String src = "[[BLG]]について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(false));
		}
		{
			String src = "[[123]]について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(false));
		}
		{
			String src = "[[BLG-NG-123]]について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN.matcher(src);

			assertThat(m.find(), is(false));
		}
	}
}
