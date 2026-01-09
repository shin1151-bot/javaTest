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
        // [수정] --headless=new 모드 사용 (최신 크롬/엣지 렌더링 엔진 사용으로 더 정확한 출력)
        command.add("--headless=new");
        command.add("--disable-gpu");

        // [가로 모드 효과 구현]
        // CLI에서는 PDF 용지 방향을 가로(Landscape)로 바꾸는 옵션이 제한적입니다.
        // 대신, "노트북 해상도(1280px)"를 "A4용지 폭"에 딱 맞게 축소(0.6배)하여
        // 가로가 꽉 차게 나오도록 조정했습니다. (1280px * 0.6 = 768px ≒ A4폭)

        // 1. 데스크탑 User-Agent 복구
        command.add(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // [중요] 컨텐츠 로딩 대기
        command.add("--virtual-time-budget=10000");

        // 2. 뷰포트 너비 1280px (너무 넓은 1920px 대신 적절한 너비 사용)
        command.add("--window-size=1280,15000");

        // 3. 배율 0.6 (1280px를 A4에 맞춤)
        command.add("--force-device-scale-factor=0.6");

        command.add("--hide-scrollbars");
        command.add("--run-all-compositor-stages-before-draw");
        command.add("--no-pdf-header-footer");
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
