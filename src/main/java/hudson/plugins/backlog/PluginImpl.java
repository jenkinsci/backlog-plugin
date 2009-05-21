package hudson.plugins.backlog;

import hudson.Plugin;
import hudson.model.Jobs;

/**
 * Entry point of for the Backlog plugin.
 * 
 * <p>
 * FIXME: HudsonのPlugin機構の変更のため、このクラスは不要になっているはずだが、現状これがないと動作しないっぽい
 * </p>
 *
 * @see http://d.hatena.ne.jp/cactusman/20090328/p1
 * @author yamamoto
 */
public class PluginImpl extends Plugin {
	
	private final BacklogChangelogAnnotator annotator = new BacklogChangelogAnnotator();

    @Override
    public void start() throws Exception {
        annotator.register();
        Jobs.PROPERTIES.add(BacklogProjectProperty.DESCRIPTOR);
    }

    public void stop() throws Exception {
        annotator.unregister();
    }
    
}
