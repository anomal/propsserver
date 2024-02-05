package io.github.anomal.propsserver.api;

import io.github.anomal.propsserver.writer.PropsWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

@RestController
@Slf4j
public class PropsController {

    @Autowired
    private PropsWriter propsWriter;
    @PostMapping("/api/v1/file/{name}")
    public ResponseEntity<Void> post(@PathVariable("name") String name, @RequestBody Properties props){
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            log.debug("Invalid name path variable: {}", name);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            propsWriter.write(name, props);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }
}
