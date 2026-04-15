package edu.up.cg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String ENV_TOKEN = "OpenAIToken";
    private static final String API_URL = "https://api.openai.com/v1/responses";
    private static final String MODEL = "gpt-4.1-nano"; // this one is cheap
    private static final int REQUEST_TIMEOUT_SECONDS = 20;
    private static final String TRANSLATION_PROMPT_PREFIX =
            "You are a master linguist with the best knowledge of all languages ";
    private static final String TRANSLATION_PROMPT_SUFFIX =
            " your task is to translate the text given to the target language adhering to the bare minimum translation and nothing more: ";

    private static String requireToken() {
        String token = System.getenv(ENV_TOKEN);
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("You should export to env vars 'OpenAIToken'");
        }
        return token;
    }

    private static String askInput(Scanner scanner, String label) {
        System.out.print(label);
        String value = scanner.nextLine();
        System.out.println();
        return value;
    }

    private static File requireExistingInputFile(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("The given file does not exist");
        }
        return file;
    }

    private static File prepareOutputFile(String path) throws Exception {
        File output = new File(path);
        if (!output.exists()) {
            output.createNewFile();
        }
        return output;
    }

    private static String readFile(File file) throws Exception {
        try (FileInputStream fsi = new FileInputStream(file)) {
            return new String(fsi.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String buildPrompt(String language, String fileContent) {
        return TRANSLATION_PROMPT_PREFIX + language + TRANSLATION_PROMPT_SUFFIX + fileContent;
    }

    private static String escapeJson(String raw) {
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String buildRequestBody(String language, String fileContent) {
        String prompt = escapeJson(buildPrompt(language, fileContent));
        return "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"input\":["
                + "{\"role\":\"user\",\"content\":\"" + prompt + "\"}"
                + "]"
                + "}";
    }

    private static File prepareRequestPayloadFile(String body) throws Exception {
        File tempJson = File.createTempFile("openai_req", ".json");
        try (FileOutputStream fos = new FileOutputStream(tempJson)) {
            fos.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return tempJson;
    }

    private static ProcessBuilder prepareCurlRequest(File requestPayload, String token) {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add(API_URL);
        command.add("-H");
        command.add("Content-Type: application/json");
        command.add("-H");
        command.add("Authorization: Bearer " + token);
        command.add("-d");
        command.add("@" + requestPayload.getAbsolutePath());
        return new ProcessBuilder(command);
    }

    private static String executeCurlRequest(File requestPayload, String token) throws Exception {
        Process process = prepareCurlRequest(requestPayload, token).start();
        boolean finished = process.waitFor(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Process ran for too long");
        }

        try (InputStream is = process.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String decodeJsonString(String value) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c != '\\') {
                out.append(c);
                i++;
                continue;
            }

            if (i + 1 >= value.length()) {
                break;
            }

            char next = value.charAt(i + 1);
            if (next == 'u' && i + 5 < value.length()) {
                String hex = value.substring(i + 2, i + 6);
                out.append((char) Integer.parseInt(hex, 16));
                i += 6;
                continue;
            }

            switch (next) {
                case '"':
                    out.append('"');
                    break;
                case '\\':
                    out.append('\\');
                    break;
                case 'n':
                    out.append('\n');
                    break;
                case 'r':
                    out.append('\r');
                    break;
                case 't':
                    out.append('\t');
                    break;
                default:
                    out.append(next);
                    break;
            }
            i += 2;
        }
        return out.toString();
    }

    private static String extractFirstTextField(String response) {
        int textKey = response.indexOf("\"text\"");
        if (textKey < 0) {
            throw new IllegalStateException("Text not found in api response.");
        }

        int colon = response.indexOf(':', textKey);
        int startQuote = response.indexOf('"', colon + 1);
        if (colon < 0 || startQuote < 0) {
            throw new IllegalStateException("Invalid API response.");
        }

        StringBuilder rawValue = new StringBuilder();
        int i = startQuote + 1;
        boolean escaping = false;

        while (i < response.length()) {
            char c = response.charAt(i);
            if (!escaping && c == '"') {
                return decodeJsonString(rawValue.toString());
            }

            if (c == '\\' && !escaping) {
                escaping = true;
                rawValue.append(c);
                i++;
                continue;
            }

            escaping = false;
            rawValue.append(c);
            i++;
        }

        throw new IllegalStateException("Could not parse translated text.");
    }

    private static void writeOutput(File outputFile, String content) throws Exception {
        try (FileOutputStream fso = new FileOutputStream(outputFile)) {
            fso.write(content.getBytes(StandardCharsets.UTF_8));
            fso.flush();
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String token = requireToken();
            String inputPath = askInput(scanner, "Enter input file route:\t");
            String outputPath = askInput(scanner, "Enter output file route:\t");
            String language = askInput(scanner, "Enter the target language:\t");

            File inputFile = requireExistingInputFile(inputPath);
            File outputFile = prepareOutputFile(outputPath);

            String fileContent = readFile(inputFile);
            String body = buildRequestBody(language, fileContent);
            File payloadFile = prepareRequestPayloadFile(body);

            try {
                String response = executeCurlRequest(payloadFile, token);
                String translatedText = extractFirstTextField(response);
                writeOutput(outputFile, translatedText);
            } finally {
                payloadFile.delete();
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }
}
