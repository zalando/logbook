package org.zalando.logbook.servlet.example;

/*
 * #%L
 * logbook
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExampleController {

    @RequestMapping("/sync")
    public ResponseEntity<Message> message() {
        final Message message = new Message();
        message.setValue("Hello, world!");
        return ResponseEntity.ok(message);
    }

    @RequestMapping("/async")
    public Callable<ResponseEntity<Message>> returnMessage() {
        return () -> {
            final Message message = new Message();
            message.setValue("Hello, world!");

            return ResponseEntity.ok(message);
        };
    }

    @RequestMapping("/empty")
    public void empty() {

    }

    @RequestMapping("/error")
    public void error() {
        throw new UnsupportedOperationException();
    }
    
    @RequestMapping(value = "/read-byte", produces = MediaType.TEXT_PLAIN_VALUE)
    public void readByte(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final ServletInputStream input = request.getInputStream();
        final ServletOutputStream output = response.getOutputStream();
        
        while (true) {
            final int read = input.read();
            if (read == -1) {
                break;
            }
            output.write(read);
        }
    }
    
    @RequestMapping(value = "/read-bytes", produces = MediaType.TEXT_PLAIN_VALUE)
    public void readBytes(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        final ServletInputStream input = request.getInputStream();
        final ServletOutputStream output = response.getOutputStream();

        final byte[] buffer = new byte[1];
        
        while (true) {
            final int read = input.read(buffer);
            if (read == -1) {
                break;
            }
            output.write(buffer);
        }
    }
    
    @RequestMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public void stream(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        ByteStreams.copy(request.getInputStream(), response.getOutputStream());
    }
    
    @RequestMapping(value = "/reader", produces = MediaType.TEXT_PLAIN_VALUE)
    public void reader(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try (PrintWriter writer = response.getWriter()) {
            CharStreams.copy(request.getReader(), writer);
        }
    }

    @RequestMapping("/unauthorized")
    public Callable<ResponseEntity<Message>> unauthorized() {
        return () -> {
            final Message message = new Message();
            message.setValue("Hello, world!");
            return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
        };
    }

}
