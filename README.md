# URL to PDF Converter

웹 페이지 URL을 PDF 파일로 변환하는 간단한 Java 프로그램입니다.

## 요구 사항

- Java 11 이상
- Maven

## 빌드 방법

터미널에서 다음 명령어를 실행하여 프로젝트를 빌드하세요:

```bash
mvn clean package
```

## 실행 방법

빌드가 완료되면 생성된 JAR 파일을 실행합니다:

```bash
java -jar target/url-to-pdf-1.0-SNAPSHOT.jar [URL] [출력파일경로]
```

### 사용 예시

```bash
java -jar target/url-to-pdf-1.0-SNAPSHOT.jar https://www.google.com google.pdf
```

실행 인자가 없으면 기본적으로 `https://example.com`을 `output.pdf`로 변환합니다.

## 주요 라이브러리

- **OpenHTMLtoPDF**: HTML을 PDF로 렌더링하는 핵심 라이브러리입니다.
- **Jsoup**: URL에서 HTML을 가져오고 파싱하여 W3C DOM으로 변환하기 위해 사용됩니다.
