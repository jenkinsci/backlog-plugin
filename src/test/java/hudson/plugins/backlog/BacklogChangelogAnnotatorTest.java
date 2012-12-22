package hudson.plugins.backlog;

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
			String src = "[[BLG_2-123]]について、対応しました。";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"<a href=\"https://backlog.backlog.jp/view/BLG_2-123\">[[BLG_2-123]]</a>について、対応しました。",
					text.toString(false));
		}
		{
			String src = "BLG_2-123について、対応しました。";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"<a href=\"https://backlog.backlog.jp/view/BLG_2-123\">BLG_2-123</a>について、対応しました。",
					text.toString(false));
		}
		{
			String src = "IEの場合に表示がおかしかったのを修正（BLG_2-1384）";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"IEの場合に表示がおかしかったのを修正（<a href=\"https://backlog.backlog.jp/view/BLG_2-1384\">BLG_2-1384</a>）",
					text.toString(false));
		}
		{
			String src = "2009-05-15 IEの場合に表示がおかしかったのを修正（BLG_2-1384）";
			MarkupText text = new MarkupText(src);
			annotator.annotate("https://backlog.backlog.jp/", text);
			Assert.assertEquals(
					"2009-05-15 IEの場合に表示がおかしかったのを修正（<a href=\"https://backlog.backlog.jp/view/BLG_2-1384\">BLG_2-1384</a>）",
					text.toString(false));
		}
	}

	@Test
	public final void testIssuePattern() {
		{
			String src = "[[BLG_2-123]]について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN
					.matcher(src);
			while (m.find()) {
				System.out.println("groupCount=" + m.groupCount());
				for (int i = 0; i < m.groupCount(); i++) {
					System.out.println(m.group(i));
				}
				System.out.println(m.group(2));
			}
		}
		{
			String src = "BLG_2-123について、対応しました。";
			Matcher m = BacklogChangelogAnnotator.ISSUE_KEY_PATTERN
					.matcher(src);
			while (m.find()) {
				System.out.println("groupCount=" + m.groupCount());
				for (int i = 0; i < m.groupCount(); i++) {
					System.out.println(m.group(i));
				}
			}
		}
	}
}
