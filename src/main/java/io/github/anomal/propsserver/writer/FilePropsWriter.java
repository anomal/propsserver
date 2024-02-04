package io.github.anomal.propsserver.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class FilePropsWriter implements PropsWriter {

    private final static int CORES = Runtime.getRuntime().availableProcessors();

    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newFixedThreadPool(CORES,
            r -> new Thread(r, getThreadName(threadNumber.getAndIncrement())));
    private volatile Map<String, BlockingQueue<PropsFile>> threadToQueue = new ConcurrentHashMap<>();

    @Autowired
    public FilePropsWriter(@Value("${outputdir}") String outputDirectory) {
        if (!Files.exists(Paths.get(outputDirectory))) {
            System.err.println(String.format("Output directory %s does not exist", outputDirectory));
            System.exit(1);
        } else if (!(new File(outputDirectory).canWrite())){
            System.err.println(String.format("Output directory %s is not writable", outputDirectory));
            System.exit(1);
        }

        log.debug("CORES: {}", CORES);

        for (int i = 0; i < CORES; i++){
            BlockingQueue blockingQueue = new LinkedBlockingQueue();
            threadToQueue.put(getThreadName(i), blockingQueue);
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                log.debug("threadName is {}", threadName);
                BlockingQueue<PropsFile> bq = blockingQueue;
                while (true){
                    PropsFile propsFile = bq.take();
                    log.debug("Writing {} with {}", propsFile.fileName, propsFile.props);
                    String outputPath = outputDirectory + File.separator + propsFile.fileName;
                    try (OutputStream outputStream = new FileOutputStream(outputPath)) {
                        propsFile.props.store(outputStream, propsFile.fileName);
                        log.debug("Wrote {}", outputPath);
                    } catch (RuntimeException e) {
                        log.error(String.format("Failed to write to %s", outputPath), e);
                    }
                }
            });
        }

    }

    @Override
    public void write(String name, Properties props) {
        try {
            String filename = name + ".properties";
            int threadId = Math.abs(filename.hashCode()) % CORES;
            PropsFile propsFile = new PropsFile(filename, props);
            String threadName = getThreadName(threadId);
            log.debug("Using {}", threadName);
            BlockingQueue blockingQueue = threadToQueue.get(threadName);
            blockingQueue.put(propsFile);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getThreadName(int threadId){
        return "Thread-" + threadId;
    }
    private static class PropsFile {
        public final String fileName;
        public final Properties props;

        public PropsFile(String fileName, Properties props){
            this.fileName = fileName;
            this.props = props;
        }
    }
}
