package leetcode.clone.leetcode.service;


import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class DockerLogHandler implements ResultCallback<Frame> {

    public static StringBuilder output = new StringBuilder();

    @Override
    public void close() throws IOException {
        log.info("Docker process is closed.");
    }
    @Override
    public void onStart(Closeable closeable) {
        log.info("Docker process started.");
    }
    @Override
    public void onNext(Frame object) {
        if(object.getStreamType() == StreamType.STDOUT){
            output.append(new String(object.getPayload()).trim()).append(System.lineSeparator());
        }

        if(object.getStreamType() == StreamType.STDERR){
            log.error(new String(object.getPayload()).trim());
        }
    }
    @Override
    public void onError(Throwable throwable) {
        log.error(throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.info("Docker process is completed.");
    }
}
