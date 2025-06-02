package com.rxlogix.file;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultipartFileSender {

    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    private static final String CONTENT_TYPE_MULTITYPE_WITH_BOUNDARY = "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY;
    private static final String CONTENT_DISPOSITION_INLINE = "inline";
    private static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment";
    private static final String IF_NONE_MATCH = "If-None-Match";
    private static final String ETAG = "ETag";
    private static final String IF_MATCH = "If-Match";
    private static final String RANGE = "Range";
    private static final String CONTENT_RANGE = "Content-Range";
    private static final String IF_RANGE = "If-Range";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT_RANGES = "Accept-Ranges";
    private static final String BYTES = "bytes";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String IMAGE = "image";
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String BYTES_RANGE_FORMAT = "bytes %d-%d/%d";
    private static final String CONTENT_DISPOSITION_FORMAT = "%s;filename=\"%s\"";
    private static final String BYTES_DINVALID_BYTE_RANGE_FORMAT = "bytes */%d";
    private static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.

    private static final Logger logger = LoggerFactory.getLogger(MultipartFileSender.class);


    File file;
    HttpServletRequest request;
    HttpServletResponse response;
    String disposition = CONTENT_DISPOSITION_INLINE;
    String contentType;
    String fileName;

    private MultipartFileSender() {
    }

    public static MultipartFileSender fromFile(File file) {
        return new MultipartFileSender().setFilepath(file);
    }

    public MultipartFileSender with(HttpServletRequest httpRequest) {
        request = httpRequest;
        return this;
    }

    public MultipartFileSender with(HttpServletResponse httpResponse) {
        response = httpResponse;
        return this;
    }

    public MultipartFileSender withDispositionInline() {
        forceDisposition(CONTENT_DISPOSITION_INLINE);
        return this;
    }

    public MultipartFileSender withDispositionAttachment() {
        forceDisposition(CONTENT_DISPOSITION_ATTACHMENT);
        return this;
    }

    public MultipartFileSender withContentType(String contentType) {
        this.contentType= contentType;
        return this;
    }

    public MultipartFileSender withFileName(String fileName) {
        this.fileName= fileName;
        return this;
    }


    public void serveResource() throws Exception {
        if (response == null || request == null) {
            return;
        }

        if (!Files.exists(file.toPath())) {
            logger.error("File doesn't exist at URI : {}", file.toPath().toAbsolutePath());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long length = file.length();
        if (StringUtils.isEmpty(fileName)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // Validate request headers for caching ---------------------------------------------------

        // If-None-Match header should contain "*" or ETag. If so, then return 304.
        String ifNoneMatch = request.getHeader(IF_NONE_MATCH);
        if (ifNoneMatch != null && HttpUtils.matches(ifNoneMatch, fileName)) {
            response.setHeader(ETAG, fileName); // Required in 304.
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }


        // Validate request headers for resume ----------------------------------------------------

        // If-Match header should contain "*" or ETag. If not, then return 412.
        String ifMatch = request.getHeader(IF_MATCH);
        if (ifMatch != null && !HttpUtils.matches(ifMatch, fileName)) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }


        // Validate and process range -------------------------------------------------------------

        // Prepare some variables. The full Range represents the complete file.
        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<>();

        // Validate and process Range and If-Range headers.
        String range = request.getHeader(RANGE);
        if (range != null) {

            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader(CONTENT_RANGE, String.format(BYTES_DINVALID_BYTE_RANGE_FORMAT, length)); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            String ifRange = request.getHeader(IF_RANGE);
            if (ifRange != null && !ifRange.equals(fileName)) {
                try {
                    long ifRangeTime = request.getDateHeader(IF_RANGE); // Throws IAE if invalid.
                    if (ifRangeTime != -1) {
                        ranges.add(full);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(full);
                }
            }

            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    long start = Range.sublong(part, 0, part.indexOf("-"));
                    long end = Range.sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response.setHeader(CONTENT_RANGE, String.format(BYTES_DINVALID_BYTE_RANGE_FORMAT, length)); // Required in 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    // Add range.
                    ranges.add(new Range(start, end, length));
                }
            }
        }

        logger.debug("Content-Type : {}", contentType);
        // Initialize response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader(CONTENT_TYPE, contentType);
        response.setHeader(ACCEPT_RANGES, BYTES);
        //response.setCharacterEncoding("UTF-8");
        response.setHeader(ETAG, fileName);


        if (StringUtils.isEmpty(disposition)) {
            if (contentType == null) {
                contentType = APPLICATION_OCTET_STREAM;
                disposition = CONTENT_DISPOSITION_ATTACHMENT;
            } else if (!contentType.startsWith(IMAGE)) {
                String accept = request.getHeader(ACCEPT);
                disposition = accept != null && HttpUtils.accepts(accept, contentType) ? CONTENT_DISPOSITION_INLINE : CONTENT_DISPOSITION_ATTACHMENT;
            } else {
                disposition = CONTENT_DISPOSITION_INLINE;
            }
        }
        response.setHeader(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_FORMAT, disposition, fileName));
        logger.debug("Content-Disposition : {}", disposition);
        // Send requested file (part(s)) to client ------------------------------------------------

        // Prepare streams.
        try(FileInputStream input = new FileInputStream(file)) {
            try (ServletOutputStream output = response.getOutputStream()) {
                if (ranges.isEmpty() || ranges.get(0) == full) {

                    // Return full file.
                    logger.debug("Return full file");
                    response.setContentType(contentType);
                    response.setHeader(CONTENT_RANGE, String.format(BYTES_RANGE_FORMAT, full.start, full.end, full.total));
                    response.setContentLengthLong(full.length);
                    Range.copy(input, output, length, full.start, full.length);

                } else if (ranges.size() == 1) {

                    // Return single part of file.
                    Range r = ranges.get(0);
                    logger.debug("Return 1 part of file : from ({}) to ({})", r.start, r.end);
                    response.setContentType(contentType);
                    response.setHeader(CONTENT_RANGE, String.format(BYTES_RANGE_FORMAT, r.start, r.end, r.total));
                    response.setHeader(CONTENT_LENGTH, String.valueOf(r.length));
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                    // Copy single part range.
                    Range.copy(input, output, length, r.start, r.length);

                } else {

                    // Return multiple parts of file.
                    response.setContentType(CONTENT_TYPE_MULTITYPE_WITH_BOUNDARY);
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                    // Cast back to ServletOutputStream to get the easy println methods.

                    // Copy multi part range.
                    for (Range r : ranges) {
                        logger.debug("Return multi part of file : from ({}) to ({})", r.start, r.end);
                        // Add multipart boundary and header fields for every range.
                        output.println();
                        output.println("--" + MULTIPART_BOUNDARY);
                        output.println(CONTENT_TYPE + ": " + contentType);
                        output.println(CONTENT_RANGE + ": " + String.format(BYTES_RANGE_FORMAT, r.start, r.end, r.total));

                        // Copy single part range of multi part range.
                        Range.copy(input, output, length, r.start, r.length);
                    }

                    // End with multipart boundary.
                    output.println();
                    output.println("--" + MULTIPART_BOUNDARY + "--");
                }
            }
        } catch (IOException e) {
            logger.error("IOException occurred while downloading! may be due to cancelled by client error: {} ", e.getMessage(), e);
            throw e;
        } finally {
            try {
                response.flushBuffer();
            } catch (Exception e) {
                logger.error("while closing streams faced errors: {}", e.getMessage(), e);
            }
        }

    }

    private static class Range {
        long start;
        long end;
        long length;
        long total;

        /**
         * Construct a byte range.
         *
         * @param start Start of the byte range.
         * @param end   End of the byte range.
         * @param total Total length of the byte source.
         */
        public Range(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

        public static long sublong(String value, int beginIndex, int endIndex) {
            String substring = value.substring(beginIndex, endIndex);
            return (!substring.isEmpty()) ? Long.parseLong(substring) : -1;
        }

        private static void copy(InputStream input, OutputStream output, long inputSize, long start, long length) throws IOException {

            int read;

            if (inputSize == length) {
                // Write full range.
                input.transferTo(output);
                output.flush();
            } else {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                input.skip(start);
                long toRead = length;

                while ((read = input.read(buffer)) > 0) {
                    if ((toRead -= read) > 0) {
                        output.write(buffer, 0, read);
                        output.flush();
                    } else {
                        output.write(buffer, 0, (int) toRead + read);
                        output.flush();
                        break;
                    }
                }
            }
        }
    }

    private static class HttpUtils {

        /**
         * Returns true if the given accept header accepts the given value.
         *
         * @param acceptHeader The accept header.
         * @param toAccept     The value to be accepted.
         * @return True if the given accept header accepts the given value.
         */
        public static boolean accepts(String acceptHeader, String toAccept) {
            String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
            Arrays.sort(acceptValues);

            return Arrays.binarySearch(acceptValues, toAccept) > -1
                    || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
                    || Arrays.binarySearch(acceptValues, "*/*") > -1;
        }

        /**
         * Returns true if the given match header matches the given value.
         *
         * @param matchHeader The match header.
         * @param toMatch     The value to be matched.
         * @return True if the given match header matches the given value.
         */
        public static boolean matches(String matchHeader, String toMatch) {
            String[] matchValues = matchHeader.split("\\s*,\\s*");
            Arrays.sort(matchValues);
            return Arrays.binarySearch(matchValues, toMatch) > -1
                    || Arrays.binarySearch(matchValues, "*") > -1;
        }
    }

    //** internal setter **//
    private MultipartFileSender setFilepath(File file) {
        this.file = file;
        this.fileName = file.toPath().getFileName().toString();
        return this;
    }

    private void forceDisposition(String disposition) {
        this.disposition = disposition;
    }
    public static void renderFile(File file, String reportFileName, String ext, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        renderFile(file, reportFileName, ext, contentType, httpRequest, httpResponse, false);
    }

    public static void renderFile(File file, String reportFileName, String ext, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean inline) throws Exception {
        try {

            String name = URLEncoder.encode(reportFileName.replaceAll(" ", "_"), StandardCharsets.UTF_8) + "." + ext;
            MultipartFileSender multipartFileSender = MultipartFileSender.fromFile(file)
                    .withContentType(contentType)
                    .withFileName(name);

            if (inline) {
                multipartFileSender.withDispositionInline();
            } else {
                multipartFileSender.withDispositionAttachment();
            }

            multipartFileSender.with(httpRequest)
                    .with(httpResponse)
                    .serveResource();
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}
