package com.github.catvod.utils;

import com.github.catvod.crawler.SpiderDebug;
import okhttp3.*;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdException;

public class HttpFetcher {

    private static OkHttpClient client = new OkHttpClient();

    public HttpFetcher() {
        client = new OkHttpClient();
    }

    public HttpFetcher(OkHttpClient client) {
        HttpFetcher.client = client;
    }

    /**
     * 发起 HTTP 请求并解压响应内容（支持 zstd/gzip）
     */
    public static String fetchAndDecompress(String url, HashMap<String, String> headers) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            ResponseBody body = response.body();
            if (body == null) throw new IOException("Empty response body");

            String contentEncoding = response.header("Content-Encoding", "").trim().toLowerCase();
            byte[] rawBytes = body.bytes();

            // 调试：保存原始数据到文件以便检查
            Files.write(Paths.get("raw_response.bin"), rawBytes);
            SpiderDebug.log("Content-Encoding: " + contentEncoding);
            SpiderDebug.log("Raw data size: " + rawBytes.length + " bytes");

            byte[] decompressed;
            if ("zstd".equals(contentEncoding)) {
                try {
                    long decompressedSize = Zstd.decompressedSize(rawBytes);
                    SpiderDebug.log("Estimated decompressed size: " + decompressedSize);

                    decompressed = decompressZstd(rawBytes);
                    SpiderDebug.log("Successfully decompressed Zstd data");
                } catch (Exception e) {
                    throw new IOException("Failed to decompress Zstd data", e);
                }
            } else if ("gzip".equals(contentEncoding)) {
                decompressed = Util.decompressGzip(rawBytes);
            } else {
                decompressed = rawBytes;
            }

            return new String(decompressed, StandardCharsets.UTF_8);
        }
    }

    /**
     * 解压 Zstandard 压缩数据
     */
    private static byte[] decompressZstd(byte[] compressedData) throws IOException {
        try {
            // 方法1：尝试标准解压（适用于完整帧数据）
            return Zstd.decompress(compressedData);
        } catch (ZstdException e) {
            // 方法2：手动缓冲解压（处理未知内容大小）
            return decompressWithDynamicBuffer(compressedData);
        }
    }

    /**
     * 使用流式API动态解压 Zstd 数据
     */
    private static byte[] decompressWithDynamicBuffer(byte[] compressedData) throws IOException {
        int bufferSize = Math.max(compressedData.length * 3, 1024 * 1024); // 至少1MB
        byte[] buffer = new byte[bufferSize];

        try (ZstdInputStream zis = new ZstdInputStream(new ByteArrayInputStream(compressedData))) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] tempBuffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = zis.read(tempBuffer)) != -1) {
                output.write(tempBuffer, 0, bytesRead);
            }

            return output.toByteArray();
        } catch (Exception e) {
            // 方法3：回退到带大小提示的解压
            return decompressWithSizeHint(compressedData, bufferSize * 2);
        }
    }

    /**
     * 使用大小提示解压 Zstd 数据
     */
    private static byte[] decompressWithSizeHint(byte[] compressedData, int suggestedSize) {
        byte[] buffer = new byte[suggestedSize];
        long resultSize = Zstd.decompressByteArray(
                buffer, 0, buffer.length,
                compressedData, 0, compressedData.length
        );

        if (Zstd.isError(resultSize)) {
            throw new RuntimeException("Zstd error: " + Zstd.getErrorName(resultSize));
        }

        return Arrays.copyOf(buffer, (int) resultSize);
    }

    /**
     * 静态工具类用于 GZIP 解压
     */
    public static class Util {
        public static byte[] decompressGzip(byte[] compressedData) throws IOException {
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressedData));
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0) {
                    output.write(buffer, 0, len);
                }
                return output.toByteArray();
            }
        }
    }
}