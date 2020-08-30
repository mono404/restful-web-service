package com.mono.restfulwebservice.project.controller;

import com.google.cloud.storage.*;
import com.mono.restfulwebservice.project.SpeechToText;
import com.mono.restfulwebservice.project.payload.FileUploadResponse;
import com.mono.restfulwebservice.project.service.FileUploadDownloadService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@RestController
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    private FileUploadDownloadService service;

    public CommonController(FileUploadDownloadService service) {
        this.service = service;
    }

    //google cloud용 버킷, 스토리지 설정
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static Storage storage = null;

    @GetMapping(path = "/home")
    public ModelAndView home() {
        return new ModelAndView("home");
    }

    @GetMapping(path = "/realtime")
    public ModelAndView realTime() {
        return new ModelAndView("realtime");
    }

    @GetMapping(path = "/selectfile")
    public ModelAndView selectFile() {
        return new ModelAndView("selectfile");
    }

    @GetMapping(path = "/prac1")
    public ModelAndView prac1() {
        return new ModelAndView("prac1");
    }

    @GetMapping("/test")
    public ModelAndView fileUpload(HttpServletRequest req) {

        return new ModelAndView("test");
    }

    @PostMapping("/uploadFile")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = service.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    @PostMapping("/WavToText")
    public String WavToText(@RequestParam("file") MultipartFile file) throws Exception {

        String fileName = service.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        SpeechToText stt = new SpeechToText("./upload/" + fileName);

        return stt.getMessage();
    }

    @PostMapping("uploadMultipleFiles")
    public List<FileUploadResponse> uploadMultiFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = service.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("STTTest")
    public String STTTest() throws Exception {

        SpeechToText stt = new SpeechToText("./upload/test6.raw");

        return stt.getMessage();
    }

    @GetMapping("GCUpload")
    public ModelAndView GCUpload() {
        return new ModelAndView("GCUpload");
    }

    @PostMapping("GCUpload")
    public String GCUpload_Submit(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = service.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        // The ID of your GCP project
        String projectId = "my-project-1598356872302";

        // The ID of your GCS bucket
        String bucketName = "mono_jung";

        // The ID of your GCS object
         String objectName = fileName;

        // The path to your file to upload
        String filePath = "./upload/" + fileName;

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        return "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName;

    }
}
