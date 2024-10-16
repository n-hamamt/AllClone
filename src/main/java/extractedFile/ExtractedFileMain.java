package extractedFile;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ExtractedFileMain {

    static int commitNumber = 0;
    static int total = 0;
    static int validFiles;
    static int validTests;

    public static void main(final String[] args){
        //対象のgitリポジトリURL
        String remoteUrl = "https://github.com/TheAlgorithms/Java.git";
        //リポジトリのファイルを格納するディレクトリ
        File localPath = new File("src/gitRepo");
        //目的のjavaプログラムのパス
        String targetFilePath = "src/main/java/com/thealgorithms/backtracking/FloodFill.java";
        //目的のテストファイルのパス
        String targetTestPath = "src/test/java/com/thealgorithms/backtracking/FloodFillTest.java";

        String targetFileName = targetFilePath.substring(targetFilePath.lastIndexOf('/') + 1, targetFilePath.lastIndexOf('.'));
        String targetTestName = targetTestPath.substring(targetTestPath.lastIndexOf('/') + 1, targetTestPath.lastIndexOf('.'));
        //目的のファイルを格納するディレクトリ
        File targetFileDir = new File("src/targetFile/" + targetFileName);
        File targetTestDir = new File("src/targetTest/" + targetTestName);



        try{
            // git clone
            System.out.println("Cloning from " + remoteUrl + " to " + localPath);
            gitClone(remoteUrl, localPath);

            try(Git git = Git.open(localPath)) {
                //get commit SHAs info(newest order)
                //SHAコード(コミットID)を新しい順に取得
                List<String> commitShas = new ArrayList<>();
                Iterable<RevCommit> commits = git.log().call();
                System.out.println("List of commit SHAs:");
                for (RevCommit commit : commits) {
                    commitShas.add(commit.getName().substring(0, 7));
                    //System.out.println(commit.getName().substring(0, 7));
                    total++;
                }
                System.out.println("Total commits: " + total);

                //////目的のファイルを各SHAごとに抽出//////
                //格納用ディレクトリがなければ新規に作成
                if(!targetFileDir.exists()){
                    targetFileDir.mkdirs();
                } else {

                }
                if(!targetTestDir.exists()){
                    targetTestDir.mkdirs();
                }

                for (String sha : commitShas) {
                    commitNumber++;
                    extractFileForCommit(git.getRepository(), sha, targetFilePath, targetFileDir, targetFileName);
                    extractTestForCommit(git.getRepository(), sha, targetTestPath, targetTestDir, targetTestName);
                }

                //重複ファイルを削除
                DuplicateFileRemover duplicateFileRemover =
                        new DuplicateFileRemover(targetFileName, targetTestName, targetFileDir, targetTestDir, validFiles, validTests);

                System.out.println("Valid files: " + duplicateFileRemover.runFile());
                System.out.println("Valid tests: " + duplicateFileRemover.runTest());

                DeleteFunctions deleteFunctions = new DeleteFunctions(new File("src/gitRepo"));
                deleteFunctions.deleteDirectoryRecursively();
            }

        } catch(GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void gitClone(String remoteUrl, File localPath) throws GitAPIException {
        CloneCommand clone = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(localPath);

        try(Git git = clone.call()) {
            System.out.println("Repository cloned to " + git.getRepository().getDirectory());
        } catch (GitAPIException e){
            e.printStackTrace();
        }
    }

    private static void extractFileForCommit(Repository repository, String commitSha,
                                             String filePath, File targetDir, String fileName) throws GitAPIException, IOException {

        //SHAコードから特定のファイルを取得
        ObjectId commitId = repository.resolve(commitSha);
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            treeWalk.addTree(repository.parseCommit(commitId).getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));

            if(treeWalk.next()){
                //Files.createDirectories(new File(targetDir + "/" + String.format("%03d", commitNumber)).toPath());

                ObjectId objectId = treeWalk.getObjectId(0);
                File extractedFile = new File(targetDir, String.format("%03d", commitNumber) + ".java");
                Files.copy(repository.open(objectId).openStream(), extractedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //System.out.println("Extracted file for commit " + commitSha + ": " + extractedFile.getPath());
            } else {
                //System.out.println("File not found in commit " + commitNumber + ":" + commitSha);
            }
        }
    }

    private static void extractTestForCommit(Repository repository, String commitSha,
                                             String testPath, File testDir, String testName) throws GitAPIException, IOException {

        //SHAコードから特定のファイルを取得
        ObjectId commitId = repository.resolve(commitSha);
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            treeWalk.addTree(repository.parseCommit(commitId).getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(testPath));

            if(treeWalk.next()){
                //Files.createDirectories(new File(testDir + "/" + String.format("%03d", commitNumber)).toPath());

                ObjectId objectId = treeWalk.getObjectId(0);
                File extractedFile = new File(testDir, String.format("%03d", commitNumber) + ".java");
                Files.copy(repository.open(objectId).openStream(), extractedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //System.out.println("Extracted file for commit " + commitSha + ": " + extractedFile.getPath());
            } else {
                //System.out.println("Test not found in commit " + commitNumber + ":" + commitSha);
            }
        }
    }
}
