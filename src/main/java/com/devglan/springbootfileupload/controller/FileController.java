package com.devglan.springbootfileupload.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String contentType = "application/octet-stream";
    private static final String fileBasePath = "";
    private static final String zipFileName = "devglanZippedFile.zip";

    @PostMapping("/upload")
    public ResponseEntity uploadToLocalFileSystem(@RequestParam("file") MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path path = Paths.get(fileBasePath + fileName);
        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/download/")
                .path(fileName)
                .toUriString();
        return ResponseEntity.ok(fileDownloadUri);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFileFromLocal(@PathVariable String fileName) {
        Path path = Paths.get(fileBasePath + fileName);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/multi-upload")
    public ResponseEntity multiUpload(@RequestParam("files") MultipartFile[] files) {
        List<Object> fileDownloadUrls = new ArrayList<>();
        Arrays.asList(files)
                .stream()
                .forEach(file -> fileDownloadUrls.add(uploadToLocalFileSystem(file).getBody()));
        return ResponseEntity.ok(fileDownloadUrls);
    }

    @GetMapping(value = "/zip-download", produces="application/zip")
    public void zipDownload(@RequestParam List<String> name, HttpServletResponse response) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        for (String fileName : name) {
            FileSystemResource resource = new FileSystemResource(fileBasePath + fileName);
            ZipEntry zipEntry = new ZipEntry(resource.getFilename());
            zipEntry.setSize(resource.contentLength());
            zipOut.putNextEntry(zipEntry);
            StreamUtils.copy(resource.getInputStream(), zipOut);
            zipOut.closeEntry();
        }
        zipOut.finish();
        zipOut.close();
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
    }

    @PostMapping("/upload")
    public ResponseEntity uploadWithExtraParams(@RequestParam("file") MultipartFile file, @RequestParam String extraParam) {
        logger.info("Extra param " + extraParam);
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path path = Paths.get(fileBasePath + fileName);
        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/download/")
                .path(fileName)
                .toUriString();
        return ResponseEntity.ok(fileDownloadUri);
    }

    @PostMapping("/upload")
    public ResponseEntity uploadToDB(@RequestParam("file") MultipartFile file) {
        //TODO
        return null;
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFromDB(@PathVariable String fileName) {
        //TODO
        return null;
    }

}
