package io.github.anomal.propsserver.writer;

import java.util.Properties;

public interface PropsWriter {

    /**
     * Send Properties to be persisted under identifier name
     * @param name Properties name/identifier
     * @param props Properties value
     */
    void write(String name, Properties props);
}
