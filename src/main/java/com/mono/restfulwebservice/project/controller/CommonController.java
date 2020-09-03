package com.mono.restfulwebservice.project.controller;

import com.google.cloud.storage.*;
import com.mono.restfulwebservice.project.SpeechToText;
import com.mono.restfulwebservice.project.UploadObject;
import com.mono.restfulwebservice.project.payload.FileUploadResponse;
import com.mono.restfulwebservice.project.service.FileUploadDownloadService;
import lombok.Data;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.apache.commons.io.FilenameUtils;
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

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @PostMapping("/wavtotxt")
    public ResponseEntity<Resource> WavToText(@RequestParam("file") MultipartFile file, HttpServletRequest req) throws Exception {

        String fileName = service.storeFile(file);
        String downUri = "./upload/" + FilenameUtils.getBaseName(file.getOriginalFilename()) + ".txt";
        String txtName = FilenameUtils.getBaseName(file.getOriginalFilename()) + ".txt";

        String fileDownloadUri_r = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        System.out.println(new FileUploadResponse(fileName, fileDownloadUri_r, file.getContentType(), file.getSize()));

        SpeechToText stt = new SpeechToText("./upload/" + fileName);

        try{
            OutputStream output = new FileOutputStream(downUri);
            byte[] by = stt.getText().getBytes();
            output.write(by);
        } catch (Exception e) {
            e.getStackTrace();
        }

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(txtName)
                .toUriString();

        System.out.println(new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize()));

        //return new ModelAndView("test");

        Resource res = service.loadFileAsResource(txtName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = req.getServletContext().getMimeType(res.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFilename() + "\"")
                .body(res);
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

    @PostMapping("/uploadMultipleFiles")
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

    @PostMapping("/downloadFile/{fileName}")
    public ResponseEntity<Resource> downloadFile_v2(@PathVariable String fileName, HttpServletRequest request) {
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

        return stt.getText();
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

//        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
//        BlobId blobId = BlobId.of(bucketName, objectName);
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
//        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        UploadObject uploadObject = new UploadObject(projectId, bucketName, objectName, filePath);

        return "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName;

    }
}
