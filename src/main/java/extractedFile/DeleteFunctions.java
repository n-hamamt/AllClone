package extractedFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class DeleteFunctions {
    private final File directory;

    public DeleteFunctions(File directory) {
        this.directory = directory;
    }

    public void deleteFilesInDirectory() {
        Path dirPath = directory.toPath();

        try(Stream<Path> paths = Files.list(dirPath)){
            paths.forEach(path -> {
                // サブディレクトリを無視して、ファイルのみを削除
                if (Files.isRegularFile(path)) {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDirectoryRecursively() {
        Path dirPath = directory.toPath();

        try {
            // walkFileTreeでディレクトリとそのサブディレクトリ、ファイルをすべて処理
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {

                // ファイルを削除するメソッド
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                // ディレクトリを削除するメソッド
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                // エラーハンドリング
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
