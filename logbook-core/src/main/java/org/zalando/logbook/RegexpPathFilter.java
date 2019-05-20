package org.zalando.logbook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Filter/replace by regular expression group. All groups are replaced.
 * <br><br>
 * Thread safe.
 */

public class RegexpPathFilter implements PathFilter {

	private static final String FILTERED = "XXX";
	
	private final Pattern pattern;

	public RegexpPathFilter(String str) {
		this(Pattern.compile(str));
	}

	public RegexpPathFilter(Pattern pattern) {
		this.pattern = pattern;
	}
	
	public String filter(String path) {
		Matcher m = pattern.matcher(path);
		if (m.matches()) {
			StringBuilder builder = new StringBuilder(path.length() * 2);

			int start = 0;
			for(int i = 0; i < m.groupCount(); i++) {

				int captureStart = m.start(i + 1);
				if(start < captureStart) {
					builder.append(path, start, captureStart);
				}
				
				builder.append(FILTERED);
				
				start = m.end(i + 1);
			}
			
			if(start < path.length()) {
				builder.append(path, start, path.length());
			}
			
			return builder.toString();
		}
		
		return path;
	}
	
	
}
