package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UrlToPdfConverter {

    public static void main(String[] args) {
        // 기본값 설정 (기본적으로 Google 메인 페이지를 변환)
        String url = "https://www.google.com";
        String outputPdf = "output.pdf";

        if (args.length >= 1) {
            url = args[0];
        }
        if (args.length >= 2) {
            outputPdf = args[1];
        }

        System.out.println("=== URL to PDF Converter (No External Libraries) ===");
        System.out.println("Processing URL: " + url);
        System.out.println("Output File: " + outputPdf);

        try {
            // 브라우저 실행 파일 찾기 (Edge 또는 Chrome)
            String browserPath = findBrowserPath();
            if (browserPath == null) {
                System.err.println(
                        "Error: Could not find Microsoft Edge or Google Chrome installed in standard locations.");
                System.err.println("Please install a browser or add it to your System PATH.");
                return;
            }
            System.out.println("Using Browser: " + browserPath);

            // PDF 변환 명령어 실행
            convertToPdf(browserPath, url, outputPdf);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String findBrowserPath() {
        // 일반적인 윈도우 설치 경로 확인
        List<String> potentialPaths = Arrays.asList(
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");

        for (String path : potentialPaths) {
            File file = new File(path);
            if (file.exists() && file.canExecute()) {
                return path;
            }
        }
        return null;
    }

    private static void convertToPdf(String browserPath, String url, String outputPdfPath)
            throws IOException, InterruptedException {
        // 출력 파일 절대 경로
        File outputFile = new File(outputPdfPath);

        // 명령어 구성: browser --headless --disable-gpu --print-to-pdf=path url
        // 주의: --print-to-pdf 옵션은 절대경로를 사용하는 것이 안전합니다.
        List<String> command = new ArrayList<>();
        command.add(browserPath);
        command.add("--headless"); // GUI 없이 실행
        command.add("--disable-gpu"); // GPU 가속 비활성화 (헤드리스 모드 안정성)
        command.add("--run-all-compositor-stages-before-draw"); // 레이아웃 렌더링 완료 대기 (중요)
        command.add("--no-pdf-header-footer"); // 머리글/바닥글 제거 (선택사항)
        command.add("--print-to-pdf=" + outputFile.getAbsolutePath());
        command.add(url);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // 에러 출력을 표준 출력과 합침

        System.out.println("Executing headless browser command...");
        Process process = pb.start();

        // 프로세스 출력 읽기 (디버깅용)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Browser] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            // 브라우저가 종료되어도 파일이 생성되는 데 시간이 약간 걸릴 수 있음
            if (outputFile.exists()) {
                System.out.println("Success! PDF created at: " + outputFile.getAbsolutePath());
            } else {
                System.err.println(
                        "Browser process finished, but output file not found via check. Verify path permissions.");
            }
        } else {
            System.err.println("Conversion failed with exit code: " + exitCode);
        }
    }
}
