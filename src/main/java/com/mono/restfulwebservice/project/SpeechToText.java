package com.mono.restfulwebservice.project;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Data
public class SpeechToText {

    String file;

    public SpeechToText(String filename) throws IOException {

        String extension = FilenameUtils.getExtension(filename);

        if(extension.equals("wav") || extension.equals("raw"))
        {
            System.out.println("raw or wav 파일입니다");
            file = filename;
        }
        else
        {
            System.out.println("wav파일이 아닙니다");
            TranslateWav translateWav = new TranslateWav(filename);
            file = "./upload/" + translateWav.getResult();
        }

    }

    private String Stt(String filename) throws Exception
    {
        System.out.println("시작");
        // Instantiates a client
        try (SpeechClient speechClient = SpeechClient.create()) {

            // The path to the audio file to transcribe
            String fileName = filename;
            System.out.println("경로설정 " + fileName);
            // Reads the audio file into memory
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("en-US").build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            System.out.println("테스트");
            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech.
                // Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());

                return alternative.getTranscript();
            }
        }
        return null;
    }

    public String getText() throws Exception {
        return Stt(file);
    }
}