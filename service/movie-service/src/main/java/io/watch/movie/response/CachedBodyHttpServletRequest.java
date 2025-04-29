package io.watch.movie.response;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedBody = requestInputStream.readAllBytes();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        return new BufferedReader((new InputStreamReader(byteArrayInputStream)));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // Not needed
        }

        @Override
        public int read() {
            return inputStream.read();
        }
    }
}
