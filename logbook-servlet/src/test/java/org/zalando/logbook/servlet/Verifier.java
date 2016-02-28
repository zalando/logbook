package org.zalando.logbook.servlet;

import com.google.common.base.Joiner;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import java.util.HashSet;
import java.util.Set;

final class Verifier extends TestWatcher {

    private final Set<Answer> answers = new HashSet<>();

    public Stubber doAnswer(final Answer answer) {
        final Answer wrapped = new MyAnswer(answer);
        answers.add(wrapped);
        return Mockito.doAnswer(wrapped);
    }

    private class MyAnswer implements Answer {

        private final Answer answer;

        public MyAnswer(final Answer answer) {
            this.answer = answer;
        }

        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
            try {
                return answer.answer(invocation);
            } finally {
                answers.remove(this);
            }
        }

        @Override
        public String toString() {
            return answer.toString();
        }

    }

    @Override
    protected void finished(final Description description) {
        if (!answers.isEmpty()) {
            throw new AssertionError("Missing " + Joiner.on(", ").join(answers));
        }
    }

}
