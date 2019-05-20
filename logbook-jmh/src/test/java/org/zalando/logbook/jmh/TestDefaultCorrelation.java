package org.zalando.logbook.jmh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
public class TestDefaultCorrelation {

	@Test
	public void testGetters() {
		DefaultPrecorrelation precorrelation = new DefaultPrecorrelation("id", null);
		assertThat(precorrelation.getId(), is("id"));
	}
}
