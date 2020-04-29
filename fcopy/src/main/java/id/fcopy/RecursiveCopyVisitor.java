/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;

import id.xfunction.XUtils;

public class RecursiveCopyVisitor implements FileVisitor<Path> {
    private Path srcPath;
    private Path dstPath;
    private BiConsumer<Path, Path> copier;
    
    public RecursiveCopyVisitor(Path srcPath, Path dstPath, BiConsumer<Path, Path> copier) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
        this.copier = copier;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        copier.accept(file, relativeToDestination(file));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Files.createDirectories(relativeToDestination(dir));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc)
            throws IOException {
        XUtils.throwRuntime("Failed to open %s: %s", file, exc.getMessage());
        return FileVisitResult.TERMINATE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException {
        return FileVisitResult.CONTINUE;
    }

    private Path relativeToDestination(Path file) {
        return dstPath.resolve(srcPath.relativize(file));
    }

}
