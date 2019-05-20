package org.zalando.logbook;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.*;

public class RegexpPathFilterTest {

	@Test
	public void testFilter1() {
		String path = "/profiles/ORG/EPOST/generate-password";
		String expr = "^\\/profiles\\/.*\\/(.*)\\/generate-password$";
		
		Pattern p = Pattern.compile(expr);
		
		RegexpPathFilter regexpPathUriFilter = new RegexpPathFilter(p);
		
		String result = regexpPathUriFilter.filter(path);
		assertThat(result, not(containsString("EPOST")));
        assertThat(result, containsString("XXX"));
	}

	@Test
	public void testFilter2() {
		String path = "/customers/1/epost@noe.no/action";
		String expr = "^\\/customers\\/.*\\/(.*)\\/action$";
		
		Pattern p = Pattern.compile(expr);
		
		RegexpPathFilter regexpPathUriFilter = new RegexpPathFilter(p);
		
		String result = regexpPathUriFilter.filter(path);
        assertThat(result, not(containsString("epost@noe.no")));
		assertThat(result, is("/customers/1/XXX/action"));
	}
	
    @Test
    public void testFilterStart() {
        String path = "/a/b/c/d/e";
        String expr = "^(\\/a)\\/.*\\/c\\/.*\\/e$";
        
        Pattern p = Pattern.compile(expr);
        
        RegexpPathFilter regexpPathUriFilter = new RegexpPathFilter(p);
        
        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("XXX/b/c/d/e"));
    }
    
    @Test
    public void testFilterEnd() {
        String path = "/a/b/c/d/e";
        String expr = "^\\/a\\/.*\\/c\\/.*\\/(.*)$";
        
        Pattern p = Pattern.compile(expr);
        
        RegexpPathFilter regexpPathUriFilter = new RegexpPathFilter(p);
        
        String result = regexpPathUriFilter.filter(path);
        assertThat(result, is("/a/b/c/d/XXX"));
    }    
	
    @Test
    public void testFilterNoneReturnsSameInstance() {
        String path = "/a/b/c/d/e";
        String expr = "^\\/e\\/(.*)\\/c\\/(.*)\\/a$";
        
        Pattern p = Pattern.compile(expr);
        
        RegexpPathFilter regexpPathUriFilter = new RegexpPathFilter(p);
        
        String result = regexpPathUriFilter.filter(path);
        assertThat(result, sameInstance(path));
    }
	
	@Test
	public void testFilterMultiple() {
		String path = "/a/b/c/d/e";
		String expr = "^\\/a\\/(.*)\\/c\\/(.*)\\/e$";
		
		Pattern p = Pattern.compile(expr);
		
		RegexpPathFilter regexpPathUriFilter = new RegexpPathFilter(p);
		
		String result = regexpPathUriFilter.filter(path);
	    assertThat(result, is("/a/XXX/c/XXX/e"));
	}
	
}
