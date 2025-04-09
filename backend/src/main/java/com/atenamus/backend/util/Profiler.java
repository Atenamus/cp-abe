package com.atenamus.backend.util;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Aspect
@Component
public class Profiler {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Value("${profiler.log.file}")
    private String logFilePath;

    private BufferedWriter logWriter;

    @PostConstruct
    public void init() {
        try {
            File logFile = new File(logFilePath);
            logFile.getParentFile().mkdirs();

            logWriter = new BufferedWriter(new FileWriter(logFile, true));

            if (logFile.length() == 0) {
                logWriter.write("timestamp,endpoint,method,duration_ms,status\n");
                logWriter.flush();
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize log file" + e.getMessage());
        }
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    public Object profileEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
                .getRequestAttributes())).getRequest();
        String endpoint = request.getRequestURI();
        String method = request.getMethod();

        long startTime = System.currentTimeMillis();
        int status = 200;

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            status = 500;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logEndpointCall(endpoint, method, duration, status);
        }
    }
//    @Around("execution(* com.atenamus.*.service.*.*Key*(..)) || " +
//            "execution(* com.example.*.service.*.*key*(..)) || " +
//            "execution(* com.example.*.service.*.*ABE*(..))")
//    public Object profileKeyOperation(ProceedingJoinPoint joinPoint) throws Throwable {
//        String operation = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." +
//                joinPoint.getSignature().getName();
//
//        long startTime = System.currentTimeMillis();
//
//        try {
//            return joinPoint.proceed();
//        } finally {
//            long duration = System.currentTimeMillis() - startTime;
//            logKeyOperation(operation, duration);
//        }
//    }

    private synchronized void logEndpointCall(String endpoint, String method,
                                              long duration, int status) {
        try {
            String timestamp = LocalDateTime.now().format(dateFormat);

            logWriter.write(String.format("%s,%s,%s,%d,%d\n",
                    timestamp, endpoint, method, duration, status));
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to log endpoint call: " + e.getMessage());
        }
    }

    private synchronized void logKeyOperation(String operation, long duration) {
        try {
            String timestamp = LocalDateTime.now().format(dateFormat);

            logWriter.write(String.format("%s,%s,INTERNAL,%d,N/A,N/A\n",
                    timestamp, operation, duration));
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("Failed to log key operation: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
