package com.mono.restfulwebservice.project;

import com.google.api.client.util.IOUtils;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.nio.file.Files;

public class TranslateWav {

    String filePath;
    String result;

    public TranslateWav(String filePath) throws IOException {
        this.filePath = filePath;

        File file = new File(filePath);
        FileItem fileItem = new DiskFileItem("mainFile", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());

        InputStream input = new FileInputStream(file);
        OutputStream os = fileItem.getOutputStream();
        IOUtils.copy(input, os);

        MultipartFile multipartFile =new CommonsMultipartFile(fileItem);

        System.out.println(multipartFile.getName());
//        String result = changeCodec(filePath);
        result = makeFlacFile(multipartFile);

        System.out.println(result);
    }

    public static String makeFlacFile ( MultipartFile mp4File) throws IOException {
        String output = "";

//        String uuid = UUID.randomUUID().toString().replace("-", "");

        try {
//            String filePath = File.separator + "var" + File.separator + "stt" + File.separator;

            Encoder encoder = new Encoder();

            File source = new File(mp4File.getOriginalFilename());
            mp4File.transferTo(source);

            System.out.println(mp4File.getOriginalFilename());

            File target = new File("./upload/" + FilenameUtils.getBaseName(mp4File.getOriginalFilename()) + ".wav");
            System.out.println("타겟 : ./upload/" + FilenameUtils.getBaseName(mp4File.getOriginalFilename()) + ".wav");
            // 오디오 포맷 속성 정의. Google speech to text api 동작 조건
            AudioAttributes audio = new AudioAttributes();
            audio.setSamplingRate(new Integer(16000)); // 샘플 레이트
            audio.setChannels(new Integer(1)); // Mono 채널로 설정해야 speech to text api 사용 가능
            audio.setCodec("pcm_s16le");  // 코덱 조건

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("wav"); // 포맷 설정
            attrs.setAudioAttributes(audio);

            encoder.encode(source, target, attrs);

            output = target.getPath();

        } catch (Exception e) {
            output = "flacFile make fail";

        }

        return output;
    }

    public String getResult() {
        return result;
    }
}