package com.mono.restfulwebservice.project;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class UploadObject {

    public UploadObject(String projectId, String bucketName, String objectName, String filePath) throws IOException {

        String fileName;
        String extension = FilenameUtils.getExtension(filePath);

        if(extension.equals("wav") || extension.equals("raw"))
        {
            System.out.println("raw or wav 파일입니다");
            fileName = filePath;
        }
        else
        {
            System.out.println("wav파일이 아닙니다");
            TranslateWav translateWav = new TranslateWav(filePath);
            fileName = translateWav.getResult();
        }

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(fileName)));

        System.out.println(
                "File " + fileName + " uploaded to bucket " + bucketName + " as " + objectName);

    }

    public static void uploadObject(String projectId, String bucketName, String objectName, String filePath) throws IOException {
        // The ID of your GCP project
        // String projectId = "your-project-id";

        // The ID of your GCS bucket
        // String bucketName = "your-unique-bucket-name";

        // The ID of your GCS object
        // String objectName = "your-object-name";

        // The path to your file to upload
        // String filePath = "path/to/your/file"

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }

    public static void transcribeDiarization(String fileName, int speaker)throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {

            ArrayList<String> languageList = new ArrayList<>();
            languageList.add("ko-KR");

            SpeakerDiarizationConfig speakerDiarizationConfig =
                    SpeakerDiarizationConfig.newBuilder()
                            .setEnableSpeakerDiarization(true)
                            .setMinSpeakerCount(speaker)
                            .setMaxSpeakerCount(speaker)
                            .build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("es-US")
                    .addAllAlternativeLanguageCodes(languageList)
                    .setEnableAutomaticPunctuation(true)
                    .setDiarizationConfig(speakerDiarizationConfig)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(fileName)
                    .build();

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, audio);

            LongRunningRecognizeResponse longRunningRecognizeResponse = response.get();

            SpeechRecognitionAlternative alternative =
                    longRunningRecognizeResponse
                            .getResults(longRunningRecognizeResponse.getResultsCount() - 1)
                            .getAlternatives(0);

            WordInfo wordInfo = alternative.getWords(0);
            int currentSpeakerTag = wordInfo.getSpeakerTag();

            StringBuilder speakerWords = new StringBuilder(
                    String.format("Speaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));

            for (int i = 1; i < alternative.getWordsCount(); i++) {
                wordInfo = alternative.getWords(i);
                if(i == 1) System.out.printf("[%s.%s] ", wordInfo.getStartTime().getSeconds(),wordInfo.getStartTime().getNanos() / 100000000);
                if (currentSpeakerTag == wordInfo.getSpeakerTag()) {
                    speakerWords.append(" ");
                    speakerWords.append(wordInfo.getWord());
                } else {
                    speakerWords.append(
                            String.format("\nSpeaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));
                    currentSpeakerTag = wordInfo.getSpeakerTag();
                }
            }

            System.out.println(speakerWords.toString());

        }
    }
}
