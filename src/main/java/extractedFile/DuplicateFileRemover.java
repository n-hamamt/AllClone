package extractedFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;

public class DuplicateFileRemover {
    private final String targetFileName;
    private final String targetTestName;
    private final File fileDirectory;
    private final File testDirectory;
    private int validFiles = 0;
    private int validTests = 0;

    //コンストラクタ
    public DuplicateFileRemover(String fileName, String testName, File fileDirectory, File testDirectory,
                                int validFiles, int validTests) {
        this.targetFileName = fileName;
        this.targetTestName = testName;
        this.fileDirectory = fileDirectory;
        this.testDirectory = testDirectory;
        this.validFiles = validFiles;
        this.validTests = validTests;
    }

    public int runFile() throws IOException {
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            System.err.println("Directory error");
            return -1;
        }

        //重複チェックを行うためのファイルのハッシュマップ
        Map<String, File> uniqueFiles = new HashMap<>();
        List<File> filesToDelete = new ArrayList<>();

        // ディレクトリ内のすべてのJavaファイルを再帰的に取得し、重複するものをリストに追加
        getFilesRecursively(fileDirectory, uniqueFiles, filesToDelete);

        // 重複ファイルを削除
        DeleteFunctions deleteFilesInDirectory = new DeleteFunctions(fileDirectory);
        deleteFilesInDirectory.deleteFilesInDirectory();

        return validFiles;
    }

    public int runTest() throws IOException {
        if (!testDirectory.exists() || !testDirectory.isDirectory()) {
            System.err.println("Directory error");
            return -1;
        }

        //重複チェックを行うためのファイルのハッシュマップ
        Map<String, File> uniqueFiles = new HashMap<>();
        List<File> filesToDelete = new ArrayList<>();

        // ディレクトリ内のすべてのJavaファイルを再帰的に取得し、重複するものをリストに追加
        getTestsRecursively(testDirectory, uniqueFiles, filesToDelete);

        // ファイルを削除
        DeleteFunctions deleteFilesInDirectory = new DeleteFunctions(testDirectory);
        deleteFilesInDirectory.deleteFilesInDirectory();

        return validTests;
    }


    private void getFilesRecursively(File dir, Map<String, File> uniqueFiles, List<File> filesToDelete)
            throws IOException {
        File[] files = dir.listFiles();

        if (files != null) {
            Arrays.sort(files);

            int commitNumber = 0;

            for (File file : files) {
                commitNumber++;

                //ファイル内容をハッシュに変換
                String fileHash = hashFileContents(file);

                if(!uniqueFiles.containsKey(fileHash)) {
                    // ユニークなファイルとしてマップに追加
                    uniqueFiles.put(fileHash, file);
                    String fileName = file.getName();
                    String fileNameExcludingJava = fileName.substring(0, fileName.length()-5);

                    validFiles++;
                    Files.createDirectories(new File(fileDirectory + "/" + String.format("%03d", commitNumber)).toPath());

                    Files.move(file.toPath(), new File(fileDirectory + "/" + String.format("%03d", commitNumber) + "/" + targetFileName + ".java").toPath(),
                            StandardCopyOption.REPLACE_EXISTING);

                    //File newFile = new File(fileDirectory + "/" + fileNameExcludingJava + "_" + String.format("%02d",validFiles) + ".java");
                }
            }
        }
    }

    private void getTestsRecursively(File dir, Map<String, File> uniqueFiles, List<File> filesToDelete)
            throws IOException {
        File[] files = dir.listFiles();

        if (files != null) {
            Arrays.sort(files);
            int commitNumber = 0;

            for (File file : files) {
                commitNumber++;

                //ファイル内容をハッシュに変換
                String fileHash = hashFileContents(file);

                if(uniqueFiles.containsKey(fileHash)) {
                    // ハッシュが同じ場合は重複ファイルとしてリストに追加
                    filesToDelete.add(file);
                } else {
                    // ユニークなファイルとしてマップに追加
                    uniqueFiles.put(fileHash, file);
                    String testName = file.getName();
                    String testNameExcludingJava = testName.substring(0, testName.length()-5);

                    validTests++;

                    Files.createDirectories(new File(testDirectory + "/" + String.format("%03d", commitNumber)).toPath());

                    Files.move(file.toPath(), new File(testDirectory + "/" + String.format("%03d", commitNumber) + "/" + targetTestName + ".java").toPath(),
                            StandardCopyOption.REPLACE_EXISTING);

                    //lassNameReplacer classNameReplacer = new ClassNameReplacer(newTest);
                    //classNameReplacer.replaceClassName(targetTestName, testNameExcludingJava + "_" + String.format("%02d",validTests));
                }
            }
        }
    }

    // ファイルの内容をハッシュ化するメソッド (SHA-256を使用)
    private String hashFileContents(File file) throws IOException {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1){
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // バイト配列を16進数文字列に変換するヘルパーメソッド
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
